package com.atelier.gestion.proyecto.controller;

import static com.atelier.gestion.proyecto.dto.ProyectoDtos.*;

import com.atelier.gestion.proyecto.service.ProyectoService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requerimientos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')")
public class RequerimientoController {
  private final ProyectoService service;
  public RequerimientoController(ProyectoService service) { this.service = service; }

  @PatchMapping("/{id}")
  public RequerimientoRespuesta actualizar(@PathVariable UUID id,
      @Valid @RequestBody RequerimientoCrear req) {
    return service.actualizarRequerimiento(id, req);
  }
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
    service.eliminarRequerimiento(id);
    return ResponseEntity.noContent().build();
  }
}
