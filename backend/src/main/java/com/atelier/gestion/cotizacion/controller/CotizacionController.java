package com.atelier.gestion.cotizacion.controller;

import static com.atelier.gestion.cotizacion.dto.CotizacionDtos.*;

import com.atelier.gestion.cotizacion.service.CotizacionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@PreAuthorize("isAuthenticated()")
public class CotizacionController {
  private final CotizacionService service;
  public CotizacionController(CotizacionService service){this.service=service;}

  @GetMapping("/{id}") public Respuesta obtener(@PathVariable UUID id){return service.obtener(id);}
  @PatchMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Guardar req){return service.actualizar(id,req);}
  @PostMapping("/{id}/enviar") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta enviar(@PathVariable UUID id){return service.enviar(id);}
  @PostMapping("/{id}/responder") @PreAuthorize("hasRole('CLIENTE')")
  public Respuesta responder(@PathVariable UUID id,@Valid @RequestBody Responder req){return service.responder(id,req);}
  @PostMapping("/{id}/renovar") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public Respuesta renovar(@PathVariable UUID id){return service.renovar(id);}
}
