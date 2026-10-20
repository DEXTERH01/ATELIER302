package com.atelier.gestion.security;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
  @Bean UserDetailsService userDetailsService(UsuarioRepository repo){return username->{var u=repo.findByCorreoIgnoreCase(username).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));return User.withUsername(u.getCorreo()).password(u.getClaveHash()).disabled(!u.isActivo()).authorities(new SimpleGrantedAuthority("ROLE_"+u.getRol().getNombre())).build();};}
  @Bean AuthenticationProvider authenticationProvider(UserDetailsService uds,PasswordEncoder encoder){DaoAuthenticationProvider p=new DaoAuthenticationProvider(uds);p.setPasswordEncoder(encoder);return p;}
  @Bean AuthenticationManager authenticationManager(AuthenticationProvider provider){return new ProviderManager(provider);}
  @Bean SecretKey jwtSecretKey(AtelierProperties p){byte[] bytes=p.jwt().secret().getBytes(StandardCharsets.UTF_8);if(bytes.length<32)throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");return new SecretKeySpec(bytes,"HmacSHA256");}
  @Bean JwtEncoder jwtEncoder(SecretKey key){return new NimbusJwtEncoder(new ImmutableSecret<>(key));}
  @Bean JwtDecoder jwtDecoder(SecretKey key){return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();}
  @Bean JwtAuthenticationConverter jwtAuthenticationConverter(){JwtGrantedAuthoritiesConverter grants=new JwtGrantedAuthoritiesConverter();grants.setAuthoritiesClaimName("roles");grants.setAuthorityPrefix("");JwtAuthenticationConverter converter=new JwtAuthenticationConverter();converter.setJwtGrantedAuthoritiesConverter(grants);return converter;}
  @Bean CorsConfigurationSource corsConfigurationSource(AtelierProperties p){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(p.cors().origins());c.setAllowedMethods(java.util.List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","X-Correlation-Id"));c.setExposedHeaders(java.util.List.of("X-Correlation-Id","Content-Disposition"));c.setAllowCredentials(true);c.setMaxAge(3600L);UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationConverter converter,RestAuthenticationEntryPoint entry,RestAccessDeniedHandler denied) throws Exception {return http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/login","/api/v1/auth/register-client","/api/v1/auth/refresh","/api/v1/auth/logout","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/actuator/health").permitAll().requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRADOR").anyRequest().authenticated()).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(converter)).authenticationEntryPoint(entry)).exceptionHandling(e->e.authenticationEntryPoint(entry).accessDeniedHandler(denied)).build();}
}

