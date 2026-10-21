package com.atelier.gestion.documento.dto;
import com.atelier.gestion.documento.entity.CategoriaDocumento;
import java.time.OffsetDateTime;
import java.util.UUID;
public final class DocumentoDtos{
  private DocumentoDtos(){}
  public record Respuesta(UUID id,UUID expedienteId,UUID proyectoId,UUID estudioId,UUID subidoPor,String autor,String nombre,String tipoDocumento,short version,String url,String publicId,String formato,String tipoMime,long tamanoBytes,CategoriaDocumento categoria,String resourceType,boolean visibleCliente,boolean vigente,OffsetDateTime creadoEn){}
}
