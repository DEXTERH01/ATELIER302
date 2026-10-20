package com.atelier.gestion.auth.controller;
import com.atelier.gestion.auth.dto.AuthDtos.*;
import com.atelier.gestion.auth.service.AuthService;
import com.atelier.gestion.config.AtelierProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service; private final AtelierProperties props;
  public AuthController(AuthService service,AtelierProperties props){this.service=service;this.props=props;}
  @PostMapping("/register-client") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterClientRequest body,HttpServletRequest req){return session(service.registrarCliente(body,ip(req),req.getHeader("User-Agent")),HttpStatus.CREATED);}
  @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body,HttpServletRequest req){return session(service.login(body,ip(req),req.getHeader("User-Agent")),HttpStatus.OK);}
  @PostMapping("/refresh") public ResponseEntity<AuthResponse> refresh(@CookieValue(name="ATELIER_REFRESH",required=false) String token,HttpServletRequest req){if(token==null)throw new com.atelier.gestion.common.exception.NoAutorizadoException("No existe sesión renovable");return session(service.renovar(token,ip(req),req.getHeader("User-Agent")),HttpStatus.OK);}
  @PostMapping("/logout") public ResponseEntity<Void> logout(@CookieValue(name="ATELIER_REFRESH",required=false) String token){service.logout(token);return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,clearCookie().toString()).build();}
  @GetMapping("/me") public UsuarioResponse me(Authentication auth){return service.me(auth.getName());}
  private ResponseEntity<AuthResponse> session(AuthService.Sesion s,HttpStatus status){return ResponseEntity.status(status).header(HttpHeaders.SET_COOKIE,cookie(s.refreshToken()).toString()).body(s.respuesta());}
  private ResponseCookie cookie(String value){return ResponseCookie.from(props.cookie().name(),value).httpOnly(true).secure(props.cookie().secure()).sameSite("Lax").path("/api/v1/auth").maxAge(Duration.ofDays(props.jwt().refreshExpirationDays())).build();}
  private ResponseCookie clearCookie(){return ResponseCookie.from(props.cookie().name(),"").httpOnly(true).secure(props.cookie().secure()).sameSite("Lax").path("/api/v1/auth").maxAge(Duration.ZERO).build();}
  private String ip(HttpServletRequest req){String forwarded=req.getHeader("X-Forwarded-For");return forwarded==null?req.getRemoteAddr():forwarded.split(",")[0].trim();}
}
