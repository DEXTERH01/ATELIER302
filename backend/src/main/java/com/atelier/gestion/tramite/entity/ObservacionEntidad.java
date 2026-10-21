package com.atelier.gestion.tramite.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="observaciones_entidad",uniqueConstraints=@UniqueConstraint(columnNames={"id_tramite","numero"}))
public class ObservacionEntidad {
  @Id @GeneratedValue @Column(name="id_observacion_entidad") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_tramite") private TramitePublico tramite;
  @Column(nullable=false) private short numero;
  @Column(nullable=false,columnDefinition="text") private String descripcion;
  @Column(name="fecha_notificacion",nullable=false) private LocalDate fechaNotificacion;
  @Column(name="fecha_limite") private LocalDate fechaLimite;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoObservacionEntidad estado;
  @Column(columnDefinition="text") private String respuesta;
  @Column(name="fecha_subsanacion") private LocalDate fechaSubsanacion;

  protected ObservacionEntidad() {}
  public ObservacionEntidad(TramitePublico tramite,short numero,String descripcion,LocalDate fechaNotificacion,
      LocalDate fechaLimite){this.tramite=tramite;this.numero=numero;this.descripcion=descripcion;
    this.fechaNotificacion=fechaNotificacion;this.fechaLimite=fechaLimite;this.estado=EstadoObservacionEntidad.PENDIENTE;}
  public void subsanar(String respuesta,LocalDate fecha){this.respuesta=respuesta;this.fechaSubsanacion=fecha;this.estado=EstadoObservacionEntidad.SUBSANADA;}
  public UUID getId(){return id;} public TramitePublico getTramite(){return tramite;} public short getNumero(){return numero;}
  public String getDescripcion(){return descripcion;} public LocalDate getFechaNotificacion(){return fechaNotificacion;}
  public LocalDate getFechaLimite(){return fechaLimite;} public EstadoObservacionEntidad getEstado(){return estado;}
  public String getRespuesta(){return respuesta;} public LocalDate getFechaSubsanacion(){return fechaSubsanacion;}
}
