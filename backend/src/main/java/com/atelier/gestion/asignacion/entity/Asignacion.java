package com.atelier.gestion.asignacion.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.especialidad.entity.Especialidad;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="asignaciones")
public class Asignacion {
  @Id @GeneratedValue @Column(name="id_asignacion") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_empleado") private Empleado empleado;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_especialidad") private Especialidad especialidad;
  @Column(name="rol_proyecto",nullable=false,length=80) private String rolProyecto;
  @Column(name="fecha_asignacion",nullable=false) private LocalDate fechaAsignacion;
  @Column(name="fecha_fin") private LocalDate fechaFin;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoAsignacion estado;
  protected Asignacion(){}
  public Asignacion(Proyecto proyecto,Empleado empleado,Especialidad especialidad,String rolProyecto,LocalDate fechaAsignacion){this.proyecto=proyecto;this.empleado=empleado;this.especialidad=especialidad;this.rolProyecto=rolProyecto;this.fechaAsignacion=fechaAsignacion;this.estado=EstadoAsignacion.ACTIVA;}
  public void actualizar(String rolProyecto,EstadoAsignacion estado,LocalDate fechaFin){this.rolProyecto=rolProyecto;this.estado=estado;this.fechaFin=estado==EstadoAsignacion.ACTIVA?null:fechaFin;}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public Empleado getEmpleado(){return empleado;} public Especialidad getEspecialidad(){return especialidad;} public String getRolProyecto(){return rolProyecto;} public LocalDate getFechaAsignacion(){return fechaAsignacion;} public LocalDate getFechaFin(){return fechaFin;} public EstadoAsignacion getEstado(){return estado;}
}
