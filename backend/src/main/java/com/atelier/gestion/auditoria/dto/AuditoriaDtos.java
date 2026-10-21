package com.atelier.gestion.auditoria.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public final class AuditoriaDtos {
  private AuditoriaDtos() {}

  public record Respuesta(Long id, String usuario, String correo, String tablaAfectada,
      String idRegistro, String accion, Map<String, Object> detalle, OffsetDateTime creadoEn) {}
}
