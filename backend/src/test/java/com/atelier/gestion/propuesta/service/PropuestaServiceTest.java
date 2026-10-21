package com.atelier.gestion.propuesta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.common.storage.*;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.pago.repository.PagoRepository;
import com.atelier.gestion.propuesta.dto.PropuestaDtos.*;
import com.atelier.gestion.propuesta.entity.*;
import com.atelier.gestion.propuesta.repository.*;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.*;

class PropuestaServiceTest {
  private final PropuestaRepository propuestas=mock(PropuestaRepository.class);
  private final ObservacionClienteRepository observaciones=mock(ObservacionClienteRepository.class);
  private final ProyectoRepository proyectos=mock(ProyectoRepository.class);
  private final EmpleadoRepository empleados=mock(EmpleadoRepository.class);
  private final PagoRepository pagos=mock(PagoRepository.class);
  private final AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
  private final ArchivoStorage storage=mock(ArchivoStorage.class);
  private final AuditoriaService auditoria=mock(AuditoriaService.class);
  private final HistorialEstadoRepository historial=mock(HistorialEstadoRepository.class);
  private PropuestaService service;private Proyecto proyecto;private Propuesta propuesta;
  private Cliente cliente;private Usuario usuario;private UUID proyectoId;private UUID propuestaId;

