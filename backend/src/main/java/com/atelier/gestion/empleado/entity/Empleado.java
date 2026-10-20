package com.atelier.gestion.empleado.entity;

import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "empleados")
public class Empleado {
  @Id @GeneratedValue @Column(name = "id_empleado") private UUID id;
  @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_usuario", unique = true) private Usuario usuario;
  @Column(name = "codigo_colegiatura", length = 40) private String codigoColegiatura;
  @Column(nullable = false, length = 80) private String cargo;
  @Column(nullable = false, length = 15) private String tipo;
  @Column(nullable = false) private boolean disponible = true;

  protected Empleado() {}
  public Empleado(Usuario usuario, String codigoColegiatura, String cargo, String tipo, boolean disponible) {
    this.usuario = usuario; this.codigoColegiatura = codigoColegiatura; this.cargo = cargo;
    this.tipo = tipo; this.disponible = disponible;
  }
  public void actualizar(String codigoColegiatura, String cargo, String tipo, boolean disponible) {
    this.codigoColegiatura = codigoColegiatura; this.cargo = cargo; this.tipo = tipo; this.disponible = disponible;
  }
  public UUID getId(){return id;} public Usuario getUsuario(){return usuario;}
  public String getCodigoColegiatura(){return codigoColegiatura;} public String getCargo(){return cargo;}
  public String getTipo(){return tipo;} public boolean isDisponible(){return disponible;}
}
