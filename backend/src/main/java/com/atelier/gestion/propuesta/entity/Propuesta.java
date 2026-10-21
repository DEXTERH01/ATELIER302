package com.atelier.gestion.propuesta.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="propuestas")
public class Propuesta {
  @Id @GeneratedValue @Column(name="id_propuesta") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="creada_por") private Empleado creadaPor;
  @Column(name="numero_version",nullable=false) private short numeroVersion;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private TipoPropuesta tipo;
  @Column(columnDefinition="text") private String descripcion;
  @Column(name="url_vista_previa",length=600) private String urlVistaPrevia;
  @Column(name="vista_previa_public_id",length=300) private String vistaPreviaPublicId;
  @Column(name="vista_previa_nombre",length=200) private String vistaPreviaNombre;
  @Column(name="vista_previa_tipo_mime",length=150) private String vistaPreviaTipoMime;
  @Column(name="vista_previa_tamano_bytes") private Long vistaPreviaTamanoBytes;
  @Column(name="vista_previa_resource_type",length=20) private String vistaPreviaResourceType;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private EstadoPropuesta estado;
  @Column(name="enviada_en") private OffsetDateTime enviadaEn;
  @Column(name="respondida_en") private OffsetDateTime respondidaEn;
  @Column(name="creado_en",nullable=false,updatable=false) private OffsetDateTime creadoEn;

  protected Propuesta() {}

  public Propuesta(Proyecto proyecto,Empleado creadaPor,short numeroVersion,TipoPropuesta tipo,
      String descripcion,String url,String publicId,String nombre,String mime,long tamano,
      String resourceType,OffsetDateTime ahora){
    this.proyecto=proyecto;this.creadaPor=creadaPor;this.numeroVersion=numeroVersion;this.tipo=tipo;
    this.descripcion=descripcion;this.urlVistaPrevia=url;this.vistaPreviaPublicId=publicId;
    this.vistaPreviaNombre=nombre;this.vistaPreviaTipoMime=mime;this.vistaPreviaTamanoBytes=tamano;
    this.vistaPreviaResourceType=resourceType;this.estado=EstadoPropuesta.BORRADOR;this.creadoEn=ahora;
  }

  public void enviar(OffsetDateTime ahora){estado=EstadoPropuesta.ENVIADA;enviadaEn=ahora;}
  public void observar(OffsetDateTime ahora){estado=EstadoPropuesta.OBSERVADA;respondidaEn=ahora;}
  public void darConformidad(OffsetDateTime ahora){estado=EstadoPropuesta.CONFORME;respondidaEn=ahora;}
  public void reemplazar(){if(estado==EstadoPropuesta.OBSERVADA)estado=EstadoPropuesta.REEMPLAZADA;}

  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;}
  public Empleado getCreadaPor(){return creadaPor;} public short getNumeroVersion(){return numeroVersion;}
  public TipoPropuesta getTipo(){return tipo;} public String getDescripcion(){return descripcion;}
  public String getUrlVistaPrevia(){return urlVistaPrevia;} public String getVistaPreviaPublicId(){return vistaPreviaPublicId;}
  public String getVistaPreviaNombre(){return vistaPreviaNombre;} public String getVistaPreviaTipoMime(){return vistaPreviaTipoMime;}
  public Long getVistaPreviaTamanoBytes(){return vistaPreviaTamanoBytes;} public String getVistaPreviaResourceType(){return vistaPreviaResourceType;}
  public EstadoPropuesta getEstado(){return estado;} public OffsetDateTime getEnviadaEn(){return enviadaEn;}
  public OffsetDateTime getRespondidaEn(){return respondidaEn;} public OffsetDateTime getCreadoEn(){return creadoEn;}
}
