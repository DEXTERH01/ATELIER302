package com.atelier.gestion.tarea.repository;
import com.atelier.gestion.tarea.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface TareaRepository extends JpaRepository<Tarea,UUID>{
  @EntityGraph(attributePaths={"asignacion","asignacion.proyecto","asignacion.empleado","asignacion.empleado.usuario","asignacion.especialidad"}) List<Tarea> findByAsignacionIdOrderByFechaLimiteAsc(UUID asignacionId);
  @Override @EntityGraph(attributePaths={"asignacion","asignacion.proyecto","asignacion.proyecto.cliente","asignacion.proyecto.cliente.usuario","asignacion.empleado","asignacion.empleado.usuario","asignacion.especialidad"}) Optional<Tarea> findById(UUID id);
  long countByAsignacionIdAndEstadoNotIn(UUID asignacionId,Collection<EstadoTarea> estados);
}
