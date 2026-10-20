package com.atelier.gestion.proyecto.controller;

import static com.atelier.gestion.proyecto.dto.ProyectoDtos.*;

import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.proyecto.service.ProyectoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proyectos")
@PreAuthorize("isAuthenticated()")
public class ProyectoController {
  private final ProyectoService service;
  public ProyectoController(ProyectoService service) { this.service = service; }

  @GetMapping public PaginaRespuesta<Respuesta> listar(@RequestParam(defaultValue="") String q,
      @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="20") int tamano) {
    return service.listar(q, pagina, tamano);
  }
  @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')")
  public ResponseEntity<Respuesta> crear(@Valid @RequestBody Crear req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
  }
  @GetMapping("/{id}") public Respuesta obtener(@PathVariable UUID id) { return service.obtener(id); }
  @PatchMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')")
  public Respuesta actualizar(@PathVariable UUID id, @Valid @RequestBody Actualizar req) {
    return service.actualizar(id, req);
  }
  @PostMapping("/{id}/transiciones") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta transicionar(@PathVariable UUID id, @Valid @RequestBody Transicion req) {
    return service.transicionar(id, req);
  }
  @GetMapping("/{id}/historial") public List<HistorialRespuesta> historial(@PathVariable UUID id) {
    return service.historial(id);
  }
  @GetMapping("/{id}/requerimientos") public List<RequerimientoRespuesta> requerimientos(@PathVariable UUID id) {
    return service.listarRequerimientos(id);
  }
  @PostMapping("/{id}/requerimientos") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')")
  public ResponseEntity<RequerimientoRespuesta> crearRequerimiento(@PathVariable UUID id,
      @Valid @RequestBody RequerimientoCrear req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRequerimiento(id, req));
  }
}
