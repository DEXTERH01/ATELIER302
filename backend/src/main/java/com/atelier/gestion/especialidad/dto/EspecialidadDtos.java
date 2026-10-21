package com.atelier.gestion.especialidad.dto;
import jakarta.validation.constraints.*;
public final class EspecialidadDtos{
  private EspecialidadDtos(){}
  public record Crear(@NotBlank @Size(max=80) String nombre,@Size(max=250) String descripcion){}
  public record Actualizar(@NotBlank @Size(max=80) String nombre,@Size(max=250) String descripcion,boolean activa){}
  public record Respuesta(Short id,String nombre,String descripcion,boolean activa){}
}
