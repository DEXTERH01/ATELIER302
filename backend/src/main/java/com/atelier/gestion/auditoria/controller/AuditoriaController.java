package com.atelier.gestion.auditoria.controller;

import com.atelier.gestion.auditoria.dto.AuditoriaDtos.Respuesta;
import com.atelier.gestion.auditoria.service.ConsultaAuditoriaService;
import com.atelier.gestion.common.api.PaginaRespuesta;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auditoria")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {
  private final ConsultaAuditoriaService service;
  public AuditoriaController(ConsultaAuditoriaService service) { this.service = service; }

  @GetMapping
  public PaginaRespuesta<Respuesta> listar(@RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "30") int tamano) {
    return service.listar(pagina, tamano);
  }
}
