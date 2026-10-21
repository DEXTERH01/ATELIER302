package com.atelier.gestion.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atelier.gestion.asignacion.controller.AsignacionController;
import com.atelier.gestion.asignacion.service.AsignacionService;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.documento.controller.DocumentoController;
import com.atelier.gestion.documento.service.DocumentoService;
import com.atelier.gestion.especialidad.controller.EspecialidadController;
import com.atelier.gestion.especialidad.service.EspecialidadService;
import com.atelier.gestion.estudio.controller.EstudioController;
import com.atelier.gestion.estudio.service.EstudioService;
import com.atelier.gestion.tarea.controller.TareaController;
import com.atelier.gestion.tarea.service.TareaService;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AsignacionController.class,TareaController.class,EstudioController.class,
    DocumentoController.class,EspecialidadController.class})
@Import({SecurityConfig.class,RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173",
    "atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test",
    "atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7",
    "atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class TechnicalAuthorizationTest {
  @Autowired MockMvc mvc;
  @MockitoBean AsignacionService asignaciones;@MockitoBean TareaService tareas;
  @MockitoBean EstudioService estudios;@MockitoBean DocumentoService documentos;
  @MockitoBean EspecialidadService especialidades;@MockitoBean UsuarioRepository usuarios;
  private final UUID id=UUID.randomUUID();

  @Test void clienteNoPuedeAsignarEspecialistas()throws Exception{
    mvc.perform(post("/api/v1/proyectos/{id}/asignaciones",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"empleadoId\":\""+UUID.randomUUID()+"\",\"especialidadId\":1,\"rolProyecto\":\"Diseño\"}")
        .with(rol("CLIENTE"))).andExpect(status().isForbidden());
  }

  @Test void encargadoPuedeAsignarEspecialistas()throws Exception{
    mvc.perform(post("/api/v1/proyectos/{id}/asignaciones",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"empleadoId\":\""+UUID.randomUUID()+"\",\"especialidadId\":1,\"rolProyecto\":\"Diseño\"}")
        .with(rol("ENCARGADO"))).andExpect(status().isCreated());
  }

  @Test void especialistaPuedeActualizarTareaPeroNoCrearEstudio()throws Exception{
    mvc.perform(patch("/api/v1/tareas/{id}",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"titulo\":\"Planos\",\"prioridad\":\"ALTA\",\"estado\":\"EN_PROCESO\"}")
        .with(rol("ESPECIALISTA"))).andExpect(status().isOk());
    mvc.perform(post("/api/v1/proyectos/{id}/estudios",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipo\":\"Suelos\",\"requerido\":true}").with(rol("ESPECIALISTA")))
        .andExpect(status().isForbidden());
  }

  @Test void soloAdministradorGestionaCatalogoDeEspecialidades()throws Exception{
    String body="{\"nombre\":\"Estructuras\",\"descripcion\":\"Cálculo\"}";
    mvc.perform(post("/api/v1/especialidades").contentType(MediaType.APPLICATION_JSON).content(body)
        .with(rol("ENCARGADO"))).andExpect(status().isForbidden());
    mvc.perform(post("/api/v1/especialidades").contentType(MediaType.APPLICATION_JSON).content(body)
        .with(rol("ADMINISTRADOR"))).andExpect(status().isCreated());
  }

  @Test void clienteAutenticadoPuedeListarDocumentosAutorizados()throws Exception{
    mvc.perform(get("/api/v1/expedientes/{id}/documentos",id).with(rol("CLIENTE")))
        .andExpect(status().isOk());
  }

  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor rol(String rol){
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_"+rol));
  }
}
