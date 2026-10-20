package com.atelier.gestion.proyecto.entity;

import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.empleado.entity.Empleado;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "proyectos")
public class Proyecto {
  @Id @GeneratedValue @Column(name = "id_proyecto") private UUID id;
  @Column(nullable = false, unique = true, length = 20) private String codigo;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_cliente") private Cliente cliente;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_encargado") private Empleado encargado;
  @Column(nullable = false, length = 180) private String nombre;
  @Column(columnDefinition = "text") private String descripcion;
  @Column(name = "tipo_proyecto", nullable = false, length = 60) private String tipoProyecto;
  @Column(length = 250) private String ubicacion;
  @Column(name = "area_terreno", precision = 12, scale = 2) private BigDecimal areaTerreno;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 35) private EstadoProyecto estado;
  @Column(name = "requiere_tramite_publico", nullable = false) private boolean requiereTramitePublico;
  @Column(name = "fecha_solicitud", nullable = false) private LocalDate fechaSolicitud;
  @Column(name = "fecha_inicio") private LocalDate fechaInicio;
  @Column(name = "fecha_fin_estimada") private LocalDate fechaFinEstimada;
  @Column(name = "fecha_cierre") private LocalDate fechaCierre;

  protected Proyecto() {}
  public Proyecto(String codigo, Cliente cliente, Empleado encargado, String nombre, String descripcion,
      String tipoProyecto, String ubicacion, BigDecimal areaTerreno, boolean requiereTramitePublico) {
    this.codigo=codigo; this.cliente=cliente; this.encargado=encargado; this.nombre=nombre;
    this.descripcion=descripcion; this.tipoProyecto=tipoProyecto; this.ubicacion=ubicacion;
    this.areaTerreno=areaTerreno; this.requiereTramitePublico=requiereTramitePublico;
    this.estado=EstadoProyecto.SOLICITUD_REGISTRADA; this.fechaSolicitud=LocalDate.now();
  }
  public void actualizar(String nombre,String descripcion,String tipoProyecto,String ubicacion,BigDecimal area,
      boolean requiereTramite,Empleado encargado,LocalDate fechaFinEstimada){this.nombre=nombre;this.descripcion=descripcion;
    this.tipoProyecto=tipoProyecto;this.ubicacion=ubicacion;this.areaTerreno=area;this.requiereTramitePublico=requiereTramite;
    this.encargado=encargado;this.fechaFinEstimada=fechaFinEstimada;}
  public void cambiarEstado(EstadoProyecto nuevo){this.estado=nuevo;if(nuevo==EstadoProyecto.PROYECTO_ACTIVO&&fechaInicio==null)fechaInicio=LocalDate.now();if(nuevo==EstadoProyecto.CERRADO)fechaCierre=LocalDate.now();}
  public UUID getId(){return id;} public String getCodigo(){return codigo;} public Cliente getCliente(){return cliente;}
  public Empleado getEncargado(){return encargado;} public String getNombre(){return nombre;} public String getDescripcion(){return descripcion;}
  public String getTipoProyecto(){return tipoProyecto;} public String getUbicacion(){return ubicacion;} public BigDecimal getAreaTerreno(){return areaTerreno;}
  public EstadoProyecto getEstado(){return estado;} public boolean isRequiereTramitePublico(){return requiereTramitePublico;}
  public LocalDate getFechaSolicitud(){return fechaSolicitud;} public LocalDate getFechaInicio(){return fechaInicio;}
  public LocalDate getFechaFinEstimada(){return fechaFinEstimada;} public LocalDate getFechaCierre(){return fechaCierre;}
}
