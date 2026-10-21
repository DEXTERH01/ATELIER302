package com.atelier.gestion.auditoria.service;

import static com.atelier.gestion.auditoria.dto.AuditoriaDtos.*;

import com.atelier.gestion.auditoria.entity.Auditoria;
import com.atelier.gestion.auditoria.repository.AuditoriaRepository;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaAuditoriaService {
  private final AuditoriaRepository auditoria;

  public ConsultaAuditoriaService(AuditoriaRepository auditoria) { this.auditoria = auditoria; }

  @Transactional(readOnly = true)
  public PaginaRespuesta<Respuesta> listar(int pagina, int tamano) {
    if (pagina < 0 || tamano < 1) throw new ReglaNegocioException("Paginación inválida");
    return PaginaRespuesta.desde(auditoria.findAllByOrderByCreadoEnDesc(
        PageRequest.of(pagina, Math.min(tamano, 100), Sort.by(Sort.Direction.DESC, "creadoEn")))
        .map(this::dto));
  }

  private Respuesta dto(Auditoria a) {
    String nombre = a.getUsuario() == null ? "Sistema" : a.getUsuario().getNombres() + " " + a.getUsuario().getApellidos();
    String correo = a.getUsuario() == null ? null : a.getUsuario().getCorreo();
    return new Respuesta(a.getId(), nombre, correo, a.getTablaAfectada(), a.getIdRegistro(),
        a.getAccion(), a.getDetalle(), a.getCreadoEn());
  }
}
