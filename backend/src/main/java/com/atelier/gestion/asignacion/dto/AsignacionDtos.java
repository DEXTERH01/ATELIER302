package com.atelier.gestion.asignacion.dto;
import com.atelier.gestion.asignacion.entity.EstadoAsignacion;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
public final class AsignacionDtos{
  private AsignacionDtos(){}
  public record Crear(@NotNull UUID empleadoId,@NotNull Short especialidadId,@NotBlank @Size(max=80) String rolProyecto,LocalDate fechaAsignacion){}
  public record Actualizar(@NotBlank @Size(max=80) String rolProyecto,@NotNull EstadoAsignacion estado,LocalDate fechaFin){}
  public record Respuesta(UUID id,UUID proyectoId,UUID empleadoId,UUID empleadoUsuarioId,String empleado,String cargo,Short especialidadId,String especialidad,String rolProyecto,LocalDate fechaAsignacion,LocalDate fechaFin,EstadoAsignacion estado){}
}
