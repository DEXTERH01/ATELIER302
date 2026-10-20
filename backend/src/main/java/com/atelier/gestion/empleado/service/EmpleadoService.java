package com.atelier.gestion.empleado.service;

import static com.atelier.gestion.empleado.dto.EmpleadoDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoService {
  private final EmpleadoRepository empleados;private final UsuarioRepository usuarios;private final SesionActual sesion;private final AuditoriaService auditoria;
  public EmpleadoService(EmpleadoRepository e,UsuarioRepository u,SesionActual s,AuditoriaService a){empleados=e;usuarios=u;sesion=s;auditoria=a;}
  @Transactional(readOnly=true) public PaginaRespuesta<Respuesta> listar(String q,int pagina,int tamano){return PaginaRespuesta.desde(empleados.findByCargoContainingIgnoreCase(StringUtils.trimToEmpty(q),PageRequest.of(pagina,Math.min(tamano,100),Sort.by("cargo"))).map(this::dto));}
  @Transactional public Respuesta crear(Crear req){if(empleados.existsByUsuarioId(req.usuarioId()))throw new ConflictoException("El usuario ya tiene perfil de empleado");var u=usuarios.findById(req.usuarioId()).orElseThrow(()->new RecursoNoEncontradoException("Usuario no encontrado"));if("CLIENTE".equals(u.getRol().getNombre()))throw new ReglaNegocioException("Un usuario CLIENTE no puede ser empleado");Empleado e=empleados.save(new Empleado(u,StringUtils.trimToNull(req.codigoColegiatura()),StringUtils.trim(req.cargo()),req.tipo(),req.disponible()));auditoria.registrar(sesion.usuario(),"empleados",e.getId(),"INSERT",Map.of());return dto(e);}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Empleado e=buscar(id);e.actualizar(StringUtils.trimToNull(req.codigoColegiatura()),StringUtils.trim(req.cargo()),req.tipo(),req.disponible());auditoria.registrar(sesion.usuario(),"empleados",id,"UPDATE",Map.of());return dto(e);}
  public Empleado buscar(UUID id){return empleados.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Empleado no encontrado"));}
  private Respuesta dto(Empleado e){var u=e.getUsuario();return new Respuesta(e.getId(),u.getId(),u.getCorreo(),u.getNombres()+" "+u.getApellidos(),u.getRol().getNombre(),e.getCodigoColegiatura(),e.getCargo(),e.getTipo(),e.isDisponible());}
}
