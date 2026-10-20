package com.atelier.gestion.expediente.service;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.expediente.dto.ExpedienteDtos.Respuesta;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpedienteService {
  private final ExpedienteRepository expedientes;private final ProyectoRepository proyectos;private final AutorizacionProyectoService acceso;private final AuditoriaService auditoria;
  public ExpedienteService(ExpedienteRepository e,ProyectoRepository p,AutorizacionProyectoService a,AuditoriaService au){expedientes=e;proyectos=p;acceso=a;auditoria=au;}
  @Transactional(readOnly=true) public Respuesta obtenerPorProyecto(UUID proyectoId){acceso.buscarVisible(proyectoId);return dto(expedientes.findByProyectoId(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("El proyecto todavía no tiene expediente")));}
  @Transactional public Respuesta crearManual(UUID proyectoId){Usuario u=acceso.usuario();acceso.exigirGestor(u);Proyecto p=proyectos.bloquear(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));if(p.getEstado()!=EstadoProyecto.PROYECTO_ACTIVO)throw new ReglaNegocioException("El expediente solo se crea cuando el proyecto está activo");return dto(crearIdempotente(p,u));}
  public Expediente crearIdempotente(Proyecto p,Usuario u){return expedientes.findByProyectoId(p.getId()).orElseGet(()->{Expediente e=expedientes.save(new Expediente(p,"EXP-"+p.getCodigo().substring(5)));auditoria.registrar(u,"expedientes",e.getId(),"INSERT",Map.of("codigo",e.getCodigo()));return e;});}
  private Respuesta dto(Expediente e){return new Respuesta(e.getId(),e.getProyecto().getId(),e.getCodigo(),e.getEstado(),e.getCreadoEn(),e.getCerradoEn());}
}
