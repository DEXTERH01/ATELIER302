package com.atelier.gestion.auth.service;
import com.atelier.gestion.auth.entity.RefreshToken;
import com.atelier.gestion.auth.repository.RefreshTokenRepository;
import com.atelier.gestion.common.exception.NoAutorizadoException;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.usuario.entity.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TokenRefreshService {
  private final RefreshTokenRepository repo; private final AtelierProperties properties; private final SecureRandom random=new SecureRandom();
  public TokenRefreshService(RefreshTokenRepository repo,AtelierProperties properties){this.repo=repo;this.properties=properties;}
  @Transactional public String emitirInicial(Usuario usuario,String ip,String agent){return crear(usuario,UUID.randomUUID(),ip,agent).texto();}
  @Transactional(noRollbackFor=NoAutorizadoException.class) public Rotacion rotar(String token,String ip,String agent){OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);RefreshToken current=repo.buscarParaRotacion(hash(token)).orElseThrow(()->new NoAutorizadoException("Refresh token inválido"));if(current.estaRevocado()){repo.revocarFamilia(current.getFamiliaId(),now);throw new NoAutorizadoException("Se detectó reutilización del refresh token; la sesión fue revocada");}if(current.estaExpirado(now)){repo.revocarFamilia(current.getFamiliaId(),now);throw new NoAutorizadoException("Refresh token expirado");}if(!current.getUsuario().isActivo()){repo.revocarFamilia(current.getFamiliaId(),now);throw new NoAutorizadoException("Usuario inactivo");}TokenCreado next=crear(current.getUsuario(),current.getFamiliaId(),ip,agent);current.revocar(now,next.entidad().getId());repo.save(current);return new Rotacion(current.getUsuario(),next.texto());}
  @Transactional public void revocar(String token){repo.buscarParaRotacion(hash(token)).ifPresent(t->repo.revocarFamilia(t.getFamiliaId(),OffsetDateTime.now(ZoneOffset.UTC)));}
  private TokenCreado crear(Usuario user,UUID familia,String ip,String agent){byte[] bytes=new byte[64];random.nextBytes(bytes);String text=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);RefreshToken entity=new RefreshToken(user,familia,hash(text),now,now.plusDays(properties.jwt().refreshExpirationDays()),limitar(ip,45),limitar(agent,300));repo.save(entity);return new TokenCreado(entity,text);}
  private String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
  private String limitar(String s,int n){return s==null?null:s.substring(0,Math.min(s.length(),n));}
  private record TokenCreado(RefreshToken entidad,String texto){} public record Rotacion(Usuario usuario,String token){}
}
