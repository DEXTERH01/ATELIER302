package com.atelier.gestion.expediente.entity;

import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity @Table(name="expedientes")
public class Expediente {
  @Id @GeneratedValue @Column(name="id_expediente") private UUID id;
  @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_proyecto",unique=true) private Proyecto proyecto;
  @Column(nullable=false,unique=true,length=30) private String codigo;
  @Column(nullable=false,length=20) private String estado;
  @Column(name="creado_en",nullable=false,updatable=false) private OffsetDateTime creadoEn;
  @Column(name="cerrado_en") private OffsetDateTime cerradoEn;
  protected Expediente() {}
  public Expediente(Proyecto proyecto,String codigo){this.proyecto=proyecto;this.codigo=codigo;this.estado="ABIERTO";this.creadoEn=OffsetDateTime.now(ZoneOffset.UTC);}
  public void completar(){if("ABIERTO".equals(estado))estado="COMPLETO";}
  public void cerrar(OffsetDateTime ahora){estado="ARCHIVADO";cerradoEn=ahora;}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public String getCodigo(){return codigo;} public String getEstado(){return estado;} public OffsetDateTime getCreadoEn(){return creadoEn;} public OffsetDateTime getCerradoEn(){return cerradoEn;}
}
