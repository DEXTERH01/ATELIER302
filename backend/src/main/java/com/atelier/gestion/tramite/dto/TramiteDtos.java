package com.atelier.gestion.tramite.dto;

import com.atelier.gestion.tramite.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.UUID;

public final class TramiteDtos {
  private TramiteDtos() {}
  public record Crear(@NotNull UUID responsableId,@NotBlank @Size(max=180) String entidad,
      @NotBlank @Size(max=150) String tipoTramite,@Size(max=80) String numeroExpedienteExterno,
      @Size(max=5000) String observacionGeneral) {}
  public record Actualizar(@NotNull UUID responsableId,@NotBlank @Size(max=180) String entidad,
      @NotBlank @Size(max=150) String tipoTramite,@Size(max=80) String numeroExpedienteExterno,
      @Size(max=5000) String observacionGeneral,EstadoTramite estado,LocalDate fechaPresentacion) {}
  public record Respuesta(UUID id,UUID proyectoId,UUID responsableId,String responsable,String entidad,
      String tipoTramite,String numeroExpedienteExterno,EstadoTramite estado,LocalDate fechaPresentacion,
      LocalDate fechaRespuesta,String observacionGeneral,OffsetDateTime creadoEn) {}
  public record CrearObservacion(@NotBlank @Size(max=5000) String descripcion,@NotNull LocalDate fechaNotificacion,
      LocalDate fechaLimite) {}
  public record Subsanar(@NotBlank @Size(max=5000) String respuesta,@NotNull LocalDate fechaSubsanacion) {}
  public record ObservacionRespuesta(UUID id,UUID tramiteId,short numero,String descripcion,
      LocalDate fechaNotificacion,LocalDate fechaLimite,EstadoObservacionEntidad estado,String respuesta,
      LocalDate fechaSubsanacion) {}
  public record Resolver(boolean aprobado,@NotNull LocalDate fechaRespuesta,
      @NotBlank @Size(max=5000) String observacionGeneral) {}
}
