package com.atelier.gestion.proyecto.entity;

import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "historial_estados")
public class HistorialEstado {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_historial") private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cambiado_por") private Usuario cambiadoPor;
  @Enumerated(EnumType.STRING) @Column(name = "estado_anterior", length = 35) private EstadoProyecto estadoAnterior;
  @Enumerated(EnumType.STRING) @Column(name = "estado_nuevo", nullable = false, length = 35) private EstadoProyecto estadoNuevo;
  @Column(length = 500) private String comentario;
  @Column(name = "cambiado_en", nullable = false) private OffsetDateTime cambiadoEn;
  protected HistorialEstado() {}
  public HistorialEstado(Proyecto proyecto,Usuario usuario,EstadoProyecto anterior,EstadoProyecto nuevo,String comentario){this.proyecto=proyecto;this.cambiadoPor=usuario;this.estadoAnterior=anterior;this.estadoNuevo=nuevo;this.comentario=comentario;this.cambiadoEn=OffsetDateTime.now(ZoneOffset.UTC);}
  public Long getId(){return id;} public EstadoProyecto getEstadoAnterior(){return estadoAnterior;} public EstadoProyecto getEstadoNuevo(){return estadoNuevo;}
  public Usuario getCambiadoPor(){return cambiadoPor;} public String getComentario(){return comentario;} public OffsetDateTime getCambiadoEn(){return cambiadoEn;}
}
