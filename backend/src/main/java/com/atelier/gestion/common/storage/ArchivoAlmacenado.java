package com.atelier.gestion.common.storage;

public record ArchivoAlmacenado(String secureUrl, String publicId, String resourceType, String tipoMime, long tamanoBytes) {}
