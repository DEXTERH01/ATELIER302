package com.atelier.gestion.pago.entity;

import com.atelier.gestion.cotizacion.entity.Cotizacion;
import com.atelier.gestion.empleado.entity.Empleado;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="pagos")
public class Pago {
  @Id @GeneratedValue @Column(name="id_pago") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_cotizacion") private Cotizacion cotizacion;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="validado_por") private Empleado validadoPor;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private TipoPago tipo;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MetodoPago metodo;
  @Column(nullable=false,precision=12,scale=2) private BigDecimal monto;
  @Column(nullable=false,length=3) private String moneda;
  @Column(name="numero_operacion",length=80) private String numeroOperacion;
  @Column(name="comprobante_url",nullable=false,length=500) private String comprobanteUrl;
  @Column(name="comprobante_public_id",nullable=false,length=250) private String comprobantePublicId;
  @Column(name="comprobante_tipo_mime",nullable=false,length=150) private String comprobanteTipoMime;
  @Column(name="comprobante_tamano_bytes",nullable=false) private Long comprobanteTamanoBytes;
  @Column(name="comprobante_categoria",nullable=false,length=40) private String comprobanteCategoria;
  @Column(name="comprobante_resource_type",nullable=false,length=20) private String comprobanteResourceType;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) private EstadoPago estado;
  @Column(name="motivo_rechazo",length=500) private String motivoRechazo;
  @Column(name="pagado_en",nullable=false) private OffsetDateTime pagadoEn;
  @Column(name="validado_en") private OffsetDateTime validadoEn;
  protected Pago() {}
  public Pago(Cotizacion cotizacion,TipoPago tipo,MetodoPago metodo,BigDecimal monto,String numeroOperacion,String url,String publicId,String resourceType,String mime,long tamano,OffsetDateTime pagadoEn){this.cotizacion=cotizacion;this.tipo=tipo;this.metodo=metodo;this.monto=monto;this.moneda="PEN";this.numeroOperacion=numeroOperacion;this.comprobanteUrl=url;this.comprobantePublicId=publicId;this.comprobanteResourceType=resourceType;this.comprobanteTipoMime=mime;this.comprobanteTamanoBytes=tamano;this.comprobanteCategoria="COMPROBANTE_PAGO";this.estado=EstadoPago.PENDIENTE;this.pagadoEn=pagadoEn;}
  public void validar(Empleado empleado,OffsetDateTime ahora){estado=EstadoPago.VALIDADO;validadoPor=empleado;validadoEn=ahora;motivoRechazo=null;}
  public void rechazar(Empleado empleado,String motivo,OffsetDateTime ahora){estado=EstadoPago.RECHAZADO;validadoPor=empleado;validadoEn=ahora;motivoRechazo=motivo;}
  public UUID getId(){return id;} public Cotizacion getCotizacion(){return cotizacion;} public Empleado getValidadoPor(){return validadoPor;} public TipoPago getTipo(){return tipo;} public MetodoPago getMetodo(){return metodo;} public BigDecimal getMonto(){return monto;} public String getMoneda(){return moneda;} public String getNumeroOperacion(){return numeroOperacion;} public String getComprobanteUrl(){return comprobanteUrl;} public String getComprobantePublicId(){return comprobantePublicId;} public String getComprobanteTipoMime(){return comprobanteTipoMime;} public Long getComprobanteTamanoBytes(){return comprobanteTamanoBytes;} public String getComprobanteCategoria(){return comprobanteCategoria;} public String getComprobanteResourceType(){return comprobanteResourceType;} public EstadoPago getEstado(){return estado;} public String getMotivoRechazo(){return motivoRechazo;} public OffsetDateTime getPagadoEn(){return pagadoEn;} public OffsetDateTime getValidadoEn(){return validadoEn;}
}
