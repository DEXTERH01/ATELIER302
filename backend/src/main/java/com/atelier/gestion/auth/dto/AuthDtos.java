package com.atelier.gestion.auth.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public final class AuthDtos {
  private AuthDtos() {}
  public record LoginRequest(@NotBlank @Email String correo,@NotBlank String password) {}
  public record RegisterClientRequest(
    @NotBlank @Email @Size(max=150) String correo,
    @NotBlank @Size(min=12,max=72) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$",message="debe incluir mayúscula, minúscula, número y símbolo") String password,
    @NotBlank @Size(max=100) String nombres,@NotBlank @Size(max=120) String apellidos,@Size(max=20) String telefono,
    @NotBlank @Pattern(regexp="NATURAL|JURIDICA") String tipoPersona,
    @NotBlank @Pattern(regexp="DNI|CE|RUC|PASAPORTE") String tipoDocumento,
    @NotBlank @Size(max=20) String numeroDocumento,@Size(max=180) String razonSocial,@Size(max=250) String direccion,@Size(max=100) String distrito) {}
  public record UsuarioResponse(UUID id,String correo,String nombreCompleto,String rol) {}
  public record AuthResponse(String accessToken,String tokenType,long expiresIn,UsuarioResponse usuario) {}
}
