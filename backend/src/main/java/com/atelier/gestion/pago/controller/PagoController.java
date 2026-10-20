package com.atelier.gestion.pago.controller;

import static com.atelier.gestion.pago.dto.PagoDtos.*;

import com.atelier.gestion.pago.entity.*;
import com.atelier.gestion.pago.service.PagoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @Validated
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class PagoController {
  private final PagoService service;
  public PagoController(PagoService service){this.service=service;}

  @GetMapping("/cotizaciones/{cotizacionId}/pagos")
  public List<Respuesta> listar(@PathVariable UUID cotizacionId){return service.listar(cotizacionId);}

  @PostMapping(value="/cotizaciones/{cotizacionId}/pagos",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('CLIENTE')")
  public ResponseEntity<Respuesta> registrar(@PathVariable UUID cotizacionId,
      @RequestParam TipoPago tipo,@RequestParam MetodoPago metodo,
      @RequestParam @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal monto,
      @RequestParam(required=false) @Size(max=80) String numeroOperacion,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) OffsetDateTime pagadoEn,
      @RequestPart("archivo") MultipartFile archivo){
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(cotizacionId,tipo,metodo,monto,numeroOperacion,pagadoEn,archivo));
  }

  @GetMapping("/pagos") @PreAuthorize("hasRole('ADMINISTRADOR')")
  public List<Respuesta> pendientes(){return service.listarPendientes();}
  @PostMapping("/pagos/{id}/validar") @PreAuthorize("hasRole('ADMINISTRADOR')")
  public Respuesta validar(@PathVariable UUID id){return service.validar(id);}
  @PostMapping("/pagos/{id}/rechazar") @PreAuthorize("hasRole('ADMINISTRADOR')")
  public Respuesta rechazar(@PathVariable UUID id,@Valid @RequestBody Rechazar req){return service.rechazar(id,req);}
}
