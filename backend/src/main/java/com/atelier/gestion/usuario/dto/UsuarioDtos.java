package com.atelier.gestion.usuario.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class UsuarioDtos {
  private UsuarioDtos() {}
  public record Crear(@NotBlank @Email @Size(max=150) String correo,
    @NotBlank @Size(min=12,max=72) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$") String password,
    @NotBlank @Size(max=100) String nombres,@NotBlank @Size(max=120) String apellidos,@Size(max=20) String telefono,
    @NotBlank @Pattern(regexp="ADMINISTRADOR|ENCARGADO|ESPECIALISTA") String rol) {}
  public record Actualizar(@NotBlank @Size(max=100) String nombres,@NotBlank @Size(max=120) String apellidos,@Size(max=20) String telefono) {}
  public record CambiarEstado(boolean activo) {}
  public record Respuesta(UUID id,String correo,String nombres,String apellidos,String telefono,String rol,boolean activo,OffsetDateTime ultimoAcceso) {}
}
