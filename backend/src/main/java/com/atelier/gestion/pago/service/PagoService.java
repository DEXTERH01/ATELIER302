package com.atelier.gestion.pago.service;

import static com.atelier.gestion.pago.dto.PagoDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.common.storage.*;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.cotizacion.entity.*;
import com.atelier.gestion.cotizacion.repository.CotizacionRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.expediente.service.ExpedienteService;
import com.atelier.gestion.pago.entity.*;
import com.atelier.gestion.pago.repository.PagoRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.math.*;
import java.time.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PagoService {
  private final PagoRepository pagos;private final CotizacionRepository cotizaciones;private final ProyectoRepository proyectos;private final HistorialEstadoRepository historial;private final EmpleadoRepository empleados;private final ExpedienteService expedientes;private final AutorizacionProyectoService acceso;private final ArchivoStorage storage;private final ValidadorArchivo validador;private final AtelierProperties propiedades;private final AuditoriaService auditoria;private final Clock clock;
  public PagoService(PagoRepository p,CotizacionRepository c,ProyectoRepository pr,HistorialEstadoRepository h,EmpleadoRepository e,ExpedienteService ex,AutorizacionProyectoService a,ArchivoStorage s,ValidadorArchivo v,AtelierProperties props,AuditoriaService au,Clock clock){pagos=p;cotizaciones=c;proyectos=pr;historial=h;empleados=e;expedientes=ex;acceso=a;storage=s;validador=v;propiedades=props;auditoria=au;this.clock=clock;}
  @Transactional(readOnly=true) public List<Respuesta> listar(UUID cotizacionId){Cotizacion c=cotizaciones.findDetailedById(cotizacionId).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));acceso.verificarVisible(c.getProyecto(),acceso.usuario());return pagos.findByCotizacionIdOrderByPagadoEnDesc(cotizacionId).stream().map(this::dto).toList();}
  @Transactional(readOnly=true) public List<Respuesta> listarPendientes(){Usuario u=acceso.usuario();acceso.exigirAdministrador(u);return pagos.findByEstadoOrderByPagadoEnAsc(EstadoPago.PENDIENTE).stream().map(this::dto).toList();}

  @Transactional public Respuesta registrar(UUID cotizacionId,TipoPago tipo,MetodoPago metodo,BigDecimal monto,String numeroOperacion,OffsetDateTime pagadoEn,MultipartFile archivo){Usuario u=acceso.usuario();Cotizacion c=cotizaciones.bloquear(cotizacionId).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));acceso.exigirClientePropietario(c.getProyecto(),u);if(c.getEstado()!=EstadoCotizacion.ACEPTADA)throw new ReglaNegocioException("Solo se registran pagos para una cotización aceptada");Proyecto p=proyectos.bloquear(c.getProyecto().getId()).orElseThrow();if(p.getEstado()!=EstadoProyecto.ESPERANDO_PAGO&&p.getEstado()!=EstadoProyecto.PAGO_EN_REVISION)throw new ReglaNegocioException("El proyecto no está esperando un pago");if(monto==null||monto.signum()<=0)throw new ReglaNegocioException("El monto debe ser mayor que cero");validador.validar(archivo);String carpeta="atelier/%s/proyectos/%s/pagos".formatted(propiedades.environment(),p.getCodigo());ArchivoAlmacenado subido=storage.subir(archivo,carpeta);registrarCompensacion(subido);Pago pago=pagos.save(new Pago(c,tipo,metodo,monto.setScale(2,RoundingMode.HALF_UP),StringUtils.trimToNull(numeroOperacion),subido.secureUrl(),subido.publicId(),subido.resourceType(),subido.tipoMime(),subido.tamanoBytes(),pagadoEn==null?OffsetDateTime.now(clock):pagadoEn));if(p.getEstado()==EstadoProyecto.ESPERANDO_PAGO)cambiarEstado(p,EstadoProyecto.PAGO_EN_REVISION,u,"Comprobante de pago registrado: "+tipo);auditoria.registrar(u,"pagos",pago.getId(),"INSERT",Map.of("monto",pago.getMonto(),"publicId",subido.publicId()));return dto(pago);}

  @Transactional public Respuesta validar(UUID id){Usuario u=acceso.usuario();acceso.exigirAdministrador(u);Pago pago=pagos.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Pago no encontrado"));if(pago.getEstado()!=EstadoPago.PENDIENTE)throw new ReglaNegocioException("El pago ya fue revisado");Proyecto p=proyectos.bloquear(pago.getCotizacion().getProyecto().getId()).orElseThrow();Empleado validador=empleadoActual(u);pago.validar(validador,OffsetDateTime.now(clock));pagos.flush();BigDecimal total=pagos.totalPorTipoYEstado(pago.getCotizacion().getId(),TipoPago.ADELANTO,EstadoPago.VALIDADO);if(total.compareTo(pago.getCotizacion().adelantoRequerido())>=0){if(p.getEstado()!=EstadoProyecto.PROYECTO_ACTIVO)cambiarEstado(p,EstadoProyecto.PROYECTO_ACTIVO,u,"Adelanto requerido validado");expedientes.crearIdempotente(p,u);}else if(p.getEstado()==EstadoProyecto.PAGO_EN_REVISION&&!pagos.existsByCotizacionProyectoIdAndEstado(p.getId(),EstadoPago.PENDIENTE)){cambiarEstado(p,EstadoProyecto.ESPERANDO_PAGO,u,"Pago validado; adelanto requerido aún incompleto");}auditoria.registrar(u,"pagos",id,"UPDATE",Map.of("operacion","VALIDACION"));return dto(pago);}

  @Transactional public Respuesta rechazar(UUID id,Rechazar req){Usuario u=acceso.usuario();acceso.exigirAdministrador(u);Pago pago=pagos.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Pago no encontrado"));if(pago.getEstado()!=EstadoPago.PENDIENTE)throw new ReglaNegocioException("El pago ya fue revisado");Proyecto p=proyectos.bloquear(pago.getCotizacion().getProyecto().getId()).orElseThrow();pago.rechazar(empleadoActual(u),StringUtils.trim(req.motivo()),OffsetDateTime.now(clock));if(p.getEstado()==EstadoProyecto.PAGO_EN_REVISION&&!pagos.existsByCotizacionProyectoIdAndEstado(p.getId(),EstadoPago.PENDIENTE))cambiarEstado(p,EstadoProyecto.ESPERANDO_PAGO,u,"Pago rechazado: "+req.motivo());auditoria.registrar(u,"pagos",id,"UPDATE",Map.of("operacion","RECHAZO"));return dto(pago);}

  private void registrarCompensacion(ArchivoAlmacenado archivo){if(!TransactionSynchronizationManager.isSynchronizationActive())throw new IllegalStateException("La carga requiere una transacción activa");TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)storage.eliminar(archivo.publicId(),archivo.resourceType());}});}
  private Empleado empleadoActual(Usuario u){return empleados.findByUsuarioCorreoIgnoreCase(u.getCorreo()).orElseThrow(()->new ReglaNegocioException("El administrador requiere un perfil de empleado"));}
  private void cambiarEstado(Proyecto p,EstadoProyecto nuevo,Usuario u,String comentario){EstadoProyecto anterior=p.getEstado();p.cambiarEstado(nuevo);historial.save(new HistorialEstado(p,u,anterior,nuevo,comentario));}
  private Respuesta dto(Pago p){Empleado v=p.getValidadoPor();return new Respuesta(p.getId(),p.getCotizacion().getId(),p.getTipo(),p.getMetodo(),p.getMonto(),p.getMoneda(),p.getNumeroOperacion(),p.getComprobanteUrl(),p.getComprobantePublicId(),p.getComprobanteTipoMime(),p.getComprobanteTamanoBytes(),p.getComprobanteCategoria(),p.getEstado(),p.getMotivoRechazo(),v==null?null:v.getId(),v==null?null:v.getUsuario().getNombres()+" "+v.getUsuario().getApellidos(),p.getPagadoEn(),p.getValidadoEn());}
}
