package com.atelier.gestion.proyecto.service;

import jakarta.persistence.EntityManager;
import java.time.*;
import org.springframework.stereotype.Service;

@Service
public class CorrelativoProyectoService {
  private final EntityManager entityManager;
  public CorrelativoProyectoService(EntityManager entityManager){this.entityManager=entityManager;}
  public String siguiente(){int anio=LocalDate.now(ZoneId.of("America/Lima")).getYear();Number numero=(Number)entityManager.createNativeQuery("""
    insert into correlativos_proyecto(anio,ultimo_numero,actualizado_en) values (:anio,1,now())
    on conflict (anio) do update set ultimo_numero=correlativos_proyecto.ultimo_numero+1,actualizado_en=now()
    returning ultimo_numero
    """).setParameter("anio",anio).getSingleResult();return "PROY-%d-%04d".formatted(anio,numero.intValue());}
}
