package com.atelier.gestion.propuesta.entity;

import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cotizacion.entity.Cotizacion;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="observaciones_cliente")
public class ObservacionCliente {
  @Id @GeneratedValue @Column(name="id_observacion_cliente") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_propuesta") private Propuesta propuesta;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_cliente") private Cliente cliente;
  @Column(name="numero_ronda",nullable=false) private short numeroRonda;
  @Column(nullable=false,columnDefinition="text") private String descripcion;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoObservacionCliente estado;
  @Column(name="es_adicional",nullable=false) private boolean adicional;
  @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_cotizacion_adicional") private Cotizacion cotizacionAdicional;
  @Column(name="creada_en",nullable=false,updatable=false) private OffsetDateTime creadaEn;
  @Column(name="atendida_en") private OffsetDateTime atendidaEn;

  protected ObservacionCliente() {}
  public ObservacionCliente(Propuesta propuesta,Cliente cliente,short numeroRonda,String descripcion,
      OffsetDateTime ahora){this.propuesta=propuesta;this.cliente=cliente;this.numeroRonda=numeroRonda;
    this.descripcion=descripcion;this.estado=EstadoObservacionCliente.PENDIENTE;this.creadaEn=ahora;}
  public void actualizar(EstadoObservacionCliente estado,boolean adicional,OffsetDateTime ahora){
    this.estado=estado;this.adicional=adicional;
    this.atendidaEn=estado==EstadoObservacionCliente.ATENDIDA?ahora:null;
  }
  public void vincularCotizacion(Cotizacion cotizacion){this.cotizacionAdicional=cotizacion;this.adicional=true;}

  public UUID getId(){return id;} public Propuesta getPropuesta(){return propuesta;} public Cliente getCliente(){return cliente;}
  public short getNumeroRonda(){return numeroRonda;} public String getDescripcion(){return descripcion;}
  public EstadoObservacionCliente getEstado(){return estado;} public boolean isAdicional(){return adicional;}
  public Cotizacion getCotizacionAdicional(){return cotizacionAdicional;} public OffsetDateTime getCreadaEn(){return creadaEn;}
  public OffsetDateTime getAtendidaEn(){return atendidaEn;}
}
