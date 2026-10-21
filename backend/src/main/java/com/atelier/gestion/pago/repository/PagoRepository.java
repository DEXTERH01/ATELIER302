package com.atelier.gestion.pago.repository;

import com.atelier.gestion.pago.entity.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends JpaRepository<Pago,UUID> {
  @EntityGraph(attributePaths={"cotizacion","cotizacion.proyecto","cotizacion.proyecto.cliente","cotizacion.proyecto.cliente.usuario","validadoPor","validadoPor.usuario"})
  List<Pago> findByCotizacionIdOrderByPagadoEnDesc(UUID cotizacionId);
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Pago p where p.id=:id") Optional<Pago> bloquear(@Param("id")UUID id);
  @Query("select coalesce(sum(p.monto),0) from Pago p where p.cotizacion.id=:cotizacionId and p.tipo=:tipo and p.estado=:estado")
  BigDecimal totalPorTipoYEstado(@Param("cotizacionId")UUID cotizacionId,
      @Param("tipo")TipoPago tipo,@Param("estado")EstadoPago estado);
  boolean existsByCotizacionProyectoIdAndEstado(UUID proyectoId,EstadoPago estado);
  @EntityGraph(attributePaths={"cotizacion","cotizacion.proyecto","cotizacion.proyecto.cliente","cotizacion.proyecto.cliente.usuario","validadoPor","validadoPor.usuario"})
  List<Pago> findByEstadoOrderByPagadoEnAsc(EstadoPago estado);
  @EntityGraph(attributePaths={"cotizacion","cotizacion.proyecto","cotizacion.proyecto.cliente","cotizacion.proyecto.cliente.usuario","validadoPor","validadoPor.usuario"})
  List<Pago> findAllByOrderByPagadoEnDesc();
}
