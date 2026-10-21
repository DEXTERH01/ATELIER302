package com.atelier.gestion.asignacion.repository;
import com.atelier.gestion.asignacion.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface AsignacionRepository extends JpaRepository<Asignacion,UUID>{
  @EntityGraph(attributePaths={"proyecto","empleado","empleado.usuario","especialidad"}) List<Asignacion> findByProyectoIdOrderByFechaAsignacionDesc(UUID proyectoId);
  @Override @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","empleado","empleado.usuario","especialidad"}) Optional<Asignacion> findById(UUID id);
  boolean existsByProyectoIdAndEmpleadoIdAndEspecialidadId(UUID proyectoId,UUID empleadoId,Short especialidadId);
}
