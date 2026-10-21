package com.atelier.gestion.documento.controller;
import static com.atelier.gestion.documento.dto.DocumentoDtos.*;
import com.atelier.gestion.documento.entity.CategoriaDocumento;
import com.atelier.gestion.documento.service.DocumentoService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/v1") @PreAuthorize("isAuthenticated()")
public class DocumentoController{
  private final DocumentoService service;public DocumentoController(DocumentoService s){service=s;}
  @GetMapping("/expedientes/{expedienteId}/documentos") public List<Respuesta> listar(@PathVariable UUID expedienteId){return service.listar(expedienteId);}
  @PostMapping(value="/expedientes/{expedienteId}/documentos",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','ESPECIALISTA')") public ResponseEntity<Respuesta> subir(@PathVariable UUID expedienteId,@RequestParam String tipoDocumento,@RequestParam CategoriaDocumento categoria,@RequestParam(required=false)UUID estudioId,@RequestParam(required=false)UUID tramiteId,@RequestParam(defaultValue="false")boolean visibleCliente,@RequestPart("archivo")MultipartFile archivo){return ResponseEntity.status(HttpStatus.CREATED).body(service.subir(expedienteId,tipoDocumento,categoria,estudioId,tramiteId,visibleCliente,archivo));}
  @GetMapping("/documentos/{id}") public Respuesta obtener(@PathVariable UUID id){return service.obtener(id);}
  @DeleteMapping("/documentos/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','ENCARGADO','ESPECIALISTA')") public ResponseEntity<Void> eliminar(@PathVariable UUID id){service.eliminar(id);return ResponseEntity.noContent().build();}
}
