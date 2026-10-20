package com.atelier.gestion.cliente.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public final class ClienteDtos {
  private ClienteDtos() {}
  public record Crear(@NotBlank @Email @Size(max=150) String correo,
    @NotBlank @Size(min=12,max=72) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$") String password,
    @NotBlank @Size(max=100) String nombres,@NotBlank @Size(max=120) String apellidos,@Size(max=20) String telefono,
    @NotBlank @Pattern(regexp="NATURAL|JURIDICA") String tipoPersona,@NotBlank @Pattern(regexp="DNI|CE|RUC|PASAPORTE") String tipoDocumento,
    @NotBlank @Size(max=20) String numeroDocumento,@Size(max=180) String razonSocial,@Size(max=250) String direccion,@Size(max=100) String distrito) {}
  public record Actualizar(@NotBlank @Pattern(regexp="NATURAL|JURIDICA") String tipoPersona,@NotBlank @Pattern(regexp="DNI|CE|RUC|PASAPORTE") String tipoDocumento,
    @NotBlank @Size(max=20) String numeroDocumento,@Size(max=180) String razonSocial,@Size(max=250) String direccion,@Size(max=100) String distrito) {}
  public record Respuesta(UUID id,UUID usuarioId,String correo,String nombreCompleto,String tipoPersona,String tipoDocumento,String numeroDocumento,String razonSocial,String direccion,String distrito) {}
}
