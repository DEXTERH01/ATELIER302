package com.atelier.gestion.empleado.repository;

import com.atelier.gestion.empleado.entity.Empleado;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado,UUID> {
  boolean existsByUsuarioId(UUID usuarioId);
  @Override @EntityGraph(attributePaths={"usuario","usuario.rol"}) Optional<Empleado> findById(UUID id);
  @EntityGraph(attributePaths={"usuario","usuario.rol"}) Optional<Empleado> findByUsuarioCorreoIgnoreCase(String correo);
  @EntityGraph(attributePaths={"usuario","usuario.rol"}) Page<Empleado> findByCargoContainingIgnoreCase(String cargo,Pageable pageable);
}
