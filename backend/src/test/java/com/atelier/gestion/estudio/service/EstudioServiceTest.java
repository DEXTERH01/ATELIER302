package com.atelier.gestion.estudio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.estudio.dto.EstudioDtos.Actualizar;
import com.atelier.gestion.estudio.entity.*;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EstudioServiceTest {
  private final EstudioTecnicoRepository estudios=mock(EstudioTecnicoRepository.class);
  private final ProyectoRepository proyectos=mock(ProyectoRepository.class);
  private final EmpleadoRepository empleados=mock(EmpleadoRepository.class);
  private final DocumentoRepository documentos=mock(DocumentoRepository.class);
  private final HistorialEstadoRepository historial=mock(HistorialEstadoRepository.class);
  private final AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
  private final AuditoriaService auditoria=mock(AuditoriaService.class);
  private EstudioService service;
  private Proyecto proyecto;
  private EstudioTecnico estudio;
  private Usuario usuario;
  private UUID proyectoId;
  private UUID estudioId;

  @BeforeEach void setUp(){
    service=new EstudioService(estudios,proyectos,empleados,documentos,historial,acceso,auditoria);
    proyecto=mock(Proyecto.class);usuario=mock(Usuario.class);
    proyectoId=UUID.randomUUID();estudioId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_ESTUDIOS);
    estudio=new EstudioTecnico(proyecto,null,"Topografía",null,true,LocalDate.of(2026,10,20),null);
    ReflectionTestUtils.setField(estudio,"id",estudioId);
    when(acceso.usuario()).thenReturn(usuario);when(acceso.esGestor(usuario)).thenReturn(true);
    when(estudios.findById(estudioId)).thenReturn(Optional.of(estudio));
    when(proyectos.bloquear(proyectoId)).thenReturn(Optional.of(proyecto));
  }

  @Test void exigeEntregableVigenteAntesDeValidarElEstudio(){
    when(documentos.existsByEstudioIdAndVigenteTrue(estudioId)).thenReturn(false);
    Actualizar req=completado();

    assertThatThrownBy(()->service.actualizar(estudioId,req))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("entregable vigente");
    assertThat(estudio.getEstado()).isEqualTo(EstadoEstudio.PENDIENTE);
    verify(estudios,never()).flush();
  }

  @Test void ultimoEstudioRequeridoValidadoIniciaDisenoConHistorial(){
    when(documentos.existsByEstudioIdAndVigenteTrue(estudioId)).thenReturn(true);
    when(estudios.countByProyectoIdAndRequeridoTrueAndEstadoNot(proyectoId,EstadoEstudio.COMPLETADO)).thenReturn(0L);

    service.actualizar(estudioId,completado());

    assertThat(estudio.getEstado()).isEqualTo(EstadoEstudio.COMPLETADO);
    verify(proyecto).cambiarEstado(EstadoProyecto.EN_DISENO);
    verify(historial).save(any(HistorialEstado.class));
  }

  private Actualizar completado(){
    return new Actualizar(null,"Topografía",null,true,EstadoEstudio.COMPLETADO,
        LocalDate.of(2026,10,20),LocalDate.of(2026,10,21),"Informe conforme",null);
  }
}
