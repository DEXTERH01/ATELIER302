package com.atelier.gestion.proyecto.repository;

import com.atelier.gestion.proyecto.entity.HistorialEstado;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado,Long> {
  @EntityGraph(attributePaths={"cambiadoPor"}) List<HistorialEstado> findByProyectoIdOrderByCambiadoEnDesc(UUID proyectoId);
}
