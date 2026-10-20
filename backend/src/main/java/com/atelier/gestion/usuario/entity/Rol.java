package com.atelier.gestion.usuario.entity;
import jakarta.persistence.*;
@Entity @Table(name="roles")
public class Rol {
  @Id @Column(name="id_rol") private Short id;
  @Column(nullable=false,unique=true,length=40) private String nombre;
  protected Rol() {}
  public Short getId(){return id;} public String getNombre(){return nombre;}
}

