package com.atelier.gestion.usuario.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="usuarios")
public class Usuario {
  @Id @GeneratedValue @Column(name="id_usuario") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_rol") private Rol rol;
  @Column(nullable=false,unique=true,length=150) private String correo;
  @Column(name="clave_hash",nullable=false) private String claveHash;
  @Column(nullable=false,length=100) private String nombres;
  @Column(nullable=false,length=120) private String apellidos;
  @Column(length=20) private String telefono;
  @Column(nullable=false) private boolean activo=true;
  @Column(name="ultimo_acceso") private OffsetDateTime ultimoAcceso;
  protected Usuario() {}
  public Usuario(Rol rol,String correo,String claveHash,String nombres,String apellidos,String telefono){this.rol=rol;this.correo=correo;this.claveHash=claveHash;this.nombres=nombres;this.apellidos=apellidos;this.telefono=telefono;}
  public void registrarAcceso(OffsetDateTime fecha){this.ultimoAcceso=fecha;}
  public void actualizar(String nombres,String apellidos,String telefono){this.nombres=nombres;this.apellidos=apellidos;this.telefono=telefono;}
  public void cambiarActivo(boolean activo){this.activo=activo;}
  public UUID getId(){return id;} public Rol getRol(){return rol;} public String getCorreo(){return correo;} public String getClaveHash(){return claveHash;} public String getNombres(){return nombres;} public String getApellidos(){return apellidos;} public String getTelefono(){return telefono;} public boolean isActivo(){return activo;} public OffsetDateTime getUltimoAcceso(){return ultimoAcceso;}
}

