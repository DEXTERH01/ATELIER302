package com.atelier.gestion.estudio.repository;
import com.atelier.gestion.estudio.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface EstudioTecnicoRepository extends JpaRepository<EstudioTecnico,UUID>{
  @EntityGraph(attributePaths={"proyecto","responsable","responsable.usuario"}) List<EstudioTecnico> findByProyectoIdOrderByTipoAsc(UUID proyectoId);
  @Override @EntityGraph(attributePaths={"proyecto","proyecto.cliente","proyecto.cliente.usuario","responsable","responsable.usuario"}) Optional<EstudioTecnico> findById(UUID id);
  @EntityGraph(attributePaths={"proyecto","responsable","responsable.usuario"}) List<EstudioTecnico> findAllByOrderByFechaSolicitudDesc();
  long countByProyectoIdAndRequeridoTrueAndEstadoNot(UUID proyectoId,EstadoEstudio estado);
}
