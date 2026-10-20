package com.atelier.gestion.asignacion.entity;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="asignaciones")
public class Asignacion {
  @Id @Column(name="id_asignacion") private UUID id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_proyecto") private Proyecto proyecto;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_empleado") private Empleado empleado;
  protected Asignacion() {}
}
