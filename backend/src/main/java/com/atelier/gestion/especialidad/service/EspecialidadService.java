package com.atelier.gestion.especialidad.service;
import static com.atelier.gestion.especialidad.dto.EspecialidadDtos.*;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.especialidad.entity.Especialidad;
import com.atelier.gestion.especialidad.repository.EspecialidadRepository;
import com.atelier.gestion.security.SesionActual;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class EspecialidadService{
  private final EspecialidadRepository repo;private final SesionActual sesion;private final AuditoriaService auditoria;
  public EspecialidadService(EspecialidadRepository r,SesionActual s,AuditoriaService a){repo=r;sesion=s;auditoria=a;}
  @Transactional(readOnly=true) public List<Respuesta> listar(){return repo.findAllByOrderByNombreAsc().stream().map(this::dto).toList();}
  @Transactional public Respuesta crear(Crear req){String nombre=StringUtils.trim(req.nombre());if(repo.existsByNombreIgnoreCase(nombre))throw new ConflictoException("La especialidad ya existe");Especialidad e=repo.save(new Especialidad(nombre,StringUtils.trimToNull(req.descripcion())));auditoria.registrar(sesion.usuario(),"especialidades",e.getId(),"INSERT",Map.of("nombre",nombre));return dto(e);}
  @Transactional public Respuesta actualizar(Short id,Actualizar req){Especialidad e=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Especialidad no encontrada"));String nombre=StringUtils.trim(req.nombre());if(repo.existsByNombreIgnoreCaseAndIdNot(nombre,id))throw new ConflictoException("La especialidad ya existe");e.actualizar(nombre,StringUtils.trimToNull(req.descripcion()),req.activa());auditoria.registrar(sesion.usuario(),"especialidades",id,"UPDATE",Map.of());return dto(e);}
  private Respuesta dto(Especialidad e){return new Respuesta(e.getId(),e.getNombre(),e.getDescripcion(),e.isActiva());}
}
