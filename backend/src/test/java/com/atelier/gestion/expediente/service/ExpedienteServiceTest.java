package com.atelier.gestion.expediente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.entity.Proyecto;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExpedienteServiceTest {
  @Test void crearEsIdempotenteSiElProyectoYaTieneExpediente(){
    ExpedienteRepository repo=mock(ExpedienteRepository.class);
    Proyecto proyecto=mock(Proyecto.class);Usuario usuario=mock(Usuario.class);
    Expediente existente=mock(Expediente.class);
    when(repo.findByProyectoId(any())).thenReturn(Optional.of(existente));
    ExpedienteService service=new ExpedienteService(repo,mock(ProyectoRepository.class),
        mock(AutorizacionProyectoService.class),mock(AuditoriaService.class));
    assertThat(service.crearIdempotente(proyecto,usuario)).isSameAs(existente);
    verify(repo,never()).save(any());
  }
}
