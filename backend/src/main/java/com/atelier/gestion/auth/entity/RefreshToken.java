package com.atelier.gestion.auth.entity;
import com.atelier.gestion.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="refresh_tokens")
public class RefreshToken {
  @Id @Column(name="id_refresh_token") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario") private Usuario usuario;
  @Column(name="familia_id",nullable=false) private UUID familiaId;
  @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash;
  @Column(name="emitido_en",nullable=false) private OffsetDateTime emitidoEn;
  @Column(name="expira_en",nullable=false) private OffsetDateTime expiraEn;
  @Column(name="revocado_en") private OffsetDateTime revocadoEn;
  @Column(name="reemplazado_por") private UUID reemplazadoPor;
  @Column(name="ip_origen",length=45) private String ipOrigen;
  @Column(name="user_agent",length=300) private String userAgent;
  protected RefreshToken() {}
  public RefreshToken(Usuario usuario,UUID familiaId,String hash,OffsetDateTime emitido,OffsetDateTime expira,String ip,String agente){this.id=UUID.randomUUID();this.usuario=usuario;this.familiaId=familiaId;this.tokenHash=hash;this.emitidoEn=emitido;this.expiraEn=expira;this.ipOrigen=ip;this.userAgent=agente;}
  public void revocar(OffsetDateTime fecha,UUID reemplazo){this.revocadoEn=fecha;this.reemplazadoPor=reemplazo;}
  public boolean estaRevocado(){return revocadoEn!=null;} public boolean estaExpirado(OffsetDateTime ahora){return !expiraEn.isAfter(ahora);}
  public UUID getId(){return id;} public Usuario getUsuario(){return usuario;} public UUID getFamiliaId(){return familiaId;}
}

