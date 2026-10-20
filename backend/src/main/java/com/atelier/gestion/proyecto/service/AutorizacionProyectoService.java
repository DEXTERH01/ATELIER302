package com.atelier.gestion.proyecto.service;

import com.atelier.gestion.common.exception.RecursoNoEncontradoException;
import com.atelier.gestion.proyecto.entity.Proyecto;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AutorizacionProyectoService {
  private final ProyectoRepository proyectos; private final SesionActual sesion;
  public AutorizacionProyectoService(ProyectoRepository proyectos,SesionActual sesion){this.proyectos=proyectos;this.sesion=sesion;}
  public Usuario usuario(){return sesion.usuario();}
  public Proyecto buscarVisible(UUID id){Proyecto p=proyectos.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));verificarVisible(p,usuario());return p;}
  public void verificarVisible(Proyecto p,Usuario u){if(esGestor(u))return;if("CLIENTE".equals(u.getRol().getNombre())&&p.getCliente().getUsuario()!=null&&u.getId().equals(p.getCliente().getUsuario().getId()))return;if("ESPECIALISTA".equals(u.getRol().getNombre())&&proyectos.asignadoA(p.getId(),u.getCorreo()))return;throw new AccessDeniedException("Proyecto ajeno");}
  public void exigirGestor(Usuario u){if(!esGestor(u))throw new AccessDeniedException("Operación reservada a gestores");}
  public void exigirAdministrador(Usuario u){if(!"ADMINISTRADOR".equals(u.getRol().getNombre()))throw new AccessDeniedException("Operación reservada al administrador");}
  public void exigirClientePropietario(Proyecto p,Usuario u){if(!"CLIENTE".equals(u.getRol().getNombre())||p.getCliente().getUsuario()==null||!u.getId().equals(p.getCliente().getUsuario().getId()))throw new AccessDeniedException("Operación reservada al cliente propietario");}
  public boolean esGestor(Usuario u){return Set.of("ADMINISTRADOR","ENCARGADO").contains(u.getRol().getNombre());}
}
