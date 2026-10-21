package com.atelier.gestion.tramite.repository;

import com.atelier.gestion.tramite.entity.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TramitePublicoRepository extends JpaRepository<TramitePublico,UUID> {
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","responsable","responsable.usuario"})
  List<TramitePublico> findByProyectoIdOrderByCreadoEnDesc(UUID proyectoId);
  @Override @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","responsable","responsable.usuario"})
  Optional<TramitePublico> findById(UUID id);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","responsable","responsable.usuario"})
  @Query("select t from TramitePublico t where t.id=:id") Optional<TramitePublico> bloquear(@Param("id")UUID id);
  boolean existsByProyectoIdAndEstadoIn(UUID proyectoId,Collection<EstadoTramite> estados);
}
