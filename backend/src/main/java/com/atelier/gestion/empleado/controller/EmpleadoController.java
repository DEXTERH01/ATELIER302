package com.atelier.gestion.empleado.controller;

import static com.atelier.gestion.empleado.dto.EmpleadoDtos.*;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.empleado.service.EmpleadoService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/empleados") @PreAuthorize("isAuthenticated()")
public class EmpleadoController {
  private final EmpleadoService service;public EmpleadoController(EmpleadoService s){service=s;}
  @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public PaginaRespuesta<Respuesta> listar(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="20")int tamano){return service.listar(q,pagina,tamano);}
  @PostMapping @PreAuthorize("hasRole('ADMINISTRADOR')") public ResponseEntity<Respuesta> crear(@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));}
  @PatchMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
}
