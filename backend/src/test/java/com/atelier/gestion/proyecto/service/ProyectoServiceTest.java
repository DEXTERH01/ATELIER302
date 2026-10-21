package com.atelier.gestion.proyecto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.cliente.service.ClienteService;
import com.atelier.gestion.empleado.service.EmpleadoService;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.dto.ProyectoDtos.Crear;
import com.atelier.gestion.proyecto.dto.ProyectoDtos.Transicion;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.*;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.*;

class ProyectoServiceTest {
  @Mock ProyectoRepository proyectos;
  @Mock RequerimientoRepository requerimientos;
  @Mock HistorialEstadoRepository historial;
  @Mock ClienteRepository clientes;
  @Mock ClienteService clienteService;
  @Mock EmpleadoService empleadoService;
  @Mock CorrelativoProyectoService correlativos;
  @Mock PoliticaEstadosProyecto politica;
  @Mock SesionActual sesion;
  @Mock AuditoriaService auditoria;
  @Mock EstudioTecnicoRepository estudios;
  @Mock ExpedienteRepository expedientes;
  ProyectoService service;

  @BeforeEach void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new ProyectoService(proyectos, requerimientos, historial, clientes, clienteService,
        empleadoService, correlativos, politica, sesion, auditoria, estudios, expedientes);
  }

  @Test void clienteCreaProyectoSoloParaSuPropioPerfilEIgnoraEncargadoSolicitado() {
    Rol rol = mock(Rol.class);
    Usuario usuario = mock(Usuario.class);
    Cliente cliente = mock(Cliente.class);
    when(rol.getNombre()).thenReturn("CLIENTE");
    when(usuario.getRol()).thenReturn(rol);
    when(sesion.usuario()).thenReturn(usuario);
    when(clienteService.clienteActual()).thenReturn(cliente);
    when(cliente.getRazonSocial()).thenReturn("Cliente de prueba");
    when(correlativos.siguiente()).thenReturn("PROY-2026-0001");
    when(proyectos.save(any())).thenAnswer(i -> i.getArgument(0));

    Crear req = new Crear(UUID.randomUUID(), UUID.randomUUID(), "Casa Lima", "Vivienda",
        "RESIDENCIAL", "Lima", null, false, LocalDate.now().plusMonths(3));
    service.crear(req);

    ArgumentCaptor<Proyecto> captor = ArgumentCaptor.forClass(Proyecto.class);
    verify(proyectos).save(captor.capture());
    assertThat(captor.getValue().getCliente()).isSameAs(cliente);
    assertThat(captor.getValue().getEncargado()).isNull();
    verifyNoInteractions(empleadoService);
    verify(historial).save(any(HistorialEstado.class));
  }

  @Test void noPermiteActivarProyectoPorTransicionManual() {
    UUID id=UUID.randomUUID();Rol rol=mock(Rol.class);Usuario usuario=mock(Usuario.class);
    when(rol.getNombre()).thenReturn("ENCARGADO");when(usuario.getRol()).thenReturn(rol);
    when(sesion.usuario()).thenReturn(usuario);when(proyectos.bloquear(id)).thenReturn(java.util.Optional.of(mock(Proyecto.class)));
    assertThatThrownBy(()->service.transicionar(id,new Transicion(EstadoProyecto.PROYECTO_ACTIVO,null)))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("flujo de negocio");
    verifyNoInteractions(politica);
  }

  @Test void noPermiteAprobarTramitePublicoPorTransicionManual() {
    UUID id=UUID.randomUUID();Rol rol=mock(Rol.class);Usuario usuario=mock(Usuario.class);Proyecto proyecto=mock(Proyecto.class);
    when(rol.getNombre()).thenReturn("ENCARGADO");when(usuario.getRol()).thenReturn(rol);
    when(sesion.usuario()).thenReturn(usuario);when(proyectos.bloquear(id)).thenReturn(java.util.Optional.of(proyecto));
    when(proyecto.isRequiereTramitePublico()).thenReturn(true);when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_TRAMITE_PUBLICO);
    assertThatThrownBy(()->service.transicionar(id,new Transicion(EstadoProyecto.APROBADO,null)))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("trámite público");
    verify(proyecto,never()).cambiarEstado(any());
  }

  @Test void noPermiteIniciarDisenoMientrasExistanEstudiosRequeridosPendientes() {
    UUID id=UUID.randomUUID();Rol rol=mock(Rol.class);Usuario usuario=mock(Usuario.class);
    Proyecto proyecto=mock(Proyecto.class);
    when(rol.getNombre()).thenReturn("ENCARGADO");when(usuario.getRol()).thenReturn(rol);
    when(sesion.usuario()).thenReturn(usuario);when(proyectos.bloquear(id)).thenReturn(java.util.Optional.of(proyecto));
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.EN_ESTUDIOS);
    when(estudios.countByProyectoIdAndRequeridoTrueAndEstadoNot(id,
        com.atelier.gestion.estudio.entity.EstadoEstudio.COMPLETADO)).thenReturn(1L);

    assertThatThrownBy(()->service.transicionar(id,new Transicion(EstadoProyecto.EN_DISENO,null)))
        .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("estudios requeridos");
    verifyNoInteractions(politica);
    verify(proyecto,never()).cambiarEstado(any());
  }
}
