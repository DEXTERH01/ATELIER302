package com.atelier.gestion.propuesta.dto;

import com.atelier.gestion.propuesta.entity.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class PropuestaDtos {
  private PropuestaDtos() {}
  public record Responder(boolean conforme,@Size(max=5000) String observacion) {}
  public record CrearObservacion(@NotBlank @Size(max=5000) String descripcion) {}
  public record ActualizarObservacion(@NotNull EstadoObservacionCliente estado,boolean adicional) {}
  public record PropuestaRespuesta(UUID id,UUID proyectoId,UUID creadaPorId,String creador,
      short numeroVersion,TipoPropuesta tipo,String descripcion,String vistaPreviaUrl,
      String vistaPreviaPublicId,String vistaPreviaNombre,String vistaPreviaTipoMime,
      Long vistaPreviaTamanoBytes,String vistaPreviaResourceType,EstadoPropuesta estado,
      OffsetDateTime enviadaEn,OffsetDateTime respondidaEn,OffsetDateTime creadoEn) {}
  public record ObservacionRespuesta(UUID id,UUID propuestaId,UUID clienteId,short numeroRonda,
      String descripcion,EstadoObservacionCliente estado,boolean adicional,boolean requiereCotizacionAdicional,
      UUID cotizacionAdicionalId,OffsetDateTime creadaEn,OffsetDateTime atendidaEn) {}
}
