package com.atelier.gestion.tramite.service;

import static com.atelier.gestion.tramite.dto.TramiteDtos.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.documento.entity.CategoriaDocumento;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.tramite.entity.*;
import com.atelier.gestion.tramite.repository.*;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TramiteService {
  private static final Set<EstadoTramite> ACTIVOS=EnumSet.of(EstadoTramite.PREPARACION,
      EstadoTramite.PRESENTADO,EstadoTramite.EN_REVISION,EstadoTramite.OBSERVADO);
  private final TramitePublicoRepository tramites;private final ObservacionEntidadRepository observaciones;
  private final ProyectoRepository proyectos;private final ExpedienteRepository expedientes;
  private final EmpleadoRepository empleados;private final DocumentoRepository documentos;
  private final HistorialEstadoRepository historial;private final AutorizacionProyectoService acceso;
  private final AuditoriaService auditoria;private final Clock clock;

  public TramiteService(TramitePublicoRepository tramites,ObservacionEntidadRepository observaciones,
      ProyectoRepository proyectos,ExpedienteRepository expedientes,EmpleadoRepository empleados,
      DocumentoRepository documentos,HistorialEstadoRepository historial,
      AutorizacionProyectoService acceso,AuditoriaService auditoria,Clock clock){
    this.tramites=tramites;this.observaciones=observaciones;this.proyectos=proyectos;
    this.expedientes=expedientes;this.empleados=empleados;this.documentos=documentos;
    this.historial=historial;this.acceso=acceso;this.auditoria=auditoria;this.clock=clock;
  }

  @Transactional(readOnly=true)
  public List<Respuesta> listar(UUID proyectoId){acceso.buscarVisible(proyectoId);return tramites
      .findByProyectoIdOrderByCreadoEnDesc(proyectoId).stream().map(this::dto).toList();}

  @Transactional(readOnly=true)
  public Respuesta obtener(UUID id){TramitePublico t=buscar(id);acceso.verificarVisible(t.getProyecto(),acceso.usuario());return dto(t);}

  @Transactional
  public Respuesta crear(UUID proyectoId,Crear req){Usuario u=acceso.usuario();acceso.exigirGestor(u);
    Proyecto p=proyectos.bloquear(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));
    if(!p.isRequiereTramitePublico())throw new ReglaNegocioException("El proyecto no requiere trámite público");
    if(p.getEstado()!=EstadoProyecto.EXPEDIENTE_FINAL)throw new ReglaNegocioException("El trámite se prepara desde el expediente final");
    if(tramites.existsByProyectoIdAndEstadoIn(proyectoId,ACTIVOS))throw new ConflictoException("Ya existe un trámite activo para el proyecto");
    Empleado responsable=empleado(req.responsableId());
    TramitePublico t=tramites.save(new TramitePublico(p,responsable,limpiar(req.entidad()),
        limpiar(req.tipoTramite()),opcional(req.numeroExpedienteExterno()),
        opcional(req.observacionGeneral()),OffsetDateTime.now(clock)));
    auditoria.registrar(u,"tramites_publicos",t.getId(),"INSERT",Map.of("estado",t.getEstado().name()));return dto(t);}

  @Transactional
  public Respuesta actualizar(UUID id,Actualizar req){Usuario u=acceso.usuario();acceso.exigirGestor(u);
    TramitePublico t=tramites.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Trámite no encontrado"));
    if(Set.of(EstadoTramite.APROBADO,EstadoTramite.RECHAZADO,EstadoTramite.ARCHIVADO).contains(t.getEstado()))
      throw new ReglaNegocioException("Un trámite resuelto no puede modificarse");
    t.actualizar(empleado(req.responsableId()),limpiar(req.entidad()),limpiar(req.tipoTramite()),
        opcional(req.numeroExpedienteExterno()),opcional(req.observacionGeneral()));
    if(req.estado()!=null&&req.estado()!=t.getEstado())cambiarEstadoTramite(t,req.estado(),req.fechaPresentacion(),u);
    auditoria.registrar(u,"tramites_publicos",id,"UPDATE",Map.of("estado",t.getEstado().name()));return dto(t);}

  @Transactional(readOnly=true)
  public List<ObservacionRespuesta> listarObservaciones(UUID tramiteId){TramitePublico t=buscar(tramiteId);
    acceso.verificarVisible(t.getProyecto(),acceso.usuario());return observaciones.findByTramiteIdOrderByNumeroAsc(tramiteId)
        .stream().map(this::dto).toList();}

  @Transactional
  public ObservacionRespuesta observar(UUID tramiteId,CrearObservacion req){Usuario u=acceso.usuario();acceso.exigirGestor(u);
    TramitePublico t=tramites.bloquear(tramiteId).orElseThrow(()->new RecursoNoEncontradoException("Trámite no encontrado"));
    if(!Set.of(EstadoTramite.PRESENTADO,EstadoTramite.EN_REVISION).contains(t.getEstado()))
      throw new ReglaNegocioException("Solo un trámite presentado o en revisión puede recibir observaciones");
    if(t.getProyecto().getEstado()!=EstadoProyecto.EN_TRAMITE_PUBLICO)
      throw new ReglaNegocioException("El proyecto no está en trámite público");
    validarFechas(req.fechaNotificacion(),req.fechaLimite());short numero=(short)(observaciones.maxNumero(tramiteId)+1);
    ObservacionEntidad o=observaciones.save(new ObservacionEntidad(t,numero,limpiar(req.descripcion()),
        req.fechaNotificacion(),req.fechaLimite()));t.observar();
    cambiarProyecto(t.getProyecto(),EstadoProyecto.CON_OBSERVACIONES,u,"Observación de entidad N.° "+numero);
    auditoria.registrar(u,"observaciones_entidad",o.getId(),"INSERT",Map.of("numero",numero));return dto(o);}

  @Transactional
  public ObservacionRespuesta subsanar(UUID id,Subsanar req){Usuario u=acceso.usuario();acceso.exigirGestor(u);
    ObservacionEntidad referencia=observaciones.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Observación no encontrada"));
    TramitePublico t=tramites.bloquear(referencia.getTramite().getId()).orElseThrow();
    ObservacionEntidad o=observaciones.bloquear(id).orElseThrow();
    if(o.getEstado()!=EstadoObservacionEntidad.PENDIENTE)throw new ReglaNegocioException("La observación ya fue atendida");
    if(t.getEstado()!=EstadoTramite.OBSERVADO)throw new ReglaNegocioException("El trámite no está observado");
    if(t.getProyecto().getEstado()!=EstadoProyecto.CON_OBSERVACIONES)throw new ReglaNegocioException("El proyecto no tiene observaciones públicas pendientes");
    if(req.fechaSubsanacion().isBefore(o.getFechaNotificacion()))throw new ReglaNegocioException("La subsanación no puede ser anterior a la notificación");
    o.subsanar(limpiar(req.respuesta()),req.fechaSubsanacion());
    if(observaciones.countByTramiteIdAndEstado(t.getId(),EstadoObservacionEntidad.PENDIENTE)==0){
      t.reanudar();cambiarProyecto(t.getProyecto(),EstadoProyecto.EN_TRAMITE_PUBLICO,u,"Observaciones de entidad subsanadas");}
    auditoria.registrar(u,"observaciones_entidad",id,"UPDATE",Map.of("estado",o.getEstado().name()));return dto(o);}

  @Transactional
  public Respuesta resolver(UUID id,Resolver req){Usuario u=acceso.usuario();acceso.exigirGestor(u);
    TramitePublico t=tramites.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Trámite no encontrado"));
    if(!Set.of(EstadoTramite.PRESENTADO,EstadoTramite.EN_REVISION,EstadoTramite.OBSERVADO).contains(t.getEstado()))
      throw new ReglaNegocioException("El trámite no está pendiente de resolución");
    if(!Set.of(EstadoProyecto.EN_TRAMITE_PUBLICO,EstadoProyecto.CON_OBSERVACIONES).contains(t.getProyecto().getEstado()))
      throw new ReglaNegocioException("El proyecto no está pendiente de resolución pública");
    if(t.getFechaPresentacion()==null)throw new ReglaNegocioException("El trámite no tiene fecha de presentación");
    if(req.fechaRespuesta().isBefore(t.getFechaPresentacion()))throw new ReglaNegocioException("La respuesta no puede ser anterior a la presentación");
    if(req.aprobado()){
      if(observaciones.countByTramiteIdAndEstado(id,EstadoObservacionEntidad.PENDIENTE)>0)
        throw new ReglaNegocioException("No se puede aprobar con observaciones pendientes");
      if(!documentos.existsByTramiteIdAndCategoriaAndVigenteTrue(id,CategoriaDocumento.RESOLUCION))
        throw new ReglaNegocioException("Debe adjuntar la resolución o licencia antes de aprobar");
    }
    t.resolver(req.aprobado(),req.fechaRespuesta(),limpiar(req.observacionGeneral()));
    EstadoProyecto destino=req.aprobado()?EstadoProyecto.APROBADO:EstadoProyecto.NO_VIABLE;
    cambiarProyecto(t.getProyecto(),destino,u,req.aprobado()?"Trámite público aprobado":"Trámite público no viable");
    auditoria.registrar(u,"tramites_publicos",id,"UPDATE",Map.of("resultado",t.getEstado().name()));return dto(t);}

  private void cambiarEstadoTramite(TramitePublico t,EstadoTramite nuevo,LocalDate fecha,Usuario u){
    if(t.getEstado()==EstadoTramite.PREPARACION&&nuevo==EstadoTramite.PRESENTADO){
      if(t.getProyecto().getEstado()!=EstadoProyecto.EXPEDIENTE_FINAL)
        throw new ReglaNegocioException("El proyecto ya no está disponible para presentar el trámite");
      Expediente e=expedientes.findByProyectoId(t.getProyecto().getId())
          .orElseThrow(()->new ReglaNegocioException("El proyecto no tiene expediente digital"));
      if(!"COMPLETO".equals(e.getEstado()))throw new ReglaNegocioException("El expediente final debe estar completo antes de presentar el trámite");
      if(StringUtils.isBlank(t.getNumeroExpedienteExterno()))throw new ReglaNegocioException("La presentación exige el número de expediente de la entidad");
      LocalDate presentacion=fecha==null?LocalDate.now(clock):fecha;
      if(presentacion.isAfter(LocalDate.now(clock)))throw new ReglaNegocioException("La presentación no puede tener fecha futura");
      t.presentar(presentacion);cambiarProyecto(t.getProyecto(),EstadoProyecto.EN_TRAMITE_PUBLICO,u,"Trámite presentado ante "+t.getEntidad());return;
    }
    if(t.getEstado()==EstadoTramite.PRESENTADO&&nuevo==EstadoTramite.EN_REVISION){t.revisar();return;}
    throw new ReglaNegocioException("Transición de trámite no permitida");
  }
  private void cambiarProyecto(Proyecto p,EstadoProyecto nuevo,Usuario u,String comentario){EstadoProyecto anterior=p.getEstado();
    if(anterior==nuevo)return;p.cambiarEstado(nuevo);historial.save(new HistorialEstado(p,u,anterior,nuevo,comentario));}
  private Empleado empleado(UUID id){return empleados.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Responsable no encontrado"));}
  private TramitePublico buscar(UUID id){return tramites.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Trámite no encontrado"));}
  private void validarFechas(LocalDate notificacion,LocalDate limite){if(limite!=null&&limite.isBefore(notificacion))throw new ReglaNegocioException("El plazo no puede ser anterior a la notificación");}
  private String limpiar(String valor){return StringUtils.trim(valor);}private String opcional(String valor){return StringUtils.trimToNull(valor);}
  private Respuesta dto(TramitePublico t){Empleado e=t.getResponsable();return new Respuesta(t.getId(),t.getProyecto().getId(),
      e==null?null:e.getId(),e==null?null:e.getUsuario().getNombres()+" "+e.getUsuario().getApellidos(),t.getEntidad(),
      t.getTipoTramite(),t.getNumeroExpedienteExterno(),t.getEstado(),t.getFechaPresentacion(),t.getFechaRespuesta(),t.getObservacionGeneral(),t.getCreadoEn());}
  private ObservacionRespuesta dto(ObservacionEntidad o){return new ObservacionRespuesta(o.getId(),o.getTramite().getId(),o.getNumero(),
      o.getDescripcion(),o.getFechaNotificacion(),o.getFechaLimite(),o.getEstado(),o.getRespuesta(),o.getFechaSubsanacion());}
}
