package com.atelier.gestion.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ArchivoStorage {
  ArchivoAlmacenado subir(MultipartFile archivo, String carpeta);
  void eliminar(String publicId, String resourceType);
}
