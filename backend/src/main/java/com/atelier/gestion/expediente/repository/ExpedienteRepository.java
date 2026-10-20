package com.atelier.gestion.expediente.repository;

import com.atelier.gestion.expediente.entity.Expediente;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ExpedienteRepository extends JpaRepository<Expediente,UUID> {
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario"}) Optional<Expediente> findByProyectoId(UUID proyectoId);
}
