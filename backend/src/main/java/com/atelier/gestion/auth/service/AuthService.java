package com.atelier.gestion.auth.service;
import com.atelier.gestion.auth.dto.AuthDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.security.JwtService;
import com.atelier.gestion.usuario.entity.*;
import com.atelier.gestion.usuario.repository.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AuthService {
  private final UsuarioRepository usuarios; private final RolRepository roles; private final ClienteRepository clientes; private final PasswordEncoder encoder; private final AuthenticationManager authentication; private final JwtService jwt; private final TokenRefreshService refresh; private final AuditoriaService auditoria;
  public AuthService(UsuarioRepository u,RolRepository r,ClienteRepository c,PasswordEncoder e,AuthenticationManager a,JwtService j,TokenRefreshService t,AuditoriaService audit){usuarios=u;roles=r;clientes=c;encoder=e;authentication=a;jwt=j;refresh=t;auditoria=audit;}
  @Transactional public Sesion registrarCliente(RegisterClientRequest req,String ip,String agent){String correo=StringUtils.lowerCase(StringUtils.trim(req.correo()));String documento=StringUtils.upperCase(StringUtils.trim(req.numeroDocumento()));if(usuarios.existsByCorreoIgnoreCase(correo))throw new ConflictoException("El correo ya está registrado");if(clientes.existsByNumeroDocumento(documento))throw new ConflictoException("El número de documento ya está registrado");Rol rol=roles.findByNombre("CLIENTE").orElseThrow(()->new IllegalStateException("Rol CLIENTE no configurado"));Usuario user=usuarios.save(new Usuario(rol,correo,encoder.encode(req.password()),StringUtils.trim(req.nombres()),StringUtils.trim(req.apellidos()),StringUtils.trimToNull(req.telefono())));clientes.save(new Cliente(user,req.tipoPersona(),req.tipoDocumento(),documento,StringUtils.trimToNull(req.razonSocial()),StringUtils.trimToNull(req.direccion()),StringUtils.trimToNull(req.distrito())));auditoria.registrarLogin(user,ip,agent,"REGISTRO_CLIENTE");return emitir(user,ip,agent);}
  @Transactional public Sesion login(LoginRequest req,String ip,String agent){String correo=StringUtils.lowerCase(StringUtils.trim(req.correo()));try{authentication.authenticate(new UsernamePasswordAuthenticationToken(correo,req.password()));}catch(AuthenticationException ex){throw new NoAutorizadoException("Credenciales inválidas");}Usuario user=usuarios.findByCorreoIgnoreCase(correo).orElseThrow(()->new NoAutorizadoException("Credenciales inválidas"));user.registrarAcceso(OffsetDateTime.now(ZoneOffset.UTC));auditoria.registrarLogin(user,ip,agent,"CREDENCIALES");return emitir(user,ip,agent);}
  @Transactional(readOnly=true) public UsuarioResponse me(String correo){return usuario(usuarios.findByCorreoIgnoreCase(correo).orElseThrow(()->new RecursoNoEncontradoException("Usuario no encontrado")));}
  public Sesion renovar(String token,String ip,String agent){var rotation=refresh.rotar(token,ip,agent);return new Sesion(respuesta(rotation.usuario()),rotation.token());}
  public void logout(String token){if(StringUtils.isNotBlank(token))refresh.revocar(token);}
  private Sesion emitir(Usuario user,String ip,String agent){return new Sesion(respuesta(user),refresh.emitirInicial(user,ip,agent));}
  private AuthResponse respuesta(Usuario user){return new AuthResponse(jwt.emitir(user),"Bearer",jwt.expiracionSegundos(),usuario(user));}
  private UsuarioResponse usuario(Usuario u){return new UsuarioResponse(u.getId(),u.getCorreo(),u.getNombres()+" "+u.getApellidos(),u.getRol().getNombre());}
  public record Sesion(AuthResponse respuesta,String refreshToken){}
}