  @BeforeEach void setUp(){
    service=new PropuestaService(propuestas,observaciones,proyectos,empleados,pagos,acceso,storage,
        mock(ValidadorArchivo.class),propiedades(),historial,auditoria,
        Clock.fixed(Instant.parse("2026-10-20T17:00:00Z"),ZoneOffset.UTC));
    proyecto=mock(Proyecto.class);propuesta=mock(Propuesta.class);cliente=mock(Cliente.class);usuario=mock(Usuario.class);
    Empleado creador=mock(Empleado.class);Usuario autor=mock(Usuario.class);
    proyectoId=UUID.randomUUID();propuestaId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getCliente()).thenReturn(cliente);
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.REVISION_CLIENTE);
    when(propuesta.getId()).thenReturn(propuestaId);when(propuesta.getProyecto()).thenReturn(proyecto);
    when(propuesta.getCreadaPor()).thenReturn(creador);when(creador.getUsuario()).thenReturn(autor);
    when(propuesta.getTipo()).thenReturn(TipoPropuesta.ANTEPROYECTO);when(propuesta.getEstado()).thenReturn(EstadoPropuesta.ENVIADA);
    when(acceso.usuario()).thenReturn(usuario);when(propuestas.bloquear(propuestaId)).thenReturn(Optional.of(propuesta));
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    when(observaciones.save(any())).thenAnswer(invocation->{ObservacionCliente o=invocation.getArgument(0);ReflectionTestUtils.setField(o,"id",UUID.randomUUID());return o;});
  }

  @Test void dosPrimerasRondasSonIncluidasYLaTerceraRequiereEvaluacionAdicional(){
    when(observaciones.maxRonda(proyectoId,TipoPropuesta.ANTEPROYECTO)).thenReturn((short)0,(short)1,(short)2);

    ObservacionRespuesta primera=service.registrarObservacion(propuestaId,"Ajustar ventana");
    ObservacionRespuesta segunda=service.registrarObservacion(propuestaId,"Mover acceso");
    ObservacionRespuesta tercera=service.registrarObservacion(propuestaId,"Cambiar distribución");

    assertThat(primera.numeroRonda()).isEqualTo((short)1);assertThat(primera.requiereCotizacionAdicional()).isFalse();
    assertThat(segunda.numeroRonda()).isEqualTo((short)2);assertThat(segunda.requiereCotizacionAdicional()).isFalse();
    assertThat(tercera.numeroRonda()).isEqualTo((short)3);assertThat(tercera.requiereCotizacionAdicional()).isTrue();
    assertThat(tercera.adicional()).isFalse();
    verify(proyecto,times(3)).cambiarEstado(EstadoProyecto.EN_DISENO);
  }

  @Test void noPermiteMarcarComoAdicionalUnaRondaIncluida(){
    ObservacionCliente observacion=observacion((short)2);when(observaciones.bloquear(observacion.getId())).thenReturn(Optional.of(observacion));
    when(acceso.esGestor(usuario)).thenReturn(true);

    assertThatThrownBy(()->service.actualizarObservacion(observacion.getId(),
        new ActualizarObservacion(EstadoObservacionCliente.PENDIENTE,true)))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("dos primeras rondas");
  }

  @Test void rondaAdicionalNoPuedeAtenderseSinCotizacionAceptadaYPagada(){
    ObservacionCliente observacion=observacion((short)3);observacion.actualizar(EstadoObservacionCliente.PENDIENTE,true,OffsetDateTime.now());
    when(observaciones.bloquear(observacion.getId())).thenReturn(Optional.of(observacion));
    when(acceso.esGestor(usuario)).thenReturn(true);

    assertThatThrownBy(()->service.actualizarObservacion(observacion.getId(),
        new ActualizarObservacion(EstadoObservacionCliente.ATENDIDA,true)))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("cotización aceptada");
  }

  @Test void conformidadDeProyectoFinalGeneraExpedienteFinal(){
    when(propuesta.getTipo()).thenReturn(TipoPropuesta.PROYECTO_FINAL);

    service.responder(propuestaId,new Responder(true,null));

    verify(propuesta).darConformidad(OffsetDateTime.parse("2026-10-20T17:00:00Z"));
    verify(proyecto).cambiarEstado(EstadoProyecto.EXPEDIENTE_FINAL);
    verify(historial).save(any(HistorialEstado.class));
  }

  @Test void rollbackCompensaLaVistaPreviaSubidaACloudinary(){
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_DISENO);when(proyecto.getCodigo()).thenReturn("PROY-2026-0001");
    when(acceso.esGestor(usuario)).thenReturn(true);when(usuario.getCorreo()).thenReturn("gestor@atelier.test");
    com.atelier.gestion.empleado.entity.Empleado empleado=mock(com.atelier.gestion.empleado.entity.Empleado.class);
    Usuario autor=mock(Usuario.class);when(empleado.getUsuario()).thenReturn(autor);
    when(empleados.findByUsuarioCorreoIgnoreCase(any())).thenReturn(Optional.of(empleado));
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
    when(storage.subir(any(),anyString())).thenReturn(new ArchivoAlmacenado("https://cloudinary/v1.pdf",
        "atelier/test/propuesta-v1","raw","application/pdf",100));
    when(propuestas.save(any())).thenAnswer(invocation->{Propuesta p=invocation.getArgument(0);ReflectionTestUtils.setField(p,"id",UUID.randomUUID());return p;});
    TransactionSynchronizationManager.initSynchronization();
    try{
      service.crear(proyectoId,TipoPropuesta.ANTEPROYECTO,"Avance",
          new MockMultipartFile("archivo","avance.pdf","application/pdf",new byte[]{1}));
      TransactionSynchronizationManager.getSynchronizations().forEach(s->
          s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
      verify(storage).eliminar("atelier/test/propuesta-v1","raw");
    }finally{TransactionSynchronizationManager.clearSynchronization();}
  }

  private ObservacionCliente observacion(short ronda){
    ObservacionCliente o=new ObservacionCliente(propuesta,cliente,ronda,"Cambio",OffsetDateTime.now());
    ReflectionTestUtils.setField(o,"id",UUID.randomUUID());return o;
  }
  private AtelierProperties propiedades(){return new AtelierProperties(new AtelierProperties.Cors("http://localhost"),
      new AtelierProperties.Jwt("01234567890123456789012345678901","test",15,7),
      new AtelierProperties.Cookie("REFRESH",false),new AtelierProperties.Cloudinary("demo","key","secret"),
      new AtelierProperties.Files(10_485_760),"test");}
}
