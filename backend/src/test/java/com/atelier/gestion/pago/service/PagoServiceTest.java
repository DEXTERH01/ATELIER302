package com.atelier.gestion.pago.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
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
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class PagoServiceTest {
  @Test void adelantoAcumuladoValidadoActivaProyectoYCreaExpediente() {
    PagoRepository pagos=mock(PagoRepository.class);ProyectoRepository proyectos=mock(ProyectoRepository.class);
    EmpleadoRepository empleados=mock(EmpleadoRepository.class);ExpedienteService expedientes=mock(ExpedienteService.class);
    AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);AuditoriaService auditoria=mock(AuditoriaService.class);
    Proyecto proyecto=mock(Proyecto.class);Empleado empleado=mock(Empleado.class);Usuario usuario=mock(Usuario.class);
    UUID proyectoId=UUID.randomUUID();UUID pagoId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getEstado()).thenReturn(EstadoProyecto.PAGO_EN_REVISION);
    when(usuario.getCorreo()).thenReturn("admin@atelier.test");when(usuario.getNombres()).thenReturn("Ada");when(usuario.getApellidos()).thenReturn("Admin");
    when(empleado.getUsuario()).thenReturn(usuario);when(acceso.usuario()).thenReturn(usuario);
    Cotizacion cotizacion=new Cotizacion(proyecto,empleado,"COT-2026-0001-I01",(short)1,
        TipoCotizacion.INICIAL,new BigDecimal("50.00"));
    cotizacion.actualizar(new BigDecimal("50.00"),null,List.of(
        new DetalleCotizacion("Diseño",null,BigDecimal.ONE,new BigDecimal("1000.00"))),BigDecimal.ZERO);
    Pago pago=new Pago(cotizacion,TipoPago.ADELANTO,MetodoPago.TRANSFERENCIA,new BigDecimal("500.00"),
        "OP-1","https://res.cloudinary.com/demo/raw/upload/pago.pdf","atelier/test/pago","raw",
        "application/pdf",200,OffsetDateTime.now(ZoneOffset.UTC));
    when(pagos.bloquear(pagoId)).thenReturn(Optional.of(pago));
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    when(empleados.findByUsuarioCorreoIgnoreCase("admin@atelier.test")).thenReturn(Optional.of(empleado));
    when(pagos.totalPorTipoYEstado(isNull(),eq(TipoPago.ADELANTO),eq(EstadoPago.VALIDADO))).thenReturn(new BigDecimal("500.00"));
    PagoService service=new PagoService(pagos,mock(CotizacionRepository.class),proyectos,
        mock(HistorialEstadoRepository.class),empleados,expedientes,acceso,mock(ArchivoStorage.class),
        mock(ValidadorArchivo.class),propiedades(),auditoria,Clock.fixed(Instant.parse("2026-10-20T17:00:00Z"),ZoneOffset.UTC));

    service.validar(pagoId);

    verify(proyecto).cambiarEstado(EstadoProyecto.PROYECTO_ACTIVO);
    verify(expedientes).crearIdempotente(proyecto,usuario);
  }

  @Test void rollbackDeBaseDeDatosCompensaElArchivoCloudinary() {
    PagoRepository pagos=mock(PagoRepository.class);CotizacionRepository cotizaciones=mock(CotizacionRepository.class);
    ProyectoRepository proyectos=mock(ProyectoRepository.class);AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
    ArchivoStorage storage=mock(ArchivoStorage.class);ValidadorArchivo validador=mock(ValidadorArchivo.class);
    Proyecto proyecto=mock(Proyecto.class);Empleado empleado=mock(Empleado.class);Usuario usuario=mock(Usuario.class);
    UUID proyectoId=UUID.randomUUID();UUID cotizacionId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getCodigo()).thenReturn("PROY-2026-0001");
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.ESPERANDO_PAGO);when(acceso.usuario()).thenReturn(usuario);
    Cotizacion cotizacion=new Cotizacion(proyecto,empleado,"COT-2026-0001-I01",(short)1,TipoCotizacion.INICIAL,new BigDecimal("50.00"));
    cotizacion.responder(true,null,OffsetDateTime.now(ZoneOffset.UTC));
    when(cotizaciones.bloquear(cotizacionId)).thenReturn(Optional.of(cotizacion));when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    ArchivoAlmacenado archivo=new ArchivoAlmacenado("https://cloudinary/pago.pdf","atelier/test/pago","raw","application/pdf",2);
    when(storage.subir(any(),anyString())).thenReturn(archivo);when(pagos.save(any())).thenAnswer(i->i.getArgument(0));
    PagoService service=new PagoService(pagos,cotizaciones,proyectos,mock(HistorialEstadoRepository.class),
        mock(EmpleadoRepository.class),mock(ExpedienteService.class),acceso,storage,validador,propiedades(),
        mock(AuditoriaService.class),Clock.systemUTC());
    TransactionSynchronizationManager.initSynchronization();
    try {
      service.registrar(cotizacionId,TipoPago.ADELANTO,MetodoPago.TRANSFERENCIA,new BigDecimal("100.00"),
          "OP-1",null,new MockMultipartFile("archivo","pago.pdf","application/pdf",new byte[]{1,2}));
      TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
      verify(storage).eliminar("atelier/test/pago","raw");
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test void validarPagoAdicionalNoReiniciaElEstadoTecnicoNiCreaOtroExpediente() {
    PagoRepository pagos=mock(PagoRepository.class);ProyectoRepository proyectos=mock(ProyectoRepository.class);
    EmpleadoRepository empleados=mock(EmpleadoRepository.class);ExpedienteService expedientes=mock(ExpedienteService.class);
    AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);Proyecto proyecto=mock(Proyecto.class);
    Empleado empleado=mock(Empleado.class);Usuario usuario=mock(Usuario.class);UUID proyectoId=UUID.randomUUID();UUID pagoId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_DISENO);
    when(usuario.getCorreo()).thenReturn("admin@atelier.test");when(empleado.getUsuario()).thenReturn(usuario);when(acceso.usuario()).thenReturn(usuario);
    Cotizacion cotizacion=new Cotizacion(proyecto,empleado,"COT-2026-0001-A01",(short)1,
        TipoCotizacion.ADICIONAL,new BigDecimal("100.00"));
    cotizacion.actualizar(new BigDecimal("100.00"),null,List.of(
        new DetalleCotizacion("Cambios",null,BigDecimal.ONE,new BigDecimal("500.00"))),BigDecimal.ZERO);
    cotizacion.responder(true,null,OffsetDateTime.now(ZoneOffset.UTC));
    Pago pago=new Pago(cotizacion,TipoPago.ADICIONAL,MetodoPago.TRANSFERENCIA,new BigDecimal("500.00"),
        "OP-A","https://cloudinary/adicional.pdf","atelier/test/adicional","raw","application/pdf",100,OffsetDateTime.now(ZoneOffset.UTC));
    when(pagos.bloquear(pagoId)).thenReturn(Optional.of(pago));when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    when(empleados.findByUsuarioCorreoIgnoreCase("admin@atelier.test")).thenReturn(Optional.of(empleado));
    PagoService service=new PagoService(pagos,mock(CotizacionRepository.class),proyectos,mock(HistorialEstadoRepository.class),
        empleados,expedientes,acceso,mock(ArchivoStorage.class),mock(ValidadorArchivo.class),propiedades(),
        mock(AuditoriaService.class),Clock.systemUTC());

    service.validar(pagoId);

    verify(proyecto,never()).cambiarEstado(any());verifyNoInteractions(expedientes);
  }

  private AtelierProperties propiedades(){return new AtelierProperties(new AtelierProperties.Cors("http://localhost"),
      new AtelierProperties.Jwt("01234567890123456789012345678901","test",15,7),
      new AtelierProperties.Cookie("REFRESH",false),new AtelierProperties.Cloudinary("demo","key","secret"),
      new AtelierProperties.Files(10_485_760),"test");}
}
