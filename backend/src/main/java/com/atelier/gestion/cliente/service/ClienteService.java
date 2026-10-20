package com.atelier.gestion.cliente.service;

import static com.atelier.gestion.cliente.dto.ClienteDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.Usuario;
import com.atelier.gestion.usuario.repository.*;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
  private final ClienteRepository clientes;private final UsuarioRepository usuarios;private final RolRepository roles;private final PasswordEncoder encoder;private final SesionActual sesion;private final AuditoriaService auditoria;
  public ClienteService(ClienteRepository c,UsuarioRepository u,RolRepository r,PasswordEncoder e,SesionActual s,AuditoriaService a){clientes=c;usuarios=u;roles=r;encoder=e;sesion=s;auditoria=a;}
  @Transactional(readOnly=true) public PaginaRespuesta<Respuesta> listar(String q,int pagina,int tamano){String t=StringUtils.trimToEmpty(q);return PaginaRespuesta.desde(clientes.findByNumeroDocumentoContainingIgnoreCaseOrUsuarioCorreoContainingIgnoreCase(t,t,PageRequest.of(pagina,Math.min(tamano,100),Sort.by("numeroDocumento"))).map(this::dto));}
  @Transactional public Respuesta crear(Crear req){String correo=StringUtils.lowerCase(StringUtils.trim(req.correo()));String documento=StringUtils.upperCase(StringUtils.trim(req.numeroDocumento()));if(usuarios.existsByCorreoIgnoreCase(correo))throw new ConflictoException("El correo ya está registrado");if(clientes.existsByNumeroDocumento(documento))throw new ConflictoException("El número de documento ya está registrado");var rol=roles.findByNombre("CLIENTE").orElseThrow(()->new ReglaNegocioException("Rol CLIENTE no configurado"));Usuario u=usuarios.save(new Usuario(rol,correo,encoder.encode(req.password()),StringUtils.trim(req.nombres()),StringUtils.trim(req.apellidos()),StringUtils.trimToNull(req.telefono())));Cliente c=clientes.save(new Cliente(u,req.tipoPersona(),req.tipoDocumento(),documento,StringUtils.trimToNull(req.razonSocial()),StringUtils.trimToNull(req.direccion()),StringUtils.trimToNull(req.distrito())));auditoria.registrar(sesion.usuario(),"clientes",c.getId(),"INSERT",Map.of());return dto(c);}
  @Transactional(readOnly=true) public Respuesta obtener(UUID id){Cliente c=buscar(id);verificarLectura(c);return dto(c);}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Cliente c=buscar(id);verificarEdicion(c);String documento=StringUtils.upperCase(StringUtils.trim(req.numeroDocumento()));if(clientes.existsByNumeroDocumentoAndIdNot(documento,id))throw new ConflictoException("El número de documento ya está registrado");c.actualizar(req.tipoPersona(),req.tipoDocumento(),documento,StringUtils.trimToNull(req.razonSocial()),StringUtils.trimToNull(req.direccion()),StringUtils.trimToNull(req.distrito()));auditoria.registrar(sesion.usuario(),"clientes",id,"UPDATE",Map.of());return dto(c);}
  public Cliente buscar(UUID id){return clientes.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Cliente no encontrado"));}
  public Cliente clienteActual(){return clientes.findByUsuarioCorreoIgnoreCase(sesion.usuario().getCorreo()).orElseThrow(()->new RecursoNoEncontradoException("Perfil de cliente no encontrado"));}
  private void verificarLectura(Cliente c){Usuario u=sesion.usuario();if(!sesion.tieneRol("ADMINISTRADOR","ENCARGADO")&&(c.getUsuario()==null||!u.getId().equals(c.getUsuario().getId())))throw new AccessDeniedException("Cliente ajeno");}
  private void verificarEdicion(Cliente c){verificarLectura(c);}
  private Respuesta dto(Cliente c){Usuario u=c.getUsuario();return new Respuesta(c.getId(),u==null?null:u.getId(),u==null?null:u.getCorreo(),u==null?c.getRazonSocial():u.getNombres()+" "+u.getApellidos(),c.getTipoPersona(),c.getTipoDocumento(),c.getNumeroDocumento(),c.getRazonSocial(),c.getDireccion(),c.getDistrito());}
}
