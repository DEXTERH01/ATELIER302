package com.atelier.gestion.especialidad.controller;
import static com.atelier.gestion.especialidad.dto.EspecialidadDtos.*;
import com.atelier.gestion.especialidad.service.EspecialidadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/especialidades") @PreAuthorize("isAuthenticated()")
public class EspecialidadController{
  private final EspecialidadService service;public EspecialidadController(EspecialidadService s){service=s;}
  @GetMapping public List<Respuesta> listar(){return service.listar();}
  @PostMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public ResponseEntity<Respuesta> crear(@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));}
  @PatchMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')") public Respuesta actualizar(@PathVariable Short id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
}
