package com.atelier.gestion.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atelier.gestion.auditoria.controller.AuditoriaController;
import com.atelier.gestion.auditoria.service.ConsultaAuditoriaService;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.reporte.controller.ReporteController;
import com.atelier.gestion.reporte.service.ReporteExcelService;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ReporteController.class, AuditoriaController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@EnableConfigurationProperties(AtelierProperties.class)
@TestPropertySource(properties={"atelier.cors.allowed-origins=http://localhost:5173","atelier.jwt.secret=01234567890123456789012345678901","atelier.jwt.issuer=test","atelier.jwt.access-expiration-minutes=15","atelier.jwt.refresh-expiration-days=7","atelier.cookie.name=ATELIER_REFRESH","atelier.cookie.secure=false"})
class ReportAuthorizationTest {
  @Autowired MockMvc mvc;
  @MockitoBean ReporteExcelService reportes; @MockitoBean ConsultaAuditoriaService auditoria; @MockitoBean UsuarioRepository usuarios;

  @Test void encargadoPuedeExportarPeroClienteNo() throws Exception {
    mvc.perform(get("/api/v1/reportes/proyectos.xlsx").with(rol("ENCARGADO"))).andExpect(status().isOk());
    mvc.perform(get("/api/v1/reportes/proyectos.xlsx").with(rol("CLIENTE"))).andExpect(status().isForbidden());
  }

  @Test void soloAdministradorConsultaAuditoria() throws Exception {
    mvc.perform(get("/api/v1/auditoria").with(rol("ENCARGADO"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/auditoria").with(rol("ADMINISTRADOR"))).andExpect(status().isOk());
  }

  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor rol(String rol) {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
  }
}
