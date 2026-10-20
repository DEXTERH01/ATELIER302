package com.atelier.gestion.security;

import com.atelier.gestion.common.exception.NoAutorizadoException;
import com.atelier.gestion.usuario.entity.Usuario;
import com.atelier.gestion.usuario.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SesionActual {
  private final UsuarioRepository usuarios;
  public SesionActual(UsuarioRepository usuarios){this.usuarios=usuarios;}
  public Usuario usuario(){var auth=SecurityContextHolder.getContext().getAuthentication();if(auth==null||!auth.isAuthenticated())throw new NoAutorizadoException("Se requiere autenticación");return usuarios.findByCorreoIgnoreCase(auth.getName()).orElseThrow(()->new NoAutorizadoException("Usuario autenticado no encontrado"));}
  public boolean tieneRol(String... roles){String actual=usuario().getRol().getNombre();for(String rol:roles)if(rol.equals(actual))return true;return false;}
}
