package com.atelier.gestion.cotizacion.repository;

import com.atelier.gestion.cotizacion.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CotizacionRepository extends JpaRepository<Cotizacion,UUID> {
  @EntityGraph(attributePaths={"detalles","proyecto","proyecto.cliente","proyecto.cliente.usuario","creadaPor","creadaPor.usuario"})
  List<Cotizacion> findByProyectoIdOrderByVersionDesc(UUID proyectoId);
  @EntityGraph(attributePaths={"detalles","proyecto","proyecto.cliente","proyecto.cliente.usuario","creadaPor","creadaPor.usuario"})
  @Query("select c from Cotizacion c where c.id=:id") Optional<Cotizacion> findDetailedById(@Param("id")UUID id);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Cotizacion c where c.id=:id") Optional<Cotizacion> bloquear(@Param("id")UUID id);
  @Query("select coalesce(max(c.version),0) from Cotizacion c where c.proyecto.id=:proyectoId and c.tipo=:tipo")
  short maxVersion(@Param("proyectoId")UUID proyectoId,@Param("tipo")TipoCotizacion tipo);
}
