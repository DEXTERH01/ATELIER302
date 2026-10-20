package com.atelier.gestion.usuario.controller;

import static com.atelier.gestion.usuario.dto.UsuarioDtos.*;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/usuarios") @PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {
  private final UsuarioService service; public UsuarioController(UsuarioService s){service=s;}
  @GetMapping public PaginaRespuesta<Respuesta> listar(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="20")int tamano){return service.listar(q,pagina,tamano);}
  @PostMapping public ResponseEntity<Respuesta> crear(@Valid @RequestBody Crear req){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));}
  @GetMapping("/{id}") public Respuesta obtener(@PathVariable UUID id){return service.obtener(id);}
  @PatchMapping("/{id}") public Respuesta actualizar(@PathVariable UUID id,@Valid @RequestBody Actualizar req){return service.actualizar(id,req);}
  @PatchMapping("/{id}/estado") public Respuesta estado(@PathVariable UUID id,@Valid @RequestBody CambiarEstado req){return service.estado(id,req);}
}
