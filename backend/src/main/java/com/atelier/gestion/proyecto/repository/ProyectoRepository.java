package com.atelier.gestion.proyecto.repository;

import com.atelier.gestion.proyecto.entity.Proyecto;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProyectoRepository extends JpaRepository<Proyecto,UUID> {
  @Override @EntityGraph(attributePaths={"cliente","cliente.usuario","encargado","encargado.usuario"}) Optional<Proyecto> findById(UUID id);
  @EntityGraph(attributePaths={"cliente","cliente.usuario","encargado","encargado.usuario"}) Page<Proyecto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre,String codigo,Pageable pageable);
  @EntityGraph(attributePaths={"cliente","cliente.usuario","encargado","encargado.usuario"}) Page<Proyecto> findByClienteId(UUID clienteId,Pageable pageable);
  @EntityGraph(attributePaths={"cliente","cliente.usuario","encargado","encargado.usuario"})
  @Query("select p from Proyecto p where lower(p.cliente.usuario.correo)=lower(:correo) and (lower(p.nombre) like lower(concat('%',:texto,'%')) or lower(p.codigo) like lower(concat('%',:texto,'%')))")
  Page<Proyecto> buscarDelCliente(@Param("correo") String correo,@Param("texto") String texto,Pageable pageable);
  @EntityGraph(attributePaths={"cliente","cliente.usuario","encargado","encargado.usuario"})
  @Query("select distinct a.proyecto from Asignacion a join a.empleado e join e.usuario u where lower(u.correo)=lower(:correo) and (lower(a.proyecto.nombre) like lower(concat('%',:texto,'%')) or lower(a.proyecto.codigo) like lower(concat('%',:texto,'%')))")
  Page<Proyecto> buscarAsignados(@Param("correo") String correo,@Param("texto") String texto,Pageable pageable);
  boolean existsByIdAndClienteUsuarioCorreoIgnoreCase(UUID id,String correo);
  @Query("select (count(a)>0) from Asignacion a where a.proyecto.id=:id and lower(a.empleado.usuario.correo)=lower(:correo)") boolean asignadoA(@Param("id")UUID id,@Param("correo")String correo);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select p from Proyecto p where p.id=:id") Optional<Proyecto> bloquear(@Param("id")UUID id);
  @Query("select count(p) from Proyecto p where lower(p.cliente.usuario.correo)=lower(:correo)") long countCliente(@Param("correo") String correo);
  @Query("select p.estado,count(p) from Proyecto p group by p.estado") java.util.List<Object[]> contarPorEstado();
  @Query("select p.estado,count(p) from Proyecto p where lower(p.cliente.usuario.correo)=lower(:correo) group by p.estado") java.util.List<Object[]> contarPorEstadoCliente(@Param("correo")String correo);
  @Query("select a.proyecto.estado,count(distinct a.proyecto) from Asignacion a where lower(a.empleado.usuario.correo)=lower(:correo) group by a.proyecto.estado") java.util.List<Object[]> contarPorEstadoEspecialista(@Param("correo")String correo);
}
