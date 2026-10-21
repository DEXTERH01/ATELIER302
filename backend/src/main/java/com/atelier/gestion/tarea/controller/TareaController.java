package com.atelier.gestion.tarea.controller;
import static com.atelier.gestion.tarea.dto.TareaDtos.*;
import com.atelier.gestion.tarea.service.TareaService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @PreAuthorize("isAuthenticated()")
public class TareaController{
  private final TareaService service;public TareaController(TareaService s){service=s;}
  @GetMapping("/asignaciones/{asignacionId}/tareas") public List<Respuesta> listar(@PathVariable UUID asignacionId){return service.listar(asignacionId);}
  @PostMapping("/asignaciones/{asignacionId}/tareas") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public ResponseEntity<Respuesta> crear(@PathVariable UUID asignacionId,@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(asignacionId,req));}
  @PatchMapping("/tareas/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','ESPECIALISTA')") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
}
