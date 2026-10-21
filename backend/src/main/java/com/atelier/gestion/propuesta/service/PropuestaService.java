package com.atelier.gestion.propuesta.service;

import static com.atelier.gestion.propuesta.dto.PropuestaDtos.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.common.storage.*;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.cotizacion.entity.*;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.pago.entity.*;
import com.atelier.gestion.pago.repository.PagoRepository;
import com.atelier.gestion.propuesta.entity.*;
import com.atelier.gestion.propuesta.repository.*;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PropuestaService {
  private final PropuestaRepository propuestas;private final ObservacionClienteRepository observaciones;
  private final ProyectoRepository proyectos;private final EmpleadoRepository empleados;private final PagoRepository pagos;
  private final AutorizacionProyectoService acceso;private final ArchivoStorage storage;private final ValidadorArchivo validador;
  private final AtelierProperties propiedades;private final HistorialEstadoRepository historial;
  private final AuditoriaService auditoria;private final Clock clock;

  public PropuestaService(PropuestaRepository propuestas,ObservacionClienteRepository observaciones,
      ProyectoRepository proyectos,EmpleadoRepository empleados,PagoRepository pagos,
      AutorizacionProyectoService acceso,ArchivoStorage storage,ValidadorArchivo validador,
      AtelierProperties propiedades,HistorialEstadoRepository historial,AuditoriaService auditoria,Clock clock){
    this.propuestas=propuestas;this.observaciones=observaciones;this.proyectos=proyectos;
    this.empleados=empleados;this.pagos=pagos;this.acceso=acceso;this.storage=storage;
    this.validador=validador;this.propiedades=propiedades;this.historial=historial;
    this.auditoria=auditoria;this.clock=clock;
  }

  @Transactional(readOnly=true)
  public List<PropuestaRespuesta> listar(UUID proyectoId){
    Proyecto proyecto=acceso.buscarVisible(proyectoId);Usuario usuario=acceso.usuario();
    return propuestas.findByProyectoIdOrderByCreadoEnDesc(proyectoId).stream()
        .filter(p->puedeVerBorrador(p,proyecto,usuario)).map(this::dto).toList();
  }

  @Transactional(readOnly=true)
  public PropuestaRespuesta obtener(UUID id){
    Propuesta p=buscar(id);Usuario u=acceso.usuario();acceso.verificarVisible(p.getProyecto(),u);
    if(p.getEstado()==EstadoPropuesta.BORRADOR&&!puedeVerBorrador(p,p.getProyecto(),u))
      throw new AccessDeniedException("El borrador no está disponible para el cliente");
    return dto(p);
  }

  @Transactional
  public PropuestaRespuesta crear(UUID proyectoId,TipoPropuesta tipo,String descripcion,MultipartFile archivo){
    Usuario u=acceso.usuario();Proyecto p=proyectos.bloquear(proyectoId)
        .orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));
    exigirCreador(p,u);if(p.getEstado()!=EstadoProyecto.EN_DISENO)
      throw new ReglaNegocioException("Las propuestas solo se crean durante el diseño");
    exigirAdicionalesPagados(p.getId(),tipo);
    validador.validar(archivo);String nombre=sanitizar(archivo.getOriginalFilename());
    String carpeta="atelier/%s/proyectos/%s/propuestas".formatted(propiedades.environment(),p.getCodigo());
    ArchivoAlmacenado subido=storage.subir(archivo,carpeta);compensar(subido);
    short version=(short)(propuestas.maxVersion(proyectoId,tipo)+1);
    Propuesta propuesta=propuestas.save(new Propuesta(p,empleadoActual(u),version,tipo,
        StringUtils.trimToNull(descripcion),subido.secureUrl(),subido.publicId(),nombre,
        subido.tipoMime(),subido.tamanoBytes(),subido.resourceType(),OffsetDateTime.now(clock)));
    auditoria.registrar(u,"propuestas",propuesta.getId(),"INSERT",Map.of("version",version,"tipo",tipo.name(),"publicId",subido.publicId()));
    return dto(propuesta);
  }

  @Transactional
  public PropuestaRespuesta enviar(UUID id){
    Usuario u=acceso.usuario();acceso.exigirGestor(u);Propuesta propuesta=propuestas.bloquear(id)
        .orElseThrow(()->new RecursoNoEncontradoException("Propuesta no encontrada"));
    Proyecto p=proyectos.bloquear(propuesta.getProyecto().getId()).orElseThrow();
    if(propuesta.getEstado()!=EstadoPropuesta.BORRADOR)throw new ReglaNegocioException("Solo se envía una propuesta en borrador");
    if(p.getEstado()!=EstadoProyecto.EN_DISENO)throw new ReglaNegocioException("El proyecto no está listo para revisión del cliente");
    if(propuestas.existsByProyectoIdAndTipoAndEstado(p.getId(),propuesta.getTipo(),EstadoPropuesta.ENVIADA))
      throw new ConflictoException("Ya existe una propuesta de este tipo pendiente de respuesta");
    exigirAdicionalesPagados(p.getId(),propuesta.getTipo());
    propuestas.findByProyectoIdAndTipoAndEstado(p.getId(),propuesta.getTipo(),EstadoPropuesta.OBSERVADA)
        .forEach(Propuesta::reemplazar);
    propuesta.enviar(OffsetDateTime.now(clock));
    cambiarEstado(p,EstadoProyecto.REVISION_CLIENTE,u,"Propuesta "+propuesta.getTipo()+" v"+propuesta.getNumeroVersion()+" enviada");
    auditoria.registrar(u,"propuestas",id,"UPDATE",Map.of("operacion","ENVIO"));return dto(propuesta);
  }

  @Transactional
  public PropuestaRespuesta responder(UUID id,Responder req){
    if(req.conforme())return darConformidad(id);
    if(StringUtils.isBlank(req.observacion()))throw new ReglaNegocioException("Registrar observaciones exige una descripción");
    registrarObservacion(id,req.observacion());return dto(buscar(id));
  }

  @Transactional
  public ObservacionRespuesta registrarObservacion(UUID propuestaId,String descripcion){
    Usuario u=acceso.usuario();Propuesta propuesta=propuestas.bloquear(propuestaId)
        .orElseThrow(()->new RecursoNoEncontradoException("Propuesta no encontrada"));
    acceso.exigirClientePropietario(propuesta.getProyecto(),u);Proyecto p=proyectos.bloquear(propuesta.getProyecto().getId()).orElseThrow();
    if(propuesta.getEstado()!=EstadoPropuesta.ENVIADA||p.getEstado()!=EstadoProyecto.REVISION_CLIENTE)
      throw new ReglaNegocioException("La propuesta no está pendiente de respuesta");
    String texto=StringUtils.trim(descripcion);if(StringUtils.isBlank(texto)||texto.length()>5000)
      throw new ReglaNegocioException("La observación es obligatoria y admite hasta 5000 caracteres");
    short ronda=(short)(observaciones.maxRonda(p.getId(),propuesta.getTipo())+1);OffsetDateTime ahora=OffsetDateTime.now(clock);
    ObservacionCliente o=observaciones.save(new ObservacionCliente(propuesta,p.getCliente(),ronda,texto,ahora));
    propuesta.observar(ahora);cambiarEstado(p,EstadoProyecto.EN_DISENO,u,"Observaciones del cliente, ronda "+ronda);
    auditoria.registrar(u,"observaciones_cliente",o.getId(),"INSERT",Map.of("ronda",ronda,"requiereEvaluacionAdicional",ronda>2));
    return dto(o);
  }

  @Transactional(readOnly=true)
  public List<ObservacionRespuesta> listarObservaciones(UUID propuestaId){
    Propuesta p=buscar(propuestaId);acceso.verificarVisible(p.getProyecto(),acceso.usuario());
    return observaciones.findByPropuestaIdOrderByNumeroRondaAsc(propuestaId).stream().map(this::dto).toList();
  }

  @Transactional
  public ObservacionRespuesta actualizarObservacion(UUID id,ActualizarObservacion req){
    Usuario u=acceso.usuario();acceso.exigirGestor(u);ObservacionCliente o=observaciones.bloquear(id)
        .orElseThrow(()->new RecursoNoEncontradoException("Observación no encontrada"));
    if(o.getEstado()!=EstadoObservacionCliente.PENDIENTE&&req.estado()!=o.getEstado())
      throw new ReglaNegocioException("Una observación cerrada no puede cambiar de estado");
    if(req.adicional()&&o.getNumeroRonda()<=2)
      throw new ReglaNegocioException("Las dos primeras rondas están incluidas y no pueden marcarse como adicionales");
    if(!req.adicional()&&o.getCotizacionAdicional()!=null)
      throw new ReglaNegocioException("La observación ya tiene una cotización adicional vinculada");
    if(req.estado()==EstadoObservacionCliente.ATENDIDA&&req.adicional())exigirPago(o);
    o.actualizar(req.estado(),req.adicional(),OffsetDateTime.now(clock));
    auditoria.registrar(u,"observaciones_cliente",id,"UPDATE",Map.of("estado",req.estado().name(),"adicional",req.adicional()));
    return dto(o);
  }

  private PropuestaRespuesta darConformidad(UUID id){
    Usuario u=acceso.usuario();Propuesta propuesta=propuestas.bloquear(id)
        .orElseThrow(()->new RecursoNoEncontradoException("Propuesta no encontrada"));
    acceso.exigirClientePropietario(propuesta.getProyecto(),u);Proyecto p=proyectos.bloquear(propuesta.getProyecto().getId()).orElseThrow();
    if(propuesta.getEstado()!=EstadoPropuesta.ENVIADA||p.getEstado()!=EstadoProyecto.REVISION_CLIENTE)
      throw new ReglaNegocioException("La propuesta no está pendiente de conformidad");
    propuesta.darConformidad(OffsetDateTime.now(clock));
    EstadoProyecto destino=propuesta.getTipo()==TipoPropuesta.PROYECTO_FINAL?EstadoProyecto.EXPEDIENTE_FINAL:EstadoProyecto.EN_DISENO;
    cambiarEstado(p,destino,u,"Conformidad del cliente para "+propuesta.getTipo()+" v"+propuesta.getNumeroVersion());
    auditoria.registrar(u,"propuestas",id,"UPDATE",Map.of("operacion","CONFORMIDAD"));return dto(propuesta);
  }

  private void exigirAdicionalesPagados(UUID proyectoId,TipoPropuesta tipo){
    observaciones.findByPropuestaProyectoIdAndPropuestaTipoAndEstado(proyectoId,tipo,EstadoObservacionCliente.PENDIENTE)
        .stream().filter(ObservacionCliente::isAdicional).forEach(this::exigirPago);
  }

  private void exigirPago(ObservacionCliente o){
    Cotizacion c=o.getCotizacionAdicional();if(c==null||c.getEstado()!=EstadoCotizacion.ACEPTADA)
      throw new ReglaNegocioException("La ronda adicional requiere una cotización aceptada");
    BigDecimal pagado=pagos.totalPorTipoYEstado(c.getId(),TipoPago.ADICIONAL,EstadoPago.VALIDADO);
    if(pagado.compareTo(c.adelantoRequerido())<0)
      throw new ReglaNegocioException("La ronda adicional requiere el pago validado de la cotización");
  }

  private boolean puedeVerBorrador(Propuesta p,Proyecto proyecto,Usuario u){
    if(p.getEstado()!=EstadoPropuesta.BORRADOR)return true;
    return acceso.esGestor(u)||("ESPECIALISTA".equals(u.getRol().getNombre())&&proyectos.asignadoA(proyecto.getId(),u.getCorreo()));
  }
  private void exigirCreador(Proyecto p,Usuario u){
    if(acceso.esGestor(u))return;
    if("ESPECIALISTA".equals(u.getRol().getNombre())&&proyectos.asignadoA(p.getId(),u.getCorreo()))return;
    throw new AccessDeniedException("Solo un gestor o especialista asignado puede crear propuestas");
  }
  private Empleado empleadoActual(Usuario u){return empleados.findByUsuarioCorreoIgnoreCase(u.getCorreo())
      .orElseThrow(()->new ReglaNegocioException("El creador requiere un perfil de empleado"));}
  private Propuesta buscar(UUID id){return propuestas.findById(id)
      .orElseThrow(()->new RecursoNoEncontradoException("Propuesta no encontrada"));}
  private void cambiarEstado(Proyecto p,EstadoProyecto nuevo,Usuario u,String comentario){EstadoProyecto anterior=p.getEstado();p.cambiarEstado(nuevo);historial.save(new HistorialEstado(p,u,anterior,nuevo,comentario));}
  private String sanitizar(String original){String nombre=StringUtils.trim(FilenameUtils.getName(StringUtils.defaultIfBlank(original,"propuesta"))).replaceAll("[\\p{Cntrl}]","");return StringUtils.left(StringUtils.defaultIfBlank(nombre,"propuesta"),200);}
  private void compensar(ArchivoAlmacenado a){if(!TransactionSynchronizationManager.isSynchronizationActive())throw new IllegalStateException("La carga requiere una transacción activa");TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)storage.eliminar(a.publicId(),a.resourceType());}});}
  private PropuestaRespuesta dto(Propuesta p){Empleado e=p.getCreadaPor();Usuario u=e.getUsuario();return new PropuestaRespuesta(p.getId(),p.getProyecto().getId(),e.getId(),u.getNombres()+" "+u.getApellidos(),p.getNumeroVersion(),p.getTipo(),p.getDescripcion(),p.getUrlVistaPrevia(),p.getVistaPreviaPublicId(),p.getVistaPreviaNombre(),p.getVistaPreviaTipoMime(),p.getVistaPreviaTamanoBytes(),p.getVistaPreviaResourceType(),p.getEstado(),p.getEnviadaEn(),p.getRespondidaEn(),p.getCreadoEn());}
  private ObservacionRespuesta dto(ObservacionCliente o){Cotizacion c=o.getCotizacionAdicional();return new ObservacionRespuesta(o.getId(),o.getPropuesta().getId(),o.getCliente().getId(),o.getNumeroRonda(),o.getDescripcion(),o.getEstado(),o.isAdicional(),o.getNumeroRonda()>2,c==null?null:c.getId(),o.getCreadaEn(),o.getAtendidaEn());}
}
