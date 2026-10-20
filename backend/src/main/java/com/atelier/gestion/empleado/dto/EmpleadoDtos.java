package com.atelier.gestion.empleado.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class EmpleadoDtos {
  private EmpleadoDtos() {}
  public record Crear(@NotNull UUID usuarioId,@Size(max=40) String codigoColegiatura,@NotBlank @Size(max=80) String cargo,
    @NotBlank @Pattern(regexp="INTERNO|EXTERNO") String tipo,boolean disponible) {}
  public record Actualizar(@Size(max=40) String codigoColegiatura,@NotBlank @Size(max=80) String cargo,
    @NotBlank @Pattern(regexp="INTERNO|EXTERNO") String tipo,boolean disponible) {}
  public record Respuesta(UUID id,UUID usuarioId,String correo,String nombreCompleto,String rol,String codigoColegiatura,String cargo,String tipo,boolean disponible) {}
}
