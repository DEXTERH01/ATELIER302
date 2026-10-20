package com.atelier.gestion.pago.dto;

import com.atelier.gestion.pago.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class PagoDtos {
  private PagoDtos() {}
  public record Rechazar(@NotBlank @Size(max=500) String motivo) {}
  public record Respuesta(UUID id,UUID cotizacionId,TipoPago tipo,MetodoPago metodo,BigDecimal monto,String moneda,
      String numeroOperacion,String comprobanteUrl,String comprobantePublicId,String comprobanteTipoMime,
      Long comprobanteTamanoBytes,String comprobanteCategoria,EstadoPago estado,String motivoRechazo,
      UUID validadoPor,String validador,OffsetDateTime pagadoEn,OffsetDateTime validadoEn) {}
}
