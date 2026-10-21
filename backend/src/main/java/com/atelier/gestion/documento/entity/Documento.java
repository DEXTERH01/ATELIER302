package com.atelier.gestion.documento.entity;

import com.atelier.gestion.estudio.entity.EstudioTecnico;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.tramite.entity.TramitePublico;
import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="documentos")
public class Documento {
  @Id @GeneratedValue @Column(name="id_documento") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_expediente") private Expediente expediente;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="subido_por") private Usuario subidoPor;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_estudio") private EstudioTecnico estudio;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_tramite") private TramitePublico tramite;
  @Column(nullable=false,length=200) private String nombre;
  @Column(name="tipo_documento",nullable=false,length=50) private String tipoDocumento;
  @Column(nullable=false) private short version;
  @Column(name="url_cloudinary",nullable=false,length=600) private String urlCloudinary;
  @Column(name="public_id_cloudinary",nullable=false,length=300) private String publicIdCloudinary;
  @Column(length=20) private String formato;
  @Column(name="tipo_mime",nullable=false,length=150) private String tipoMime;
  @Column(name="tamano_bytes",nullable=false) private long tamanoBytes;
  @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private CategoriaDocumento categoria;
  @Column(name="resource_type",nullable=false,length=20) private String resourceType;
  @Column(name="visible_cliente",nullable=false) private boolean visibleCliente;
  @Column(nullable=false) private boolean vigente;
  @Column(name="creado_en",nullable=false,updatable=false) private OffsetDateTime creadoEn;
  protected Documento(){}
  public Documento(Expediente expediente,Usuario subidoPor,EstudioTecnico estudio,TramitePublico tramite,String nombre,String tipoDocumento,short version,String url,String publicId,String formato,String mime,long tamano,CategoriaDocumento categoria,String resourceType,boolean visibleCliente,OffsetDateTime ahora){this.expediente=expediente;this.subidoPor=subidoPor;this.estudio=estudio;this.tramite=tramite;this.nombre=nombre;this.tipoDocumento=tipoDocumento;this.version=version;this.urlCloudinary=url;this.publicIdCloudinary=publicId;this.formato=formato;this.tipoMime=mime;this.tamanoBytes=tamano;this.categoria=categoria;this.resourceType=resourceType;this.visibleCliente=visibleCliente;this.vigente=true;this.creadoEn=ahora;}
  public void retirar(){vigente=false;}
  public UUID getId(){return id;} public Expediente getExpediente(){return expediente;} public Usuario getSubidoPor(){return subidoPor;} public EstudioTecnico getEstudio(){return estudio;} public TramitePublico getTramite(){return tramite;} public String getNombre(){return nombre;} public String getTipoDocumento(){return tipoDocumento;} public short getVersion(){return version;} public String getUrlCloudinary(){return urlCloudinary;} public String getPublicIdCloudinary(){return publicIdCloudinary;} public String getFormato(){return formato;} public String getTipoMime(){return tipoMime;} public long getTamanoBytes(){return tamanoBytes;} public CategoriaDocumento getCategoria(){return categoria;} public String getResourceType(){return resourceType;} public boolean isVisibleCliente(){return visibleCliente;} public boolean isVigente(){return vigente;} public OffsetDateTime getCreadoEn(){return creadoEn;}
}
