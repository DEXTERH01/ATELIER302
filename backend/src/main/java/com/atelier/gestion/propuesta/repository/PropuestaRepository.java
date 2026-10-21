package com.atelier.gestion.propuesta.repository;

import com.atelier.gestion.propuesta.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PropuestaRepository extends JpaRepository<Propuesta,UUID> {
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","creadaPor","creadaPor.usuario"})
  List<Propuesta> findByProyectoIdOrderByCreadoEnDesc(UUID proyectoId);
  @Override
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","creadaPor","creadaPor.usuario"})
  Optional<Propuesta> findById(UUID id);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","creadaPor","creadaPor.usuario"})
  @Query("select p from Propuesta p where p.id=:id") Optional<Propuesta> bloquear(@Param("id")UUID id);
  @Query("select coalesce(max(p.numeroVersion),0) from Propuesta p where p.proyecto.id=:proyectoId and p.tipo=:tipo")
  short maxVersion(@Param("proyectoId")UUID proyectoId,@Param("tipo")TipoPropuesta tipo);
  boolean existsByProyectoIdAndTipoAndEstado(UUID proyectoId,TipoPropuesta tipo,EstadoPropuesta estado);
  List<Propuesta> findByProyectoIdAndTipoAndEstado(UUID proyectoId,TipoPropuesta tipo,EstadoPropuesta estado);
}
