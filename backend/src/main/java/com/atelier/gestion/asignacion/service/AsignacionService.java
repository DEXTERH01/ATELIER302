package com.atelier.gestion.asignacion.service;
import static com.atelier.gestion.asignacion.dto.AsignacionDtos.*;
import com.atelier.gestion.asignacion.entity.*;
import com.atelier.gestion.asignacion.repository.AsignacionRepository;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.*;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.repository.EmpleadoRepository;
import com.atelier.gestion.especialidad.entity.Especialidad;
import com.atelier.gestion.especialidad.repository.EspecialidadRepository;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.tarea.entity.EstadoTarea;
import com.atelier.gestion.tarea.repository.TareaRepository;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AsignacionService{
  private static final Set<EstadoProyecto> FASE_TECNICA=Set.of(EstadoProyecto.PROYECTO_ACTIVO,EstadoProyecto.EN_ESTUDIOS,EstadoProyecto.EN_DISENO);
  private final AsignacionRepository repo;private final ProyectoRepository proyectos;private final EmpleadoRepository empleados;private final EspecialidadRepository especialidades;private final TareaRepository tareas;private final AutorizacionProyectoService acceso;private final AuditoriaService auditoria;
  public AsignacionService(AsignacionRepository r,ProyectoRepository p,EmpleadoRepository e,EspecialidadRepository es,TareaRepository t,AutorizacionProyectoService a,AuditoriaService au){repo=r;proyectos=p;empleados=e;especialidades=es;tareas=t;acceso=a;auditoria=au;}
  @Transactional(readOnly=true) public List<Respuesta> listar(UUID proyectoId){acceso.buscarVisible(proyectoId);return repo.findByProyectoIdOrderByFechaAsignacionDesc(proyectoId).stream().map(this::dto).toList();}
  @Transactional public Respuesta crear(UUID proyectoId,Crear req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Proyecto p=proyectos.bloquear(proyectoId).orElseThrow(()->new RecursoNoEncontradoException("Proyecto no encontrado"));exigirFase(p);Empleado empleado=empleados.findById(req.empleadoId()).orElseThrow(()->new RecursoNoEncontradoException("Empleado no encontrado"));if(!empleado.isDisponible()||!empleado.getUsuario().isActivo())throw new ReglaNegocioException("El empleado no está disponible");Especialidad especialidad=especialidades.findById(req.especialidadId()).orElseThrow(()->new RecursoNoEncontradoException("Especialidad no encontrada"));if(!especialidad.isActiva())throw new ReglaNegocioException("La especialidad está inactiva");if(repo.existsByProyectoIdAndEmpleadoIdAndEspecialidadId(proyectoId,req.empleadoId(),req.especialidadId()))throw new ConflictoException("El especialista ya tiene esta especialidad en el proyecto");Asignacion a=repo.save(new Asignacion(p,empleado,especialidad,StringUtils.trim(req.rolProyecto()),req.fechaAsignacion()==null?LocalDate.now():req.fechaAsignacion()));auditoria.registrar(u,"asignaciones",a.getId(),"INSERT",Map.of("proyecto",proyectoId));return dto(a);}
  @Transactional public Respuesta actualizar(UUID id,Actualizar req){Usuario u=acceso.usuario();acceso.exigirGestor(u);Asignacion a=buscar(id);if(a.getEstado()!=EstadoAsignacion.ACTIVA&&req.estado()!=a.getEstado())throw new ReglaNegocioException("Una asignación cerrada no puede reactivarse");LocalDate fin=req.estado()==EstadoAsignacion.ACTIVA?null:(req.fechaFin()==null?LocalDate.now():req.fechaFin());if(fin!=null&&fin.isBefore(a.getFechaAsignacion()))throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la asignación");if(req.estado()!=EstadoAsignacion.ACTIVA&&tareas.countByAsignacionIdAndEstadoNotIn(id,List.of(EstadoTarea.COMPLETADA,EstadoTarea.CANCELADA))>0)throw new ReglaNegocioException("Debe completar o cancelar las tareas pendientes antes de cerrar la asignación");a.actualizar(StringUtils.trim(req.rolProyecto()),req.estado(),fin);auditoria.registrar(u,"asignaciones",id,"UPDATE",Map.of("estado",req.estado().name()));return dto(a);}
  public Asignacion buscar(UUID id){return repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Asignación no encontrada"));}
  private void exigirFase(Proyecto p){if(!FASE_TECNICA.contains(p.getEstado()))throw new ReglaNegocioException("El proyecto no está en ejecución técnica");}
  private Respuesta dto(Asignacion a){Empleado e=a.getEmpleado();var u=e.getUsuario();return new Respuesta(a.getId(),a.getProyecto().getId(),e.getId(),u.getId(),u.getNombres()+" "+u.getApellidos(),e.getCargo(),a.getEspecialidad().getId(),a.getEspecialidad().getNombre(),a.getRolProyecto(),a.getFechaAsignacion(),a.getFechaFin(),a.getEstado());}
}
