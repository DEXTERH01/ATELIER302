package com.atelier.gestion.propuesta.controller;

import static com.atelier.gestion.propuesta.dto.PropuestaDtos.*;

import com.atelier.gestion.propuesta.entity.TipoPropuesta;
import com.atelier.gestion.propuesta.service.PropuestaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class PropuestaController {
  private final PropuestaService service;
  public PropuestaController(PropuestaService service){this.service=service;}

  @GetMapping("/proyectos/{proyectoId}/propuestas")
  public List<PropuestaRespuesta> listar(@PathVariable UUID proyectoId){return service.listar(proyectoId);}

  @PostMapping(value="/proyectos/{proyectoId}/propuestas",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','ESPECIALISTA')")
  public ResponseEntity<PropuestaRespuesta> crear(@PathVariable UUID proyectoId,
      @RequestParam TipoPropuesta tipo,@RequestParam(required=false) @Size(max=5000) String descripcion,
      @RequestPart("archivo") MultipartFile archivo){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(proyectoId,tipo,descripcion,archivo));
  }

  @GetMapping("/propuestas/{id}")
  public PropuestaRespuesta obtener(@PathVariable UUID id){return service.obtener(id);}

  @PostMapping("/propuestas/{id}/enviar")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public PropuestaRespuesta enviar(@PathVariable UUID id){return service.enviar(id);}

  @PostMapping("/propuestas/{id}/respuesta")
  @PreAuthorize("hasRole('CLIENTE')")
  public PropuestaRespuesta responder(@PathVariable UUID id,@Valid @RequestBody Responder req){return service.responder(id,req);}

  @GetMapping("/propuestas/{id}/observaciones")
  public List<ObservacionRespuesta> observaciones(@PathVariable UUID id){return service.listarObservaciones(id);}

  @PostMapping("/propuestas/{id}/observaciones")
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<ObservacionRespuesta> observar(@PathVariable UUID id,
      @Valid @RequestBody CrearObservacion req){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarObservacion(id,req.descripcion()));
  }

  @PatchMapping("/observaciones-cliente/{id}")
  @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')")
  public ObservacionRespuesta actualizar(@PathVariable UUID id,@Valid @RequestBody ActualizarObservacion req){
    return service.actualizarObservacion(id,req);
  }
}
