package com.atelier.gestion.estudio.dto;
import com.atelier.gestion.estudio.entity.EstadoEstudio;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public final class EstudioDtos{
  private EstudioDtos(){}
  public record Crear(UUID responsableId,@NotBlank @Size(max=80) String tipo,@Size(max=180) String proveedor,boolean requerido,LocalDate fechaSolicitud,@DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal costo){}
  public record Actualizar(UUID responsableId,@NotBlank @Size(max=80) String tipo,@Size(max=180) String proveedor,boolean requerido,@NotNull EstadoEstudio estado,LocalDate fechaSolicitud,LocalDate fechaResultado,@Size(max=10000) String resultado,@DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal costo){}
  public record Respuesta(UUID id,UUID proyectoId,UUID responsableId,UUID responsableUsuarioId,String responsable,String tipo,String proveedor,boolean requerido,EstadoEstudio estado,LocalDate fechaSolicitud,LocalDate fechaResultado,String resultado,BigDecimal costo){}
}
