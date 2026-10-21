package com.atelier.gestion.expediente.repository;

import com.atelier.gestion.expediente.entity.Expediente;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ExpedienteRepository extends JpaRepository<Expediente,UUID> {
  @Override @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario"}) Optional<Expediente> findById(UUID id);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario"})
  @Query("select e from Expediente e where e.id=:id") Optional<Expediente> bloquear(UUID id);
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario"}) Optional<Expediente> findByProyectoId(UUID proyectoId);
}
