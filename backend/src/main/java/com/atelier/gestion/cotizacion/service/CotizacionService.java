package com.atelier.gestion.cotizacion.service;

import static com.atelier.gestion.cotizacion.dto.CotizacionDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.cotizacion.entity.*;
import com.atelier.gestion.cotizacion.repository.CotizacionRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
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

@Service
public class CotizacionService {
  private final CotizacionRepository cotizaciones;private final ProyectoRepository proyectos;private final HistorialEstadoRepository historial;
  private final EmpleadoRepository empleados;private final AutorizacionProyectoService acceso;private final AuditoriaService auditoria;private final Clock clock;
  public CotizacionService(CotizacionRepository c,ProyectoRepository p,HistorialEstadoRepository h,EmpleadoRepository e,AutorizacionProyectoService a,AuditoriaService au,Clock clock){cotizaciones=c;proyectos=p;historial=h;empleados=e;acceso=a;auditoria=au;this.clock=clock;}

  @Transactional(readOnly=true) public List<Respuesta> listar(UUID proyectoId){acceso.buscarVisible(proyectoId);return cotizaciones.findByProyectoIdOrderByVersionDesc(proyectoId).stream().map(this::dto).toList();}
  @Transactional(readOnly=true) public Respuesta obtener(UUID id){Cotizacion c=buscar(id);acceso.verificarVisible(c.getProyecto(),acceso.usuario());return dto(c);}

