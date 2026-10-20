package com.atelier.gestion.cliente.entity;
import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="clientes")
public class Cliente {
  @Id @GeneratedValue @Column(name="id_cliente") private UUID id;
  @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",unique=true) private Usuario usuario;
  @Column(name="tipo_persona",nullable=false,length=15) private String tipoPersona;
  @Column(name="tipo_documento",nullable=false,length=10) private String tipoDocumento;
  @Column(name="numero_documento",nullable=false,unique=true,length=20) private String numeroDocumento;
  @Column(name="razon_social",length=180) private String razonSocial;
  @Column(length=250) private String direccion;
  @Column(length=100) private String distrito;
  protected Cliente() {}
  public Cliente(Usuario usuario,String tipoPersona,String tipoDocumento,String numeroDocumento,String razonSocial,String direccion,String distrito){this.usuario=usuario;this.tipoPersona=tipoPersona;this.tipoDocumento=tipoDocumento;this.numeroDocumento=numeroDocumento;this.razonSocial=razonSocial;this.direccion=direccion;this.distrito=distrito;}
  public UUID getId(){return id;}
}

