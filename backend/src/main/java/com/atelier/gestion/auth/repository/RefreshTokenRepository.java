package com.atelier.gestion.auth.repository;
import com.atelier.gestion.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID>{
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from RefreshToken r join fetch r.usuario u join fetch u.rol where r.tokenHash=:hash") Optional<RefreshToken> buscarParaRotacion(@Param("hash") String hash);
  @Modifying @Query("update RefreshToken r set r.revocadoEn=:fecha where r.familiaId=:familia and r.revocadoEn is null") int revocarFamilia(@Param("familia") UUID familia,@Param("fecha") OffsetDateTime fecha);
}

