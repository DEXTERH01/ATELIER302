package com.atelier.gestion.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ArchivoStorage {
  ArchivoAlmacenado subir(MultipartFile archivo, String carpeta);
  boolean eliminar(String publicId, String resourceType);
}
