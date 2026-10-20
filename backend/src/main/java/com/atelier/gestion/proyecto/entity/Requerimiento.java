package com.atelier.gestion.proyecto.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "requerimientos")
public class Requerimiento {
  @Id @GeneratedValue @Column(name = "id_requerimiento") private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_proyecto") private Proyecto proyecto;
  @Column(nullable = false, length = 60) private String categoria;
  @Column(nullable = false, columnDefinition = "text") private String descripcion;
  @Column(nullable = false, length = 10) private String prioridad;
  @Column(nullable = false) private boolean obligatorio;
  @Column(nullable = false) private boolean completado;
  protected Requerimiento() {}
  public Requerimiento(Proyecto proyecto,String categoria,String descripcion,String prioridad,boolean obligatorio,boolean completado){this.proyecto=proyecto;this.categoria=categoria;this.descripcion=descripcion;this.prioridad=prioridad;this.obligatorio=obligatorio;this.completado=completado;}
  public void actualizar(String categoria,String descripcion,String prioridad,boolean obligatorio,boolean completado){this.categoria=categoria;this.descripcion=descripcion;this.prioridad=prioridad;this.obligatorio=obligatorio;this.completado=completado;}
  public UUID getId(){return id;} public Proyecto getProyecto(){return proyecto;} public String getCategoria(){return categoria;}
  public String getDescripcion(){return descripcion;} public String getPrioridad(){return prioridad;}
  public boolean isObligatorio(){return obligatorio;} public boolean isCompletado(){return completado;}
}
