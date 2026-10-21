package com.atelier.gestion.estudio.controller;
import static com.atelier.gestion.estudio.dto.EstudioDtos.*;
import com.atelier.gestion.estudio.service.EstudioService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1") @PreAuthorize("isAuthenticated()")
public class EstudioController{
  private final EstudioService service;public EstudioController(EstudioService s){service=s;}
  @GetMapping("/proyectos/{proyectoId}/estudios") public List<Respuesta> listar(@PathVariable UUID proyectoId){return service.listar(proyectoId);}
  @PostMapping("/proyectos/{proyectoId}/estudios") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public ResponseEntity<Respuesta> crear(@PathVariable UUID proyectoId,@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(proyectoId,req));}
  @PatchMapping("/estudios/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','ESPECIALISTA')") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
}
