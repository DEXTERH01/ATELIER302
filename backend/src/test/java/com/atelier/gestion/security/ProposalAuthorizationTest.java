package com.atelier.gestion.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.propuesta.controller.PropuestaController;
import com.atelier.gestion.propuesta.service.PropuestaService;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PropuestaController.class)
@Import({SecurityConfig.class,RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173",
    "atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test",
    "atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7",
    "atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class ProposalAuthorizationTest {
  @Autowired MockMvc mvc;@MockitoBean PropuestaService propuestas;@MockitoBean UsuarioRepository usuarios;
  private final UUID id=UUID.randomUUID();

  @Test void especialistaCreaBorradorPeroNoPuedeEnviarlo()throws Exception{
    MockMultipartFile archivo=new MockMultipartFile("archivo","avance.pdf","application/pdf",new byte[]{1});
    mvc.perform(multipart("/api/v1/proyectos/{id}/propuestas",id).file(archivo)
        .param("tipo","ANTEPROYECTO").with(rol("ESPECIALISTA"))).andExpect(status().isCreated());
    mvc.perform(post("/api/v1/propuestas/{id}/enviar",id).with(rol("ESPECIALISTA")))
        .andExpect(status().isForbidden());
  }

  @Test void clienteRespondePeroNoPuedeCrearPropuesta()throws Exception{
    mvc.perform(post("/api/v1/propuestas/{id}/respuesta",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"conforme\":true}").with(rol("CLIENTE"))).andExpect(status().isOk());
    MockMultipartFile archivo=new MockMultipartFile("archivo","avance.pdf","application/pdf",new byte[]{1});
    mvc.perform(multipart("/api/v1/proyectos/{id}/propuestas",id).file(archivo)
        .param("tipo","ANTEPROYECTO").with(rol("CLIENTE"))).andExpect(status().isForbidden());
  }

  @Test void encargadoPuedeEnviarYActualizarObservaciones()throws Exception{
    mvc.perform(post("/api/v1/propuestas/{id}/enviar",id).with(rol("ENCARGADO"))).andExpect(status().isOk());
    mvc.perform(patch("/api/v1/observaciones-cliente/{id}",id).contentType(MediaType.APPLICATION_JSON)
        .content("{\"estado\":\"PENDIENTE\",\"adicional\":true}").with(rol("ENCARGADO")))
        .andExpect(status().isOk());
  }

  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor rol(String rol){
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_"+rol));
  }
}
