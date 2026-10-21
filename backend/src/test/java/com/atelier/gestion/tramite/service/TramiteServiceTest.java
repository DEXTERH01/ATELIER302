package com.atelier.gestion.tramite.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.documento.entity.CategoriaDocumento;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.tramite.dto.TramiteDtos.*;
import com.atelier.gestion.tramite.entity.*;
import com.atelier.gestion.tramite.repository.*;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TramiteServiceTest {
  private final TramitePublicoRepository tramites=mock(TramitePublicoRepository.class);
  private final ObservacionEntidadRepository observaciones=mock(ObservacionEntidadRepository.class);
  private final ProyectoRepository proyectos=mock(ProyectoRepository.class);
  private final ExpedienteRepository expedientes=mock(ExpedienteRepository.class);
  private final EmpleadoRepository empleados=mock(EmpleadoRepository.class);
  private final DocumentoRepository documentos=mock(DocumentoRepository.class);
  private final HistorialEstadoRepository historial=mock(HistorialEstadoRepository.class);
  private final AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
  private final AuditoriaService auditoria=mock(AuditoriaService.class);
  private final TramiteService service=new TramiteService(tramites,observaciones,proyectos,expedientes,
      empleados,documentos,historial,acceso,auditoria,
      Clock.fixed(Instant.parse("2026-10-21T15:00:00Z"),ZoneOffset.UTC));
  private final UUID tramiteId=UUID.randomUUID();private final UUID proyectoId=UUID.randomUUID();
  private TramitePublico tramite;private Proyecto proyecto;private Usuario usuario;

  @BeforeEach void setUp(){tramite=mock(TramitePublico.class);proyecto=mock(Proyecto.class);usuario=mock(Usuario.class);
    when(tramite.getId()).thenReturn(tramiteId);when(tramite.getProyecto()).thenReturn(proyecto);
    when(proyecto.getId()).thenReturn(proyectoId);when(acceso.usuario()).thenReturn(usuario);
    when(tramites.bloquear(tramiteId)).thenReturn(Optional.of(tramite));}

  @Test void noPresentaSiExpedienteNoEstaCompleto(){Expediente expediente=mock(Expediente.class);
    when(tramite.getEstado()).thenReturn(EstadoTramite.PREPARACION);when(proyecto.getEstado()).thenReturn(EstadoProyecto.EXPEDIENTE_FINAL);
    when(tramite.getNumeroExpedienteExterno()).thenReturn("MML-2026-100");when(expedientes.findByProyectoId(proyectoId)).thenReturn(Optional.of(expediente));
    when(expediente.getEstado()).thenReturn("ABIERTO");
    UUID responsableId=UUID.randomUUID();when(empleados.findById(responsableId)).thenReturn(Optional.of(mock(Empleado.class)));
    Actualizar req=new Actualizar(responsableId,"Municipalidad","Licencia","MML-2026-100",null,EstadoTramite.PRESENTADO,LocalDate.of(2026,10,21));
    assertThatThrownBy(()->service.actualizar(tramiteId,req)).isInstanceOf(ReglaNegocioException.class).hasMessageContaining("completo");
    verify(tramite,never()).presentar(any());verify(historial,never()).save(any());}

  @Test void noApruebaConObservacionesPendientes(){when(tramite.getEstado()).thenReturn(EstadoTramite.EN_REVISION);
    when(tramite.getFechaPresentacion()).thenReturn(LocalDate.of(2026,10,1));when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_TRAMITE_PUBLICO);
    when(observaciones.countByTramiteIdAndEstado(tramiteId,EstadoObservacionEntidad.PENDIENTE)).thenReturn(1L);
    assertThatThrownBy(()->service.resolver(tramiteId,new Resolver(true,LocalDate.of(2026,10,21),"Licencia aprobada")))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("pendientes");
    verify(documentos,never()).existsByTramiteIdAndCategoriaAndVigenteTrue(any(),eq(CategoriaDocumento.RESOLUCION));}

  @Test void noApruebaSinResolucionAdjunta(){when(tramite.getEstado()).thenReturn(EstadoTramite.EN_REVISION);
    when(tramite.getFechaPresentacion()).thenReturn(LocalDate.of(2026,10,1));when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_TRAMITE_PUBLICO);
    when(observaciones.countByTramiteIdAndEstado(tramiteId,EstadoObservacionEntidad.PENDIENTE)).thenReturn(0L);
    when(documentos.existsByTramiteIdAndCategoriaAndVigenteTrue(tramiteId,CategoriaDocumento.RESOLUCION)).thenReturn(false);
    assertThatThrownBy(()->service.resolver(tramiteId,new Resolver(true,LocalDate.of(2026,10,21),"Licencia aprobada")))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("resolución");}
}
