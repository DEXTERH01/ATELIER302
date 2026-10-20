package com.atelier.gestion.common.storage;

import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CloudinaryArchivoStorage implements ArchivoStorage {
  private static final Logger log = LoggerFactory.getLogger(CloudinaryArchivoStorage.class);
  private final Cloudinary cloudinary;
  public CloudinaryArchivoStorage(Cloudinary cloudinary) { this.cloudinary = cloudinary; }

  @Override public ArchivoAlmacenado subir(MultipartFile archivo, String carpeta) {
    try {
      Map<?, ?> resultado = cloudinary.uploader().upload(archivo.getBytes(),
          ObjectUtils.asMap("folder", carpeta, "resource_type", "auto", "use_filename", true,
              "unique_filename", true, "overwrite", false));
      return new ArchivoAlmacenado(String.valueOf(resultado.get("secure_url")),
          String.valueOf(resultado.get("public_id")), String.valueOf(resultado.get("resource_type")),
          archivo.getContentType(), archivo.getSize());
    } catch (IOException | RuntimeException ex) {
      log.error("No se pudo cargar el comprobante en Cloudinary", ex);
      throw new ReglaNegocioException("No fue posible almacenar el comprobante");
    }
  }

  @Override public void eliminar(String publicId, String resourceType) {
    try {
      cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    } catch (IOException | RuntimeException ex) {
      log.error("Falló la compensación de Cloudinary para publicId={}", publicId, ex);
    }
  }
}
