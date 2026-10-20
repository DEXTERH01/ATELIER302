package com.atelier.gestion.security;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
  private final JwtEncoder encoder; private final AtelierProperties properties;
  public JwtService(JwtEncoder encoder,AtelierProperties properties){this.encoder=encoder;this.properties=properties;}
  public String emitir(Usuario usuario){Instant now=Instant.now();JwtClaimsSet claims=JwtClaimsSet.builder().issuer(properties.jwt().issuer()).issuedAt(now).expiresAt(now.plus(properties.jwt().accessExpirationMinutes(),ChronoUnit.MINUTES)).subject(usuario.getCorreo()).claim("uid",usuario.getId().toString()).claim("roles",List.of("ROLE_"+usuario.getRol().getNombre())).build();return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();}
  public long expiracionSegundos(){return properties.jwt().accessExpirationMinutes()*60;}
}

