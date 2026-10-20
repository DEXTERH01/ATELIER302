package com.atelier.gestion.auditoria.service;

import com.atelier.gestion.auditoria.entity.Auditoria;
import com.atelier.gestion.auditoria.repository.AuditoriaRepository;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
  private final AuditoriaRepository repository;

  public AuditoriaService(AuditoriaRepository repository) {
    this.repository = repository;
  }

  public void registrarLogin(Usuario usuario, String ip, String agente, String origen) {
    Map<String, Object> detalle = new LinkedHashMap<>();
    detalle.put("resultado", "EXITOSO");
    detalle.put("origen", origen);
    if (StringUtils.isNotBlank(ip)) detalle.put("ip", StringUtils.left(ip, 64));
    if (StringUtils.isNotBlank(agente)) detalle.put("userAgent", StringUtils.left(agente, 300));
    repository.save(new Auditoria(usuario, "usuarios", String.valueOf(usuario.getId()), "LOGIN", detalle));
  }
}
