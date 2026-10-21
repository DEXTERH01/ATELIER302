package com.atelier.gestion.common.storage;

import com.atelier.gestion.common.exception.ArchivoInvalidoException;
import com.atelier.gestion.config.AtelierProperties;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ValidadorArchivo {
  private static final Map<String, Set<String>> TIPOS = Map.of(
      "pdf", Set.of("application/pdf"),
      "jpg", Set.of("image/jpeg"),
      "jpeg", Set.of("image/jpeg"),
      "png", Set.of("image/png"));
  private final AtelierProperties propiedades;
  public ValidadorArchivo(AtelierProperties propiedades) { this.propiedades = propiedades; }

  public void validar(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) throw new ArchivoInvalidoException("El archivo es obligatorio");
    if (archivo.getSize() > propiedades.files().maxUploadBytes())
      throw new ArchivoInvalidoException("El archivo supera el tamaño máximo permitido");
    String extension = StringUtils.lowerCase(FilenameUtils.getExtension(archivo.getOriginalFilename()));
    String mime = StringUtils.lowerCase(archivo.getContentType());
    if (!TIPOS.containsKey(extension) || !TIPOS.get(extension).contains(mime))
      throw new ArchivoInvalidoException("Solo se permiten archivos PDF, JPG, JPEG o PNG con MIME válido");
  }
}
