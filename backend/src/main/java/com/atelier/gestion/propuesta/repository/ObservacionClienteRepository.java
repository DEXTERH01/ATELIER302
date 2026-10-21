package com.atelier.gestion.propuesta.repository;

import com.atelier.gestion.propuesta.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ObservacionClienteRepository extends JpaRepository<ObservacionCliente,UUID> {
  @EntityGraph(attributePaths={"propuesta","propuesta.proyecto","propuesta.proyecto.cliente","propuesta.proyecto.cliente.usuario","cliente","cliente.usuario","cotizacionAdicional"})
  List<ObservacionCliente> findByPropuestaIdOrderByNumeroRondaAsc(UUID propuestaId);
  @Override
  @EntityGraph(attributePaths={"propuesta","propuesta.proyecto","propuesta.proyecto.cliente","propuesta.proyecto.cliente.usuario","cliente","cliente.usuario","cotizacionAdicional"})
  Optional<ObservacionCliente> findById(UUID id);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths={"propuesta","propuesta.proyecto","propuesta.proyecto.cliente","propuesta.proyecto.cliente.usuario","cliente","cliente.usuario","cotizacionAdicional"})
  @Query("select o from ObservacionCliente o where o.id=:id") Optional<ObservacionCliente> bloquear(@Param("id")UUID id);
  @Query("select coalesce(max(o.numeroRonda),0) from ObservacionCliente o where o.propuesta.proyecto.id=:proyectoId and o.propuesta.tipo=:tipo")
  short maxRonda(@Param("proyectoId")UUID proyectoId,@Param("tipo")TipoPropuesta tipo);
  @EntityGraph(attributePaths={"propuesta","propuesta.proyecto","cotizacionAdicional"})
  Optional<ObservacionCliente> findByCotizacionAdicionalId(UUID cotizacionId);
  boolean existsByCotizacionAdicionalId(UUID cotizacionId);
  @EntityGraph(attributePaths={"propuesta","propuesta.proyecto","cotizacionAdicional"})
  List<ObservacionCliente> findByPropuestaProyectoIdAndPropuestaTipoAndEstado(
      UUID proyectoId,TipoPropuesta tipo,EstadoObservacionCliente estado);
}
