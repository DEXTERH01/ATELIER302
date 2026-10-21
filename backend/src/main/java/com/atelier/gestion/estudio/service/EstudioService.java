package com.atelier.gestion.estudio.service;
import static com.atelier.gestion.estudio.dto.EstudioDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.estudio.entity.*;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class EstudioService{
  private static final Map<EstadoEstudio,Set<EstadoEstudio>> TRANSICIONES=Map.of(EstadoEstudio.PENDIENTE,Set.of(EstadoEstudio.PENDIENTE,EstadoEstudio.EN_PROCESO,EstadoEstudio.COMPLETADO,EstadoEstudio.NO_APLICA),EstadoEstudio.EN_PROCESO,Set.of(EstadoEstudio.EN_PROCESO,EstadoEstudio.COMPLETADO),EstadoEstudio.COMPLETADO,Set.of(EstadoEstudio.COMPLETADO),EstadoEstudio.NO_APLICA,Set.of(EstadoEstudio.NO_APLICA));
  private final EstudioTecnicoRepository repo;private final ProyectoRepository proyectos;private final EmpleadoRepository empleados;private final DocumentoRepository documentos;private final HistorialEstadoRepository historial;private final AutorizacionProyectoService acceso;private final AuditoriaService auditoria;
  public EstudioService(EstudioTecnicoRepository r,ProyectoRepository p,EmpleadoRepository e,DocumentoRepository d,HistorialEstadoRepository h,AutorizacionProyectoService a,AuditoriaService au){repo=r;proyectos=p;empleados=e;documentos=d;historial=h;acceso=a;auditoria=au;}
  @Transactional(readOnly=true) public List<Respuesta> listar(UUID proyectoId){acceso.buscarVisible(proyectoId);return repo.findByProyectoIdOrderByTipoAsc(proyectoId).stream().map(this::dto).toList();}
  @Transactional public Respuesta crear(UUID proyectoId,Crear req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Proyecto p=proyectos.bloquear(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));if(!Set.of(EstadoProyecto.PROYECTO_ACTIVO,EstadoProyecto.EN_ESTUDIOS).contains(p.getEstado()))throw new ReglaNegocioException("Los estudios solo se planifican en un proyecto activo");Empleado responsable=empleado(req.responsableId());EstudioTecnico e=repo.save(new EstudioTecnico(p,responsable,StringUtils.trim(req.tipo()),StringUtils.trimToNull(req.proveedor()),req.requerido(),req.fechaSolicitud()==null?LocalDate.now():req.fechaSolicitud(),req.costo()));if(req.requerido()&&p.getEstado()==EstadoProyecto.PROYECTO_ACTIVO)cambiarEstado(p,EstadoProyecto.EN_ESTUDIOS,u,"Estudio técnico requerido: "+e.getTipo());auditoria.registrar(u,"estudios_tecnicos",e.getId(),"INSERT",Map.of("tipo",e.getTipo()));return dto(e);}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Usuario u=acceso.usuario();EstudioTecnico e=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Estudio no encontrado"));boolean gestor=acceso.esGestor(u);boolean responsable=e.getResponsable()!=null&&u.getId().equals(e.getResponsable().getUsuario().getId());if(!gestor&&!responsable)throw new AccessDeniedException("Operación reservada al responsable o gestor");if(!TRANSICIONES.get(e.getEstado()).contains(req.estado()))throw new ReglaNegocioException("Transición de estudio no permitida");if(!gestor){if(req.estado()==EstadoEstudio.COMPLETADO||req.estado()==EstadoEstudio.NO_APLICA)throw new AccessDeniedException("La validación del estudio corresponde al gestor");if(cambioEstructural(e,req))throw new AccessDeniedException("El especialista solo puede actualizar avance y resultado");}
    if(req.estado()==EstadoEstudio.NO_APLICA&&req.requerido())throw new ReglaNegocioException("Un estudio requerido no puede marcarse como no aplicable");if(req.estado()==EstadoEstudio.COMPLETADO){if(StringUtils.isBlank(req.resultado())||req.fechaResultado()==null)throw new ReglaNegocioException("Completar el estudio exige resultado y fecha");if(!documentos.existsByEstudioIdAndVigenteTrue(id))throw new ReglaNegocioException("Debe adjuntar un entregable vigente antes de validar el estudio");}
    Empleado responsableNuevo=gestor?empleado(req.responsableId()):e.getResponsable();e.actualizar(responsableNuevo,StringUtils.trim(req.tipo()),StringUtils.trimToNull(req.proveedor()),req.requerido(),req.estado(),req.fechaSolicitud(),req.fechaResultado(),StringUtils.trimToNull(req.resultado()),req.costo());repo.flush();Proyecto p=proyectos.bloquear(e.getProyecto().getId()).orElseThrow();if(gestor&&p.getEstado()==EstadoProyecto.EN_ESTUDIOS&&repo.countByProyectoIdAndRequeridoTrueAndEstadoNot(p.getId(),EstadoEstudio.COMPLETADO)==0)cambiarEstado(p,EstadoProyecto.EN_DISENO,u,"Todos los estudios requeridos fueron validados");auditoria.registrar(u,"estudios_tecnicos",id,"UPDATE",Map.of("estado",req.estado().name()));return dto(e);}
  private Empleado empleado(UUID id){if(id==null)return null;Empleado e=empleados.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Empleado no encontrado"));if(!e.isDisponible()||!e.getUsuario().isActivo())throw new ReglaNegocioException("El responsable no está disponible");return e;}
  private boolean cambioEstructural(EstudioTecnico e,Actualizar r){return !Objects.equals(e.getResponsable()==null?null:e.getResponsable().getId(),r.responsableId())||!Objects.equals(e.getTipo(),StringUtils.trim(r.tipo()))||!Objects.equals(e.getProveedor(),StringUtils.trimToNull(r.proveedor()))||e.isRequerido()!=r.requerido()||!Objects.equals(e.getFechaSolicitud(),r.fechaSolicitud())||!mismoDinero(e.getCosto(),r.costo());}
  private boolean mismoDinero(BigDecimal a,BigDecimal b){return a==null?b==null:b!=null&&a.compareTo(b)==0;}
  private void cambiarEstado(Proyecto p,EstadoProyecto nuevo,Usuario u,String comentario){EstadoProyecto anterior=p.getEstado();p.cambiarEstado(nuevo);historial.save(new HistorialEstado(p,u,anterior,nuevo,comentario));}
  private Respuesta dto(EstudioTecnico e){Empleado r=e.getResponsable();var u=r==null?null:r.getUsuario();return new Respuesta(e.getId(),e.getProyecto().getId(),r==null?null:r.getId(),u==null?null:u.getId(),u==null?null:u.getNombres()+" "+u.getApellidos(),e.getTipo(),e.getProveedor(),e.isRequerido(),e.getEstado(),e.getFechaSolicitud(),e.getFechaResultado(),e.getResultado(),e.getCosto());}
}
