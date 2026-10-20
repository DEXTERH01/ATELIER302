package com.atelier.gestion.expediente.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ExpedienteDtos {
  private ExpedienteDtos() {}
  public record Respuesta(UUID id,UUID proyectoId,String codigo,String estado,OffsetDateTime creadoEn,OffsetDateTime cerradoEn) {}
}
