package com.atelier.gestion.proyecto.repository;

import com.atelier.gestion.proyecto.entity.Requerimiento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequerimientoRepository extends JpaRepository<Requerimiento,UUID> {
  List<Requerimiento> findByProyectoIdOrderByObligatorioDescPrioridadDesc(UUID proyectoId);
  @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario"})
  @Query("select r from Requerimiento r where r.id=:id")
  Optional<Requerimiento> findWithProyectoById(@org.springframework.data.repository.query.Param("id") UUID id);
  long countByProyectoIdAndObligatorioTrue(UUID proyectoId);
  long countByProyectoIdAndObligatorioTrueAndCompletadoFalse(UUID proyectoId);
  @Query("select count(r) from Requerimiento r where r.obligatorio=true and r.completado=false") long countPendientes();
  @Query("select count(r) from Requerimiento r where r.obligatorio=true and r.completado=false and lower(r.proyecto.cliente.usuario.correo)=lower(:correo)") long countPendientesCliente(@org.springframework.data.repository.query.Param("correo")String correo);
  @Query("select count(distinct r) from Requerimiento r join Asignacion a on a.proyecto=r.proyecto where r.obligatorio=true and r.completado=false and lower(a.empleado.usuario.correo)=lower(:correo)") long countPendientesEspecialista(@org.springframework.data.repository.query.Param("correo")String correo);
}
