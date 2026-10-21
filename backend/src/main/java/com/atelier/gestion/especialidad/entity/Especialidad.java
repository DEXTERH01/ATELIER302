package com.atelier.gestion.especialidad.entity;

import jakarta.persistence.*;

@Entity @Table(name="especialidades")
public class Especialidad {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_especialidad") private Short id;
  @Column(nullable=false,unique=true,length=80) private String nombre;
  @Column(length=250) private String descripcion;
  @Column(nullable=false) private boolean activa=true;
  protected Especialidad(){}
  public Especialidad(String nombre,String descripcion){this.nombre=nombre;this.descripcion=descripcion;}
  public void actualizar(String nombre,String descripcion,boolean activa){this.nombre=nombre;this.descripcion=descripcion;this.activa=activa;}
  public Short getId(){return id;} public String getNombre(){return nombre;} public String getDescripcion(){return descripcion;} public boolean isActiva(){return activa;}
}
