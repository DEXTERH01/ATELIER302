package com.atelier.gestion.proyecto.controller;

import com.atelier.gestion.proyecto.dto.ProyectoDtos.Dashboard;
import com.atelier.gestion.proyecto.service.ProyectoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {
  private final ProyectoService service;
  public DashboardController(ProyectoService service) { this.service = service; }
  @GetMapping("/resumen") public Dashboard resumen() { return service.dashboard(); }
}
