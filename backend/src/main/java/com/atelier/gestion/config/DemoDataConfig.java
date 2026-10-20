package com.atelier.gestion.config;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.usuario.entity.Usuario;
import com.atelier.gestion.usuario.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration @Profile("dev")
public class DemoDataConfig {
  @Bean CommandLineRunner usuariosDemo(UsuarioRepository usuarios,RolRepository roles,
      ClienteRepository clientes,EmpleadoRepository empleados,PasswordEncoder encoder){return args->{
        empleado(empleados,crear(usuarios,roles,encoder,"admin@atelier.demo","ADMINISTRADOR","Andrea","Administradora"),null,"Administradora","INTERNO");
        empleado(empleados,crear(usuarios,roles,encoder,"encargado@atelier.demo","ENCARGADO","Carlos","Mendoza"),null,"Encargado de proyectos","INTERNO");
        empleado(empleados,crear(usuarios,roles,encoder,"arquitecta@atelier.demo","ESPECIALISTA","Lucía","Torres"),"CAP-DEMO-001","Arquitecta","INTERNO");
        if(!usuarios.existsByCorreoIgnoreCase("cliente@atelier.demo")){Usuario u=crear(usuarios,roles,encoder,"cliente@atelier.demo","CLIENTE","Elena","Rojas");clientes.save(new Cliente(u,"NATURAL","DNI","70000001",null,"Av. Los Arquitectos 245","San Juan de Miraflores"));}};}
  private Usuario crear(UsuarioRepository usuarios,RolRepository roles,PasswordEncoder encoder,String correo,String rol,String nombres,String apellidos){return usuarios.findByCorreoIgnoreCase(correo).orElseGet(()->usuarios.save(new Usuario(roles.findByNombre(rol).orElseThrow(),correo,encoder.encode("AtelierDemo2026!"),nombres,apellidos,null)));}
  private void empleado(EmpleadoRepository empleados,Usuario usuario,String colegiatura,String cargo,String tipo){if(!empleados.existsByUsuarioId(usuario.getId()))empleados.save(new Empleado(usuario,colegiatura,cargo,tipo,true));}
}
