package com.atelier.gestion.cliente.controller;

import static com.atelier.gestion.cliente.dto.ClienteDtos.*;
import com.atelier.gestion.cliente.service.ClienteService;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.proyecto.dto.ProyectoDtos;
import com.atelier.gestion.proyecto.service.ProyectoService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/clientes")
public class ClienteController {
  private final ClienteService service;private final ProyectoService proyectos;public ClienteController(ClienteService s,ProyectoService p){service=s;proyectos=p;}
  @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public PaginaRespuesta<Respuesta> listar(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="20")int tamano){return service.listar(q,pagina,tamano);}
  @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO')") public ResponseEntity<Respuesta> crear(@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));}
  @GetMapping("/{id}") @PreAuthorize("isAuthenticated()") public Respuesta obtener(@PathVariable UUID id){return service.obtener(id);}
  @PatchMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
  @GetMapping("/{id}/proyectos") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','CLIENTE')") public PaginaRespuesta<ProyectoDtos.Respuesta> proyectos(@PathVariable UUID id,@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="20")int tamano){return proyectos.listarCliente(id,pagina,tamano);}
}
