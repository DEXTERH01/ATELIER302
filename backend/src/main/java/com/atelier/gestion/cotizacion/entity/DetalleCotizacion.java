package com.atelier.gestion.cotizacion.entity;

import jakarta.persistence.*;
import java.math.*;
import java.util.UUID;

@Entity @Table(name="detalle_cotizacion")
public class DetalleCotizacion {
  @Id @GeneratedValue @Column(name="id_detalle") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_cotizacion") private Cotizacion cotizacion;
  @Column(nullable=false,length=180) private String concepto;
  @Column(length=500) private String descripcion;
  @Column(nullable=false,precision=10,scale=2) private BigDecimal cantidad;
  @Column(name="precio_unitario",nullable=false,precision=12,scale=2) private BigDecimal precioUnitario;
  @Column(insertable=false,updatable=false,precision=12,scale=2) private BigDecimal importe;
  protected DetalleCotizacion() {}
  public DetalleCotizacion(String concepto,String descripcion,BigDecimal cantidad,BigDecimal precioUnitario){this.concepto=concepto;this.descripcion=descripcion;this.cantidad=cantidad.setScale(2,RoundingMode.HALF_UP);this.precioUnitario=precioUnitario.setScale(2,RoundingMode.HALF_UP);}
  void asignar(Cotizacion cotizacion){this.cotizacion=cotizacion;}
  public BigDecimal calcularImporte(){return cantidad.multiply(precioUnitario).setScale(2,RoundingMode.HALF_UP);}
  public UUID getId(){return id;} public String getConcepto(){return concepto;} public String getDescripcion(){return descripcion;} public BigDecimal getCantidad(){return cantidad;} public BigDecimal getPrecioUnitario(){return precioUnitario;} public BigDecimal getImporte(){return calcularImporte();}
}
