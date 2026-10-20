package com.atelier.gestion.common.api;
import java.util.List;
import org.springframework.data.domain.Page;
public record PaginaRespuesta<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
  public static <T> PaginaRespuesta<T> desde(Page<T> p) { return new PaginaRespuesta<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages()); }
}

