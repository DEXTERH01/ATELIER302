package com.atelier.gestion.auth.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.atelier.gestion.auth.dto.AuthDtos.RegisterClientRequest;
import com.atelier.gestion.auth.dto.AuthDtos.LoginRequest;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.common.exception.ConflictoException;
import com.atelier.gestion.security.JwtService;
import com.atelier.gestion.usuario.entity.*;
import com.atelier.gestion.usuario.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
class AuthServiceTest {
  @Mock UsuarioRepository usuarios; @Mock RolRepository roles; @Mock ClienteRepository clientes; @Mock PasswordEncoder encoder; @Mock AuthenticationManager authentication; @Mock JwtService jwt; @Mock TokenRefreshService refresh; @Mock AuditoriaService auditoria;
  AuthService service;
  @BeforeEach void setUp(){MockitoAnnotations.openMocks(this);service=new AuthService(usuarios,roles,clientes,encoder,authentication,jwt,refresh,auditoria);}
  @Test void registroAsignaSiempreRolCliente(){Rol cliente=mock(Rol.class);when(cliente.getNombre()).thenReturn("CLIENTE");when(roles.findByNombre("CLIENTE")).thenReturn(Optional.of(cliente));when(encoder.encode(anyString())).thenReturn("hash-bcrypt");when(usuarios.save(any())).thenAnswer(i->i.getArgument(0));when(refresh.emitirInicial(any(),any(),any())).thenReturn("refresh");when(jwt.emitir(any())).thenReturn("access");RegisterClientRequest req=new RegisterClientRequest("CLIENTE@EXAMPLE.COM","Segura1234!x","Ana","Pérez",null,"NATURAL","DNI","12345678",null,null,null);service.registrarCliente(req,"127.0.0.1","test");ArgumentCaptor<Usuario> captor=ArgumentCaptor.forClass(Usuario.class);verify(usuarios).save(captor.capture());assertThat(captor.getValue().getRol().getNombre()).isEqualTo("CLIENTE");assertThat(captor.getValue().getCorreo()).isEqualTo("cliente@example.com");verify(clientes).save(any());}
  @Test void rechazaCorreoDuplicado(){when(usuarios.existsByCorreoIgnoreCase(any())).thenReturn(true);RegisterClientRequest req=new RegisterClientRequest("a@b.com","Segura1234!x","Ana","Pérez",null,"NATURAL","DNI","12345678",null,null,null);assertThatThrownBy(()->service.registrarCliente(req,"ip","ua")).isInstanceOf(ConflictoException.class).hasMessageContaining("correo");verifyNoInteractions(roles,encoder);}
  @Test void loginExitosoDejaAuditoria(){Usuario usuario=mock(Usuario.class);Rol rol=mock(Rol.class);when(usuario.getRol()).thenReturn(rol);when(usuario.getCorreo()).thenReturn("cliente@atelier.pe");when(usuario.getNombres()).thenReturn("Cliente");when(usuario.getApellidos()).thenReturn("Demo");when(rol.getNombre()).thenReturn("CLIENTE");when(usuarios.findByCorreoIgnoreCase("cliente@atelier.pe")).thenReturn(Optional.of(usuario));when(refresh.emitirInicial(any(),any(),any())).thenReturn("refresh");when(jwt.emitir(any())).thenReturn("access");service.login(new LoginRequest("CLIENTE@ATELIER.PE","AtelierDemo2026!"),"127.0.0.1","test");verify(auditoria).registrarLogin(usuario,"127.0.0.1","test","CREDENCIALES");}
}

