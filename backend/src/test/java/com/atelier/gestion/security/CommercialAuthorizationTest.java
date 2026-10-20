package com.atelier.gestion.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.cotizacion.controller.CotizacionController;
import com.atelier.gestion.cotizacion.service.CotizacionService;
import com.atelier.gestion.pago.controller.PagoController;
import com.atelier.gestion.pago.service.PagoService;
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

@WebMvcTest({CotizacionController.class,PagoController.class})
@Import({SecurityConfig.class,RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173","atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test","atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7","atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class CommercialAuthorizationTest {
  @Autowired MockMvc mvc;
  @MockitoBean CotizacionService cotizaciones;@MockitoBean PagoService pagos;@MockitoBean UsuarioRepository usuarios;
  private final UUID id=UUID.randomUUID();

  @Test void clienteNoPuedeEnviarCotizacion()throws Exception{mvc.perform(post("/api/v1/cotizaciones/{id}/enviar",id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))).andExpect(status().isForbidden());}
  @Test void encargadoPuedeEnviarCotizacion()throws Exception{mvc.perform(post("/api/v1/cotizaciones/{id}/enviar",id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENCARGADO")))).andExpect(status().isOk());}
  @Test void soloClientePuedeResponderCotizacion()throws Exception{mvc.perform(post("/api/v1/cotizaciones/{id}/responder",id).contentType(MediaType.APPLICATION_JSON).content("{\"aceptar\":true}").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENCARGADO")))).andExpect(status().isForbidden());}
  @Test void clientePuedeResponderCotizacion()throws Exception{mvc.perform(post("/api/v1/cotizaciones/{id}/responder",id).contentType(MediaType.APPLICATION_JSON).content("{\"aceptar\":true}").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))).andExpect(status().isOk());}
  @Test void soloAdministradorPuedeValidarPago()throws Exception{mvc.perform(post("/api/v1/pagos/{id}/validar",id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENCARGADO")))).andExpect(status().isForbidden());}
  @Test void administradorPuedeValidarPago()throws Exception{mvc.perform(post("/api/v1/pagos/{id}/validar",id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))).andExpect(status().isOk());}
}
