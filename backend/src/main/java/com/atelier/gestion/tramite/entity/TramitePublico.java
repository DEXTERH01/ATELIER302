package com.atelier.gestion.tramite.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name="tramites_publicos")
public class TramitePublico {
  @Id @GeneratedValue @Column(name="id_tramite") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="responsable") private Empleado responsable;
  @Column(nullable=false,length=180) private String entidad;
  @Column(name="tipo_tramite",nullable=false,length=150) private String tipoTramite;
  @Column(name="numero_expediente_externo",length=80) private String numeroExpedienteExterno;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=25) private EstadoTramite estado;
  @Column(name="fecha_presentacion") private LocalDate fechaPresentacion;
  @Column(name="fecha_respuesta") private LocalDate fechaRespuesta;
  @Column(name="observacion_general",columnDefinition="text") private String observacionGeneral;
  @Column(name="creado_en",nullable=false,updatable=false) private OffsetDateTime creadoEn;

  protected TramitePublico() {}
  public TramitePublico(Proyecto proyecto,Empleado responsable,String entidad,String tipoTramite,
      String numeroExpedienteExterno,String observacionGeneral,OffsetDateTime ahora){
    this.proyecto=proyecto;this.responsable=responsable;this.entidad=entidad;
    this.tipoTramite=tipoTramite;this.numeroExpedienteExterno=numeroExpedienteExterno;
    this.observacionGeneral=observacionGeneral;this.estado=EstadoTramite.PREPARACION;this.creadoEn=ahora;
  }
  public void actualizar(Empleado responsable,String entidad,String tipoTramite,String numeroExterno,
      String observacion){this.responsable=responsable;this.entidad=entidad;this.tipoTramite=tipoTramite;
    this.numeroExpedienteExterno=numeroExterno;this.observacionGeneral=observacion;}
  public void presentar(LocalDate fecha){estado=EstadoTramite.PRESENTADO;fechaPresentacion=fecha;}
  public void revisar(){estado=EstadoTramite.EN_REVISION;}
  public void observar(){estado=EstadoTramite.OBSERVADO;}
  public void reanudar(){estado=EstadoTramite.EN_REVISION;}
  public void resolver(boolean aprobado,LocalDate fecha,String observacion){estado=aprobado?EstadoTramite.APROBADO:EstadoTramite.RECHAZADO;fechaRespuesta=fecha;observacionGeneral=observacion;}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public Empleado getResponsable(){return responsable;}
  public String getEntidad(){return entidad;} public String getTipoTramite(){return tipoTramite;} public String getNumeroExpedienteExterno(){return numeroExpedienteExterno;}
  public EstadoTramite getEstado(){return estado;} public LocalDate getFechaPresentacion(){return fechaPresentacion;} public LocalDate getFechaRespuesta(){return fechaRespuesta;}
  public String getObservacionGeneral(){return observacionGeneral;} public OffsetDateTime getCreadoEn(){return creadoEn;}
}
