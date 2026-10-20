package com.atelier.gestion.proyecto.dto;

import com.atelier.gestion.proyecto.entity.EstadoProyecto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ProyectoDtos {
  private ProyectoDtos() {}
  public record Crear(UUID clienteId,UUID encargadoId,@NotBlank @Size(max=180) String nombre,@Size(max=5000) String descripcion,
    @NotBlank @Size(max=60) String tipoProyecto,@Size(max=250) String ubicacion,@DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal areaTerreno,
    boolean requiereTramitePublico,LocalDate fechaFinEstimada) {}
  public record Actualizar(@NotBlank @Size(max=180) String nombre,@Size(max=5000) String descripcion,@NotBlank @Size(max=60) String tipoProyecto,
    @Size(max=250) String ubicacion,@DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal areaTerreno,boolean requiereTramitePublico,
    UUID encargadoId,LocalDate fechaFinEstimada) {}
  public record Transicion(@NotNull EstadoProyecto estado,@Size(max=500) String comentario) {}
  public record Respuesta(UUID id,String codigo,UUID clienteId,String cliente,UUID encargadoId,String encargado,String nombre,String descripcion,String tipoProyecto,
    String ubicacion,BigDecimal areaTerreno,EstadoProyecto estado,boolean requiereTramitePublico,LocalDate fechaSolicitud,LocalDate fechaInicio,LocalDate fechaFinEstimada,LocalDate fechaCierre) {}
  public record RequerimientoCrear(@NotBlank @Size(max=60) String categoria,@NotBlank @Size(max=5000) String descripcion,
    @NotBlank @Pattern(regexp="BAJA|MEDIA|ALTA") String prioridad,boolean obligatorio,boolean completado) {}
  public record RequerimientoRespuesta(UUID id,String categoria,String descripcion,String prioridad,boolean obligatorio,boolean completado) {}
  public record HistorialRespuesta(Long id,EstadoProyecto estadoAnterior,EstadoProyecto estadoNuevo,UUID usuarioId,String responsable,String comentario,OffsetDateTime cambiadoEn) {}
  public record Dashboard(long totalProyectos,long requerimientosPendientes,long totalClientes,Map<EstadoProyecto,Long> porEstado) {}
}
