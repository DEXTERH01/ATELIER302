package com.atelier.gestion.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.tramite.controller.TramiteController;
import com.atelier.gestion.tramite.service.TramiteService;
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

@WebMvcTest(TramiteController.class)
@Import({SecurityConfig.class,RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173",
    "atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test",
    "atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7",
    "atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class ProcedureAuthorizationTest {
  @Autowired MockMvc mvc;@MockitoBean TramiteService tramites;@MockitoBean UsuarioRepository usuarios;
  private final UUID id=UUID.randomUUID();
  @Test void clientePuedeConsultarPeroNoGestionar()throws Exception{
    mvc.perform(get("/api/v1/proyectos/{id}/tramites",id).with(rol("CLIENTE"))).andExpect(status().isOk());
    mvc.perform(post("/api/v1/proyectos/{id}/tramites",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"responsableId\":\""+UUID.randomUUID()+"\",\"entidad\":\"MML\",\"tipoTramite\":\"Licencia\"}")
        .with(rol("CLIENTE"))).andExpect(status().isForbidden());}
  @Test void encargadoPuedeRegistrarObservacionesYResolver()throws Exception{
    mvc.perform(post("/api/v1/tramites/{id}/observaciones",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"descripcion\":\"Subsanar plano\",\"fechaNotificacion\":\"2026-10-21\"}")
        .with(rol("ENCARGADO"))).andExpect(status().isCreated());
    mvc.perform(post("/api/v1/tramites/{id}/resolver",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"aprobado\":false,\"fechaRespuesta\":\"2026-10-21\",\"observacionGeneral\":\"No viable\"}")
        .with(rol("ENCARGADO"))).andExpect(status().isOk());}
  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor rol(String rol){return jwt().authorities(new SimpleGrantedAuthority("ROLE_"+rol));}
}
