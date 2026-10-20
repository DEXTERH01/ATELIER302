package com.atelier.gestion.cotizacion.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity @Table(name="cotizaciones")
public class Cotizacion {
  @Id @GeneratedValue @Column(name="id_cotizacion") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="creada_por") private Empleado creadaPor;
  @Column(nullable=false,unique=true,length=25) private String numero;
  @Column(nullable=false) private short version;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private TipoCotizacion tipo;
  @Column(nullable=false,precision=12,scale=2) private BigDecimal subtotal;
  @Column(nullable=false,precision=12,scale=2) private BigDecimal impuesto;
  @Column(insertable=false,updatable=false,precision=12,scale=2) private BigDecimal total;
  @Column(name="porcentaje_adelanto",nullable=false,precision=5,scale=2) private BigDecimal porcentajeAdelanto;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoCotizacion estado;
  @Column(name="emitida_en") private OffsetDateTime emitidaEn;
  @Column(name="vence_en") private OffsetDateTime venceEn;
  @Column(name="respondida_en") private OffsetDateTime respondidaEn;
  @Column(columnDefinition="text") private String observacion;
  @OneToMany(mappedBy="cotizacion",cascade=CascadeType.ALL,orphanRemoval=true)
  @OrderBy("concepto asc") private List<DetalleCotizacion> detalles=new ArrayList<>();
  protected Cotizacion() {}
  public Cotizacion(Proyecto proyecto,Empleado creadaPor,String numero,short version,TipoCotizacion tipo,BigDecimal porcentajeAdelanto){this.proyecto=proyecto;this.creadaPor=creadaPor;this.numero=numero;this.version=version;this.tipo=tipo;this.porcentajeAdelanto=porcentajeAdelanto;this.estado=EstadoCotizacion.BORRADOR;this.subtotal=BigDecimal.ZERO.setScale(2);this.impuesto=BigDecimal.ZERO.setScale(2);}
  public void reemplazarDetalles(List<DetalleCotizacion> nuevos,BigDecimal porcentajeImpuesto){detalles.clear();nuevos.forEach(d->{d.asignar(this);detalles.add(d);});subtotal=detalles.stream().map(DetalleCotizacion::calcularImporte).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);impuesto=subtotal.multiply(porcentajeImpuesto).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);}
  public void actualizar(BigDecimal porcentajeAdelanto,String observacion,List<DetalleCotizacion> detalles,BigDecimal porcentajeImpuesto){this.porcentajeAdelanto=porcentajeAdelanto;this.observacion=observacion;reemplazarDetalles(detalles,porcentajeImpuesto);}
  public void enviar(OffsetDateTime ahora){estado=EstadoCotizacion.ENVIADA;emitidaEn=ahora;venceEn=ahora.plusDays(10);}
  public void responder(boolean aceptar,String observacion,OffsetDateTime ahora){estado=aceptar?EstadoCotizacion.ACEPTADA:EstadoCotizacion.RECHAZADA;this.observacion=observacion;respondidaEn=ahora;}
  public void vencer(){estado=EstadoCotizacion.VENCIDA;}
  public BigDecimal getTotal(){return subtotal.add(impuesto).setScale(2,RoundingMode.HALF_UP);}
  public BigDecimal adelantoRequerido(){return getTotal().multiply(porcentajeAdelanto).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public Empleado getCreadaPor(){return creadaPor;} public String getNumero(){return numero;} public short getVersion(){return version;} public TipoCotizacion getTipo(){return tipo;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getImpuesto(){return impuesto;} public BigDecimal getPorcentajeAdelanto(){return porcentajeAdelanto;} public EstadoCotizacion getEstado(){return estado;} public OffsetDateTime getEmitidaEn(){return emitidaEn;} public OffsetDateTime getVenceEn(){return venceEn;} public OffsetDateTime getRespondidaEn(){return respondidaEn;} public String getObservacion(){return observacion;} public List<DetalleCotizacion> getDetalles(){return List.copyOf(detalles);}
}
