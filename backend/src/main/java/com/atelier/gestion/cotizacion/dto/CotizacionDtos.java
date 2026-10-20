package com.atelier.gestion.cotizacion.dto;

import com.atelier.gestion.cotizacion.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public final class CotizacionDtos {
  private CotizacionDtos() {}
  public record DetalleEntrada(@NotBlank @Size(max=180) String concepto,@Size(max=500) String descripcion,
      @NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal cantidad,
      @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal precioUnitario) {}
  public record Guardar(@NotNull TipoCotizacion tipo,
      @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer=3,fraction=2) BigDecimal porcentajeImpuesto,
      @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer=3,fraction=2) BigDecimal porcentajeAdelanto,
      @Size(max=2000) String observacion,@NotEmpty @Size(max=100) List<@Valid DetalleEntrada> detalles) {}
  public record Responder(boolean aceptar,@Size(max=2000) String observacion) {}
  public record DetalleRespuesta(UUID id,String concepto,String descripcion,BigDecimal cantidad,BigDecimal precioUnitario,BigDecimal importe) {}
  public record Respuesta(UUID id,UUID proyectoId,String numero,short version,TipoCotizacion tipo,BigDecimal subtotal,
      BigDecimal impuesto,BigDecimal total,BigDecimal porcentajeAdelanto,BigDecimal adelantoRequerido,
      EstadoCotizacion estado,OffsetDateTime emitidaEn,OffsetDateTime venceEn,OffsetDateTime respondidaEn,
      String observacion,List<DetalleRespuesta> detalles) {}
}
