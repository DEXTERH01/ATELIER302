package com.atelier.gestion.usuario.service;

import static com.atelier.gestion.usuario.dto.UsuarioDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.Usuario;
import com.atelier.gestion.usuario.repository.*;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
  private final UsuarioRepository usuarios; private final RolRepository roles; private final PasswordEncoder encoder;
  private final SesionActual sesion; private final AuditoriaService auditoria;
  public UsuarioService(UsuarioRepository u,RolRepository r,PasswordEncoder e,SesionActual s,AuditoriaService a){usuarios=u;roles=r;encoder=e;sesion=s;auditoria=a;}
  @Transactional(readOnly=true) public PaginaRespuesta<Respuesta> listar(String q,int pagina,int tamano){String t=StringUtils.trimToEmpty(q);return PaginaRespuesta.desde(usuarios.findByCorreoContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(t,t,t,PageRequest.of(pagina,Math.min(tamano,100),Sort.by("apellidos","nombres"))).map(this::dto));}
  @Transactional public Respuesta crear(Crear req){String correo=StringUtils.lowerCase(StringUtils.trim(req.correo()));if(usuarios.existsByCorreoIgnoreCase(correo))throw new ConflictoException("El correo ya está registrado");var rol=roles.findByNombre(req.rol()).orElseThrow(()->new ReglaNegocioException("Rol no configurado"));Usuario u=usuarios.save(new Usuario(rol,correo,encoder.encode(req.password()),StringUtils.trim(req.nombres()),StringUtils.trim(req.apellidos()),StringUtils.trimToNull(req.telefono())));auditoria.registrar(sesion.usuario(),"usuarios",u.getId(),"INSERT",Map.of("rol",req.rol()));return dto(u);}
  @Transactional(readOnly=true) public Respuesta obtener(UUID id){return dto(buscar(id));}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Usuario u=buscar(id);u.actualizar(StringUtils.trim(req.nombres()),StringUtils.trim(req.apellidos()),StringUtils.trimToNull(req.telefono()));auditoria.registrar(sesion.usuario(),"usuarios",id,"UPDATE",Map.of("campos","perfil"));return dto(u);}
  @Transactional public Respuesta estado(UUID id,CambiarEstado req){Usuario actual=sesion.usuario();if(actual.getId().equals(id)&&!req.activo())throw new ReglaNegocioException("No puede desactivar su propia cuenta");Usuario u=buscar(id);u.cambiarActivo(req.activo());auditoria.registrar(actual,"usuarios",id,"UPDATE",Map.of("activo",req.activo()));return dto(u);}
  private Usuario buscar(UUID id){return usuarios.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Usuario no encontrado"));}
  private Respuesta dto(Usuario u){return new Respuesta(u.getId(),u.getCorreo(),u.getNombres(),u.getApellidos(),u.getTelefono(),u.getRol().getNombre(),u.isActivo(),u.getUltimoAcceso());}
}
