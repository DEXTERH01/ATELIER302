package com.atelier.gestion.tarea.dto;
import com.atelier.gestion.tarea.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.UUID;
public final class TareaDtos{
  private TareaDtos(){}
  public record Crear(@NotBlank @Size(max=180) String titulo,@Size(max=5000) String descripcion,@NotNull PrioridadTarea prioridad,LocalDate fechaInicio,LocalDate fechaLimite){}
  public record Actualizar(@NotBlank @Size(max=180) String titulo,@Size(max=5000) String descripcion,@NotNull PrioridadTarea prioridad,@NotNull EstadoTarea estado,LocalDate fechaInicio,LocalDate fechaLimite){}
  public record Respuesta(UUID id,UUID asignacionId,UUID proyectoId,UUID empleadoId,String responsable,String especialidad,String titulo,String descripcion,PrioridadTarea prioridad,EstadoTarea estado,LocalDate fechaInicio,LocalDate fechaLimite,OffsetDateTime completadaEn,OffsetDateTime creadoEn){}
}
