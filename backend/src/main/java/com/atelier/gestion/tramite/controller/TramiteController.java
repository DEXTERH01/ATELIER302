package com.atelier.gestion.tramite.controller;

import static com.atelier.gestion.tramite.dto.TramiteDtos.*;

import com.atelier.gestion.tramite.service.TramiteService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1") @PreAuthorize("isAuthenticated()")
public class TramiteController {
  private final TramiteService service;public TramiteController(TramiteService service){this.service=service;}
  @GetMapping("/proyectos/{proyectoId}/tramites") public List<Respuesta> listar(@PathVariable UUID proyectoId){return service.listar(proyectoId);}
  @PostMapping("/proyectos/{proyectoId}/tramites") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ResponseEntity<Respuesta> crear(@PathVariable UUID proyectoId,@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(proyectoId,req));}
  @GetMapping("/tramites/{id}") public Respuesta obtener(@PathVariable UUID id){return service.obtener(id);}
  @PatchMapping("/tramites/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
  @GetMapping("/tramites/{id}/observaciones") public List<ObservacionRespuesta> observaciones(@PathVariable UUID id){return service.listarObservaciones(id);}
  @PostMapping("/tramites/{id}/observaciones") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ResponseEntity<ObservacionRespuesta> observar(@PathVariable UUID id,@Valid @RequestBody CrearObservacion req){return ResponseEntity.status(HttpStatus.CREATED).body(service.observar(id,req));}
  @PostMapping("/observaciones-entidad/{id}/subsanar") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ObservacionRespuesta subsanar(@PathVariable UUID id,@Valid @RequestBody Subsanar req){return service.subsanar(id,req);}
  @PostMapping("/tramites/{id}/resolver") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta resolver(@PathVariable UUID id,@Valid @RequestBody Resolver req){return service.resolver(id,req);}
}
