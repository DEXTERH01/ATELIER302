package com.atelier.gestion.expediente.controller;

import com.atelier.gestion.expediente.dto.ExpedienteDtos.Respuesta;
import com.atelier.gestion.expediente.service.ExpedienteService;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/expediente")
@PreAuthorize("isAuthenticated()")
public class ExpedienteController {
  private final ExpedienteService service;
  public ExpedienteController(ExpedienteService service){this.service=service;}
  @GetMapping public Respuesta obtener(@PathVariable UUID proyectoId){return service.obtenerPorProyecto(proyectoId);}
  @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ResponseEntity<Respuesta> crear(@PathVariable UUID proyectoId){return ResponseEntity.status(HttpStatus.CREATED).body(service.crearManual(proyectoId));}
  @PostMapping("/completar") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta completar(@PathVariable UUID proyectoId){return service.completar(proyectoId);}
}
