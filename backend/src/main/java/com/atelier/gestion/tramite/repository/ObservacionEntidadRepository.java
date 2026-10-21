package com.atelier.gestion.tramite.repository;

import com.atelier.gestion.tramite.entity.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ObservacionEntidadRepository extends JpaRepository<ObservacionEntidad,UUID> {
  @Override
  @EntityGraph(attributePaths={"tramite","tramite.proyecto","tramite.proyecto.cliente","tramite.proyecto.cliente.usuario"})
  Optional<ObservacionEntidad> findById(UUID id);
  @EntityGraph(attributePaths={"tramite","tramite.proyecto","tramite.proyecto.cliente","tramite.proyecto.cliente.usuario"})
  List<ObservacionEntidad> findByTramiteIdOrderByNumeroAsc(UUID tramiteId);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths={"tramite","tramite.proyecto","tramite.proyecto.cliente","tramite.proyecto.cliente.usuario"})
  @Query("select o from ObservacionEntidad o where o.id=:id") Optional<ObservacionEntidad> bloquear(@Param("id")UUID id);
  @Query("select coalesce(max(o.numero),0) from ObservacionEntidad o where o.tramite.id=:tramiteId") short maxNumero(@Param("tramiteId")UUID tramiteId);
  long countByTramiteIdAndEstado(UUID tramiteId,EstadoObservacionEntidad estado);
}
