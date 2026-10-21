package com.atelier.gestion.reporte.controller;

import com.atelier.gestion.reporte.service.ReporteExcelService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
public class ReporteController {
  private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  private final ReporteExcelService service;
  public ReporteController(ReporteExcelService service) { this.service = service; }

  @GetMapping("/proyectos.xlsx") public ResponseEntity<byte[]> proyectos() { return archivo("proyectos-por-estado.xlsx", service.proyectosPorEstado()); }
  @GetMapping("/pagos.xlsx") public ResponseEntity<byte[]> pagos() { return archivo("pagos.xlsx", service.pagos()); }
  @GetMapping("/carga-tecnica.xlsx") public ResponseEntity<byte[]> cargaTecnica() { return archivo("carga-tecnica.xlsx", service.cargaTecnica()); }

  private ResponseEntity<byte[]> archivo(String nombre, byte[] contenido) {
    return ResponseEntity.ok().contentType(XLSX).header(HttpHeaders.CONTENT_DISPOSITION,
        ContentDisposition.attachment().filename(nombre).build().toString()).body(contenido);
  }
}
