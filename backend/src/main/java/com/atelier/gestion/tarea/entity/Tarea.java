package com.atelier.gestion.tarea.entity;

import com.atelier.gestion.asignacion.entity.Asignacion;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="tareas")
public class Tarea {
  @Id @GeneratedValue @Column(name="id_tarea") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_asignacion") private Asignacion asignacion;
  @Column(nullable=false,length=180) private String titulo;
  @Column(columnDefinition="text") private String descripcion;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private PrioridadTarea prioridad;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoTarea estado;
  @Column(name="fecha_inicio") private LocalDate fechaInicio;
  @Column(name="fecha_limite") private LocalDate fechaLimite;
  @Column(name="completada_en") private OffsetDateTime completadaEn;
  @Column(name="creado_en",nullable=false,updatable=false) private OffsetDateTime creadoEn;
  protected Tarea(){}
  public Tarea(Asignacion asignacion,String titulo,String descripcion,PrioridadTarea prioridad,LocalDate fechaInicio,LocalDate fechaLimite,OffsetDateTime ahora){this.asignacion=asignacion;this.titulo=titulo;this.descripcion=descripcion;this.prioridad=prioridad;this.fechaInicio=fechaInicio;this.fechaLimite=fechaLimite;this.estado=EstadoTarea.PENDIENTE;this.creadoEn=ahora;}
  public void actualizar(String titulo,String descripcion,PrioridadTarea prioridad,EstadoTarea estado,LocalDate fechaInicio,LocalDate fechaLimite,OffsetDateTime ahora){this.titulo=titulo;this.descripcion=descripcion;this.prioridad=prioridad;this.estado=estado;this.fechaInicio=fechaInicio;this.fechaLimite=fechaLimite;if(estado==EstadoTarea.COMPLETADA&&completadaEn==null)completadaEn=ahora;}
  public UUID getId(){return id;} public Asignacion getAsignacion(){return asignacion;} public String getTitulo(){return titulo;} public String getDescripcion(){return descripcion;} public PrioridadTarea getPrioridad(){return prioridad;} public EstadoTarea getEstado(){return estado;} public LocalDate getFechaInicio(){return fechaInicio;} public LocalDate getFechaLimite(){return fechaLimite;} public OffsetDateTime getCompletadaEn(){return completadaEn;} public OffsetDateTime getCreadoEn(){return creadoEn;}
}
