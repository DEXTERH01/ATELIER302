package com.atelier.gestion.auth.service;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import com.atelier.gestion.auth.entity.RefreshToken;
import com.atelier.gestion.auth.repository.RefreshTokenRepository;
import com.atelier.gestion.common.exception.NoAutorizadoException;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class TokenRefreshServiceTest {
  @Test void reutilizacionRevocaTodaLaFamilia(){RefreshTokenRepository repo=mock(RefreshTokenRepository.class);AtelierProperties props=new AtelierProperties(new AtelierProperties.Cors("http://localhost"),new AtelierProperties.Jwt("01234567890123456789012345678901","test",15,7),new AtelierProperties.Cookie("ATELIER_REFRESH",false),new AtelierProperties.Cloudinary("demo","key","secret"),new AtelierProperties.Files(10_485_760),"test");TokenRefreshService service=new TokenRefreshService(repo,props);UUID familia=UUID.randomUUID();RefreshToken token=new RefreshToken(mock(Usuario.class),familia,"hash",OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),OffsetDateTime.now(ZoneOffset.UTC).plusDays(1),"ip","ua");token.revocar(OffsetDateTime.now(ZoneOffset.UTC),UUID.randomUUID());when(repo.buscarParaRotacion(anyString())).thenReturn(Optional.of(token));assertThatThrownBy(()->service.rotar("token-reutilizado","ip","ua")).isInstanceOf(NoAutorizadoException.class).hasMessageContaining("reutilización");verify(repo).revocarFamilia(eq(familia),any());}
}

