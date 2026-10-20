package com.atelier.gestion.security;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.atelier.gestion.config.*;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
@WebMvcTest(SecurityAuthorizationTest.TestController.class)
@Import({SecurityConfig.class,RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class,SecurityAuthorizationTest.TestController.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173","atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test","atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7","atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class SecurityAuthorizationTest {
  @Autowired MockMvc mvc; @MockitoBean UsuarioRepository usuarios;
  @Test void clienteNoAccedeAOperacionAdministrativa()throws Exception{mvc.perform(get("/api/v1/test/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))).andExpect(status().isForbidden());}
  @Test void administradorAccedeAOperacionAdministrativa()throws Exception{mvc.perform(get("/api/v1/test/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))).andExpect(status().isOk());}
  @RestController public static class TestController {@GetMapping("/api/v1/test/admin") @PreAuthorize("hasRole('ADMINISTRADOR')") String admin(){return "ok";}}
}
