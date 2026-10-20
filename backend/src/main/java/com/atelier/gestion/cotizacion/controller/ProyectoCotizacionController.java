package com.atelier.gestion.cotizacion.controller;

import static com.atelier.gestion.cotizacion.dto.CotizacionDtos.*;

import com.atelier.gestion.cotizacion.service.CotizacionService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/cotizaciones")
@PreAuthorize("isAuthenticated()")
public class ProyectoCotizacionController {
  private final CotizacionService service;
  public ProyectoCotizacionController(CotizacionService service){this.service=service;}
  @GetMapping public List<Respuesta> listar(@PathVariable UUID proyectoId){return service.listar(proyectoId);}
  @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ResponseEntity<Respuesta> crear(@PathVariable UUID proyectoId,@Valid @RequestBody Guardar req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(proyectoId,req));}
}
