package com.atelier.gestion.estudio.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.math.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="estudios_tecnicos")
public class EstudioTecnico {
  @Id @GeneratedValue @Column(name="id_estudio") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="responsable") private Empleado responsable;
  @Column(nullable=false,length=80) private String tipo;
  @Column(length=180) private String proveedor;
  @Column(nullable=false) private boolean requerido;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoEstudio estado;
  @Column(name="fecha_solicitud") private LocalDate fechaSolicitud;
  @Column(name="fecha_resultado") private LocalDate fechaResultado;
  @Column(columnDefinition="text") private String resultado;
  @Column(precision=12,scale=2) private BigDecimal costo;
  protected EstudioTecnico(){}
  public EstudioTecnico(Proyecto proyecto,Empleado responsable,String tipo,String proveedor,boolean requerido,LocalDate fechaSolicitud,BigDecimal costo){this.proyecto=proyecto;this.responsable=responsable;this.tipo=tipo;this.proveedor=proveedor;this.requerido=requerido;this.fechaSolicitud=fechaSolicitud;this.costo=dinero(costo);this.estado=EstadoEstudio.PENDIENTE;}
  public void actualizar(Empleado responsable,String tipo,String proveedor,boolean requerido,EstadoEstudio estado,LocalDate fechaSolicitud,LocalDate fechaResultado,String resultado,BigDecimal costo){this.responsable=responsable;this.tipo=tipo;this.proveedor=proveedor;this.requerido=requerido;this.estado=estado;this.fechaSolicitud=fechaSolicitud;this.fechaResultado=fechaResultado;this.resultado=resultado;this.costo=dinero(costo);}
  private BigDecimal dinero(BigDecimal valor){return valor==null?null:valor.setScale(2,RoundingMode.HALF_UP);}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public Empleado getResponsable(){return responsable;} public String getTipo(){return tipo;} public String getProveedor(){return proveedor;} public boolean isRequerido(){return requerido;} public EstadoEstudio getEstado(){return estado;} public LocalDate getFechaSolicitud(){return fechaSolicitud;} public LocalDate getFechaResultado(){return fechaResultado;} public String getResultado(){return resultado;} public BigDecimal getCosto(){return costo;}
}
