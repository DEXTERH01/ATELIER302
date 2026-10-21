package com.atelier.gestion.expediente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.documento.entity.CategoriaDocumento;
import com.atelier.gestion.propuesta.repository.PropuestaRepository;
import com.atelier.gestion.propuesta.entity.*;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class ExpedienteServiceTest {
  private final ExpedienteRepository expedientes=mock(ExpedienteRepository.class);
  private final ProyectoRepository proyectos=mock(ProyectoRepository.class);
  private final DocumentoRepository documentos=mock(DocumentoRepository.class);
  private final PropuestaRepository propuestas=mock(PropuestaRepository.class);
  private final AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
  private final AuditoriaService auditoria=mock(AuditoriaService.class);
  private final ExpedienteService service=new ExpedienteService(expedientes,proyectos,documentos,propuestas,acceso,auditoria);

  @Test void crearEsIdempotenteSiElProyectoYaTieneExpediente(){
    Proyecto proyecto=mock(Proyecto.class);Usuario usuario=mock(Usuario.class);
    Expediente existente=mock(Expediente.class);
    when(expedientes.findByProyectoId(any())).thenReturn(Optional.of(existente));
    assertThat(service.crearIdempotente(proyecto,usuario)).isSameAs(existente);
    verify(expedientes,never()).save(any());
  }

  @Test void exigePropuestaFinalConformeParaCompletar(){UUID proyectoId=UUID.randomUUID();Proyecto proyecto=mock(Proyecto.class);Expediente expediente=mock(Expediente.class);
    when(acceso.usuario()).thenReturn(mock(Usuario.class));when(proyecto.getEstado()).thenReturn(EstadoProyecto.EXPEDIENTE_FINAL);
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));when(expedientes.findByProyectoId(proyectoId)).thenReturn(Optional.of(expediente));
    when(propuestas.existsByProyectoIdAndTipoAndEstado(proyectoId,TipoPropuesta.PROYECTO_FINAL,EstadoPropuesta.CONFORME)).thenReturn(false);
    assertThatThrownBy(()->service.completar(proyectoId)).isInstanceOf(com.atelier.gestion.common.exception.ReglaNegocioException.class).hasMessageContaining("propuesta final");
    verify(expediente,never()).completar();}

  @Test void completaCuandoPropuestaYDocumentoFinalExisten(){UUID proyectoId=UUID.randomUUID();UUID expedienteId=UUID.randomUUID();Proyecto proyecto=mock(Proyecto.class);Expediente expediente=mock(Expediente.class);Usuario usuario=mock(Usuario.class);
    when(acceso.usuario()).thenReturn(usuario);when(proyecto.getEstado()).thenReturn(EstadoProyecto.EXPEDIENTE_FINAL);
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));when(expedientes.findByProyectoId(proyectoId)).thenReturn(Optional.of(expediente));
    when(expediente.getId()).thenReturn(expedienteId);when(expediente.getProyecto()).thenReturn(proyecto);when(expediente.getEstado()).thenReturn("COMPLETO");
    when(propuestas.existsByProyectoIdAndTipoAndEstado(proyectoId,TipoPropuesta.PROYECTO_FINAL,EstadoPropuesta.CONFORME)).thenReturn(true);
    when(documentos.existsByExpedienteIdAndCategoriaAndVigenteTrue(expedienteId,CategoriaDocumento.EXPEDIENTE)).thenReturn(true);
    service.completar(proyectoId);verify(expediente).completar();verify(auditoria).registrar(eq(usuario),eq("expedientes"),eq(expedienteId),eq("UPDATE"),anyMap());}

  @Test void cierreArchivaElExpedienteSegunElEsquema(){Expediente expediente=new Expediente(mock(Proyecto.class),"EXP-2026-9999");OffsetDateTime ahora=OffsetDateTime.parse("2026-10-21T15:00:00Z");
    expediente.cerrar(ahora);assertThat(expediente.getEstado()).isEqualTo("ARCHIVADO");assertThat(expediente.getCerradoEn()).isEqualTo(ahora);}
}
