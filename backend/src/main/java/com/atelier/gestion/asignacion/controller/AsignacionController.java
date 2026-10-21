package com.atelier.gestion.asignacion.controller;
import static com.atelier.gestion.asignacion.dto.AsignacionDtos.*;
import com.atelier.gestion.asignacion.service.AsignacionService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @PreAuthorize("isAuthenticated()")
public class AsignacionController{
  private final AsignacionService service;public AsignacionController(AsignacionService s){service=s;}
  @GetMapping("/proyectos/{proyectoId}/asignaciones") public List<Respuesta> listar(@PathVariable UUID proyectoId){return service.listar(proyectoId);}
  @PostMapping("/proyectos/{proyectoId}/asignaciones") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public ResponseEntity<Respuesta> crear(@PathVariable UUID proyectoId,@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(proyectoId,req));}
  @PatchMapping("/asignaciones/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
}
