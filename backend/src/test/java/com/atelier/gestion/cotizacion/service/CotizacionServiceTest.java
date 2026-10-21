package com.atelier.gestion.cotizacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cotizacion.entity.*;
import com.atelier.gestion.cotizacion.repository.CotizacionRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.propuesta.repository.ObservacionClienteRepository;
import com.atelier.gestion.propuesta.entity.*;
import com.atelier.gestion.cotizacion.dto.CotizacionDtos.*;
import com.atelier.gestion.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CotizacionServiceTest {
  @Test void renovarConservaVersionAnteriorYCreaNuevoBorrador() {
    CotizacionRepository repo=mock(CotizacionRepository.class);ProyectoRepository proyectos=mock(ProyectoRepository.class);
    EmpleadoRepository empleados=mock(EmpleadoRepository.class);AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
    Proyecto proyecto=mock(Proyecto.class);Empleado empleado=mock(Empleado.class);Usuario usuario=mock(Usuario.class);
    UUID proyectoId=UUID.randomUUID();UUID anteriorId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getCodigo()).thenReturn("PROY-2026-0001");
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.COTIZACION_ENVIADA);when(usuario.getCorreo()).thenReturn("gestor@atelier.test");
    Cotizacion anterior=new Cotizacion(proyecto,empleado,"COT-2026-0001-I01",(short)1,
        TipoCotizacion.INICIAL,new BigDecimal("50.00"));
    ReflectionTestUtils.setField(anterior,"id",anteriorId);
    anterior.actualizar(new BigDecimal("50.00"),"Vigencia inicial",List.of(
        new DetalleCotizacion("Diseño",null,BigDecimal.ONE,new BigDecimal("1000.00"))),new BigDecimal("18.00"));
    anterior.enviar(OffsetDateTime.parse("2026-10-01T12:00:00Z"));
    when(acceso.usuario()).thenReturn(usuario);when(repo.bloquear(anteriorId)).thenReturn(Optional.of(anterior));
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));when(repo.maxVersion(proyectoId,TipoCotizacion.INICIAL)).thenReturn((short)1);
    when(empleados.findByUsuarioCorreoIgnoreCase("gestor@atelier.test")).thenReturn(Optional.of(empleado));
    when(repo.save(any())).thenAnswer(i->i.getArgument(0));
    CotizacionService service=new CotizacionService(repo,proyectos,mock(HistorialEstadoRepository.class),
        empleados,acceso,mock(ObservacionClienteRepository.class),mock(AuditoriaService.class),
        Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"),ZoneOffset.UTC));

    var nueva=service.renovar(anteriorId);

    assertThat(anterior.getEstado()).isEqualTo(EstadoCotizacion.VENCIDA);
    assertThat(nueva.version()).isEqualTo((short)2);assertThat(nueva.estado()).isEqualTo(EstadoCotizacion.BORRADOR);
    assertThat(nueva.total()).isEqualByComparingTo(anterior.getTotal());
    verify(proyecto).cambiarEstado(EstadoProyecto.EN_COTIZACION);
  }

  @Test void terceraRondaMarcadaGeneraCotizacionAdicionalVinculada() {
    CotizacionRepository repo=mock(CotizacionRepository.class);ProyectoRepository proyectos=mock(ProyectoRepository.class);
    EmpleadoRepository empleados=mock(EmpleadoRepository.class);AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
    ObservacionClienteRepository observaciones=mock(ObservacionClienteRepository.class);
    Proyecto proyecto=mock(Proyecto.class);Empleado empleado=mock(Empleado.class);Usuario usuario=mock(Usuario.class);
    Propuesta propuesta=mock(Propuesta.class);ObservacionCliente observacion=mock(ObservacionCliente.class);
    UUID proyectoId=UUID.randomUUID();UUID observacionId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getCodigo()).thenReturn("PROY-2026-0001");
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_DISENO);when(usuario.getCorreo()).thenReturn("gestor@atelier.test");
    when(propuesta.getProyecto()).thenReturn(proyecto);when(observacion.getPropuesta()).thenReturn(propuesta);
    when(observacion.getNumeroRonda()).thenReturn((short)3);when(observacion.isAdicional()).thenReturn(true);
    when(observacion.getEstado()).thenReturn(EstadoObservacionCliente.PENDIENTE);
    when(observaciones.bloquear(observacionId)).thenReturn(Optional.of(observacion));
    when(acceso.usuario()).thenReturn(usuario);when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    when(empleados.findByUsuarioCorreoIgnoreCase("gestor@atelier.test")).thenReturn(Optional.of(empleado));
    when(repo.maxVersion(proyectoId,TipoCotizacion.ADICIONAL)).thenReturn((short)0);
    when(repo.save(any())).thenAnswer(i->i.getArgument(0));
    CotizacionService service=new CotizacionService(repo,proyectos,mock(HistorialEstadoRepository.class),
        empleados,acceso,observaciones,mock(AuditoriaService.class),Clock.systemUTC());
    Guardar req=new Guardar(TipoCotizacion.ADICIONAL,new BigDecimal("18.00"),new BigDecimal("100.00"),
        "Tercera ronda",observacionId,List.of(new DetalleEntrada("Cambios",null,BigDecimal.ONE,new BigDecimal("500.00"))));

    var creada=service.crear(proyectoId,req);

    assertThat(creada.tipo()).isEqualTo(TipoCotizacion.ADICIONAL);
    verify(observacion).vincularCotizacion(any(Cotizacion.class));
    verify(proyecto,never()).cambiarEstado(any());
  }
}