  @Transactional public Respuesta crear(UUID proyectoId,Guardar req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Proyecto p=proyectos.bloquear(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));if(p.getEstado()!=EstadoProyecto.EN_COTIZACION)throw new ReglaNegocioException("El proyecto debe estar EN_COTIZACION");Empleado creador=empleadoActual(u);short version=(short)(cotizaciones.maxVersion(proyectoId,req.tipo())+1);Cotizacion c=new Cotizacion(p,creador,numero(p,req.tipo(),version),version,req.tipo(),dinero(req.porcentajeAdelanto()));aplicar(c,req);cotizaciones.save(c);auditoria.registrar(u,"cotizaciones",c.getId(),"INSERT",Map.of("numero",c.getNumero()));return dto(c);}

  @Transactional public Respuesta actualizar(UUID id,Guardar req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Cotizacion c=cotizaciones.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));if(c.getEstado()!=EstadoCotizacion.BORRADOR)throw new ReglaNegocioException("Solo puede editarse una cotización en borrador");if(c.getTipo()!=req.tipo())throw new ReglaNegocioException("El tipo de cotización no puede modificarse");aplicar(c,req);auditoria.registrar(u,"cotizaciones",id,"UPDATE",Map.of("operacion","EDICION"));return dto(c);}

  @Transactional public Respuesta enviar(UUID id){Usuario u=acceso.usuario();acceso.exigirGestor(u);Cotizacion c=cotizaciones.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));Proyecto p=proyectos.bloquear(c.getProyecto().getId()).orElseThrow();if(c.getEstado()!=EstadoCotizacion.BORRADOR)throw new ReglaNegocioException("La cotización ya fue enviada");if(p.getEstado()!=EstadoProyecto.EN_COTIZACION)throw new ReglaNegocioException("El proyecto no está listo para enviar cotización");OffsetDateTime ahora=OffsetDateTime.now(clock);c.enviar(ahora);cambiarEstado(p,EstadoProyecto.COTIZACION_ENVIADA,u,"Cotización "+c.getNumero()+" enviada; vence en 10 días");auditoria.registrar(u,"cotizaciones",id,"UPDATE",Map.of("operacion","ENVIO"));return dto(c);}

  @Transactional public Respuesta responder(UUID id,Responder req){Usuario u=acceso.usuario();Cotizacion c=cotizaciones.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));acceso.exigirClientePropietario(c.getProyecto(),u);Proyecto p=proyectos.bloquear(c.getProyecto().getId()).orElseThrow();if(c.getEstado()!=EstadoCotizacion.ENVIADA)throw new ReglaNegocioException("La cotización no está pendiente de respuesta");OffsetDateTime ahora=OffsetDateTime.now(clock);if(c.getVenceEn()==null||!ahora.isBefore(c.getVenceEn()))throw new ReglaNegocioException("La cotización venció y debe renovarse");if(!req.aceptar()&&StringUtils.isBlank(req.observacion()))throw new ReglaNegocioException("El rechazo exige una observación");c.responder(req.aceptar(),StringUtils.trimToNull(req.observacion()),ahora);cambiarEstado(p,req.aceptar()?EstadoProyecto.ESPERANDO_PAGO:EstadoProyecto.EN_COTIZACION,u,req.aceptar()?"Cotización aceptada":"Cotización rechazada: "+req.observacion());auditoria.registrar(u,"cotizaciones",id,"UPDATE",Map.of("operacion",req.aceptar()?"ACEPTACION":"RECHAZO"));return dto(c);}

  @Transactional public Respuesta renovar(UUID id){Usuario u=acceso.usuario();acceso.exigirGestor(u);Cotizacion anterior=cotizaciones.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));OffsetDateTime ahora=OffsetDateTime.now(clock);if(anterior.getEstado()!=EstadoCotizacion.ENVIADA||anterior.getVenceEn()==null||ahora.isBefore(anterior.getVenceEn()))throw new ReglaNegocioException("Solo puede renovarse una cotización enviada y vencida");Proyecto p=proyectos.bloquear(anterior.getProyecto().getId()).orElseThrow();anterior.vencer();short version=(short)(cotizaciones.maxVersion(p.getId(),anterior.getTipo())+1);Cotizacion nueva=new Cotizacion(p,empleadoActual(u),numero(p,anterior.getTipo(),version),version,anterior.getTipo(),anterior.getPorcentajeAdelanto());List<DetalleCotizacion> detalles=anterior.getDetalles().stream().map(d->new DetalleCotizacion(d.getConcepto(),d.getDescripcion(),d.getCantidad(),d.getPrecioUnitario())).toList();BigDecimal porcentaje=anterior.getSubtotal().signum()==0?BigDecimal.ZERO:anterior.getImpuesto().multiply(BigDecimal.valueOf(100)).divide(anterior.getSubtotal(),2,RoundingMode.HALF_UP);nueva.actualizar(anterior.getPorcentajeAdelanto(),anterior.getObservacion(),detalles,porcentaje);cotizaciones.save(nueva);if(p.getEstado()==EstadoProyecto.COTIZACION_ENVIADA)cambiarEstado(p,EstadoProyecto.EN_COTIZACION,u,"Cotización vencida; creada renovación "+nueva.getNumero());auditoria.registrar(u,"cotizaciones",nueva.getId(),"INSERT",Map.of("renueva",anterior.getId().toString()));return dto(nueva);}

  private Cotizacion buscar(UUID id){return cotizaciones.findDetailedById(id).orElseThrow(()->new RecursoNoEncontradoException("Cotización no encontrada"));}
  private Empleado empleadoActual(Usuario u){return empleados.findByUsuarioCorreoIgnoreCase(u.getCorreo()).orElseThrow(()->new ReglaNegocioException("El usuario gestor requiere un perfil de empleado"));}
  private void aplicar(Cotizacion c,Guardar req){List<DetalleCotizacion> detalles=req.detalles().stream().map(d->new DetalleCotizacion(StringUtils.trim(d.concepto()),StringUtils.trimToNull(d.descripcion()),dinero(d.cantidad()),dinero(d.precioUnitario()))).toList();c.actualizar(dinero(req.porcentajeAdelanto()),StringUtils.trimToNull(req.observacion()),detalles,dinero(req.porcentajeImpuesto()));}
  private BigDecimal dinero(BigDecimal v){return v.setScale(2,RoundingMode.HALF_UP);}
  private String numero(Proyecto p,TipoCotizacion tipo,short version){return "COT-"+p.getCodigo().substring(5)+"-"+(tipo==TipoCotizacion.INICIAL?"I":"A")+"%02d".formatted(version);}
  private void cambiarEstado(Proyecto p,EstadoProyecto nuevo,Usuario u,String comentario){EstadoProyecto anterior=p.getEstado();p.cambiarEstado(nuevo);historial.save(new HistorialEstado(p,u,anterior,nuevo,comentario));}
  private Respuesta dto(Cotizacion c){return new Respuesta(c.getId(),c.getProyecto().getId(),c.getNumero(),c.getVersion(),c.getTipo(),c.getSubtotal(),c.getImpuesto(),c.getTotal(),c.getPorcentajeAdelanto(),c.adelantoRequerido(),c.getEstado(),c.getEmitidaEn(),c.getVenceEn(),c.getRespondidaEn(),c.getObservacion(),c.getDetalles().stream().map(d->new DetalleRespuesta(d.getId(),d.getConcepto(),d.getDescripcion(),d.getCantidad(),d.getPrecioUnitario(),d.getImporte())).toList());}
}
