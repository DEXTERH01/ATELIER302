package com.atelier.gestion.auditoria.entity;

import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auditoria")
public class Auditoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_auditoria")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario")
  private Usuario usuario;

  @Column(name = "tabla_afectada", nullable = false, length = 80)
  private String tablaAfectada;

  @Column(name = "id_registro", length = 80)
  private String idRegistro;

  @Column(nullable = false, length = 15)
  private String accion;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> detalle;

  @Column(name = "creado_en", nullable = false)
  private OffsetDateTime creadoEn;

  protected Auditoria() {}

  public Auditoria(Usuario usuario, String tablaAfectada, String idRegistro, String accion,
      Map<String, Object> detalle) {
    this.usuario = usuario;
    this.tablaAfectada = tablaAfectada;
    this.idRegistro = idRegistro;
    this.accion = accion;
    this.detalle = detalle;
    this.creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
  }

  public Long getId() { return id; }
  public Usuario getUsuario() { return usuario; }
  public String getTablaAfectada() { return tablaAfectada; }
  public String getIdRegistro() { return idRegistro; }
  public String getAccion() { return accion; }
  public Map<String, Object> getDetalle() { return detalle; }
  public OffsetDateTime getCreadoEn() { return creadoEn; }
}
