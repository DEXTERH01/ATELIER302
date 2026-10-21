package com.atelier.gestion.tarea.service;
import static com.atelier.gestion.tarea.dto.TareaDtos.*;
import com.atelier.gestion.asignacion.entity.*;
import com.atelier.gestion.asignacion.repository.AsignacionRepository;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.tarea.entity.*;
import com.atelier.gestion.tarea.repository.TareaRepository;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class TareaService{
  private static final Map<EstadoTarea,Set<EstadoTarea>> TRANSICIONES=Map.of(EstadoTarea.PENDIENTE,Set.of(EstadoTarea.PENDIENTE,EstadoTarea.EN_PROCESO,EstadoTarea.BLOQUEADA,EstadoTarea.CANCELADA),EstadoTarea.EN_PROCESO,Set.of(EstadoTarea.EN_PROCESO,EstadoTarea.BLOQUEADA,EstadoTarea.COMPLETADA,EstadoTarea.CANCELADA),EstadoTarea.BLOQUEADA,Set.of(EstadoTarea.BLOQUEADA,EstadoTarea.EN_PROCESO,EstadoTarea.CANCELADA),EstadoTarea.COMPLETADA,Set.of(EstadoTarea.COMPLETADA),EstadoTarea.CANCELADA,Set.of(EstadoTarea.CANCELADA));
  private final TareaRepository repo;private final AsignacionRepository asignaciones;private final AutorizacionProyectoService acceso;private final AuditoriaService auditoria;private final Clock clock;
  public TareaService(TareaRepository r,AsignacionRepository a,AutorizacionProyectoService ac,AuditoriaService au,Clock c){repo=r;asignaciones=a;acceso=ac;auditoria=au;clock=c;}
  @Transactional(readOnly=true) public List<Respuesta> listar(UUID asignacionId){Asignacion a=buscarAsignacion(asignacionId);exigirAcceso(a,acceso.usuario());return repo.findByAsignacionIdOrderByFechaLimiteAsc(asignacionId).stream().map(this::dto).toList();}
  @Transactional public Respuesta crear(UUID asignacionId,Crear req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Asignacion a=buscarAsignacion(asignacionId);if(a.getEstado()!=EstadoAsignacion.ACTIVA)throw new ReglaNegocioException("La asignación no está activa");validarFechas(req.fechaInicio(),req.fechaLimite());Tarea t=repo.save(new Tarea(a,StringUtils.trim(req.titulo()),StringUtils.trimToNull(req.descripcion()),req.prioridad(),req.fechaInicio(),req.fechaLimite(),OffsetDateTime.now(clock)));auditoria.registrar(u,"tareas",t.getId(),"INSERT",Map.of("asignacion",asignacionId));return dto(t);}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Usuario u=acceso.usuario();Tarea t=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Tarea no encontrada"));Asignacion a=t.getAsignacion();exigirAcceso(a,u);if(a.getEstado()!=EstadoAsignacion.ACTIVA)throw new ReglaNegocioException("La asignación no está activa");if(!TRANSICIONES.get(t.getEstado()).contains(req.estado()))throw new ReglaNegocioException("Transición de tarea no permitida");if(!acceso.esGestor(u)&&(!Objects.equals(t.getTitulo(),StringUtils.trim(req.titulo()))||!Objects.equals(t.getDescripcion(),StringUtils.trimToNull(req.descripcion()))||t.getPrioridad()!=req.prioridad()||!Objects.equals(t.getFechaInicio(),req.fechaInicio())||!Objects.equals(t.getFechaLimite(),req.fechaLimite())))throw new AccessDeniedException("El especialista solo puede cambiar el estado de su tarea");validarFechas(req.fechaInicio(),req.fechaLimite());t.actualizar(StringUtils.trim(req.titulo()),StringUtils.trimToNull(req.descripcion()),req.prioridad(),req.estado(),req.fechaInicio(),req.fechaLimite(),OffsetDateTime.now(clock));auditoria.registrar(u,"tareas",id,"UPDATE",Map.of("estado",req.estado().name()));return dto(t);}
  private Asignacion buscarAsignacion(UUID id){return asignaciones.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Asignación no encontrada"));}
  private void exigirAcceso(Asignacion a,Usuario u){if(acceso.esGestor(u))return;if("ESPECIALISTA".equals(u.getRol().getNombre())&&u.getId().equals(a.getEmpleado().getUsuario().getId())&&a.getEstado()==EstadoAsignacion.ACTIVA)return;throw new AccessDeniedException("No tiene acceso a las tareas de esta asignación");}
  private void validarFechas(LocalDate inicio,LocalDate limite){if(inicio!=null&&limite!=null&&limite.isBefore(inicio))throw new ReglaNegocioException("La fecha límite no puede ser anterior al inicio");}
  private Respuesta dto(Tarea t){Asignacion a=t.getAsignacion();var u=a.getEmpleado().getUsuario();return new Respuesta(t.getId(),a.getId(),a.getProyecto().getId(),a.getEmpleado().getId(),u.getNombres()+" "+u.getApellidos(),a.getEspecialidad().getNombre(),t.getTitulo(),t.getDescripcion(),t.getPrioridad(),t.getEstado(),t.getFechaInicio(),t.getFechaLimite(),t.getCompletadaEn(),t.getCreadoEn());}
}
