package com.atelier.gestion.proyecto.service;

import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.proyecto.entity.EstadoProyecto;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PoliticaEstadosProyecto {
  private static final Map<EstadoProyecto,Set<EstadoProyecto>> TRANSICIONES=Map.ofEntries(
    Map.entry(EstadoProyecto.SOLICITUD_REGISTRADA,Set.of(EstadoProyecto.DATOS_INCOMPLETOS,EstadoProyecto.EN_COTIZACION,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.DATOS_INCOMPLETOS,Set.of(EstadoProyecto.SOLICITUD_REGISTRADA,EstadoProyecto.EN_COTIZACION,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.EN_COTIZACION,Set.of(EstadoProyecto.COTIZACION_ENVIADA,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.COTIZACION_ENVIADA,Set.of(EstadoProyecto.EN_COTIZACION,EstadoProyecto.ESPERANDO_PAGO,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.ESPERANDO_PAGO,Set.of(EstadoProyecto.PAGO_EN_REVISION,EstadoProyecto.EN_COTIZACION,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.PAGO_EN_REVISION,Set.of(EstadoProyecto.ESPERANDO_PAGO,EstadoProyecto.PROYECTO_ACTIVO,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.PROYECTO_ACTIVO,Set.of(EstadoProyecto.EN_ESTUDIOS,EstadoProyecto.EN_DISENO,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.EN_ESTUDIOS,Set.of(EstadoProyecto.EN_DISENO,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.EN_DISENO,Set.of(EstadoProyecto.REVISION_CLIENTE,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.REVISION_CLIENTE,Set.of(EstadoProyecto.EN_DISENO,EstadoProyecto.EXPEDIENTE_FINAL,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.EXPEDIENTE_FINAL,Set.of(EstadoProyecto.EN_TRAMITE_PUBLICO,EstadoProyecto.APROBADO,EstadoProyecto.CERRADO,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.EN_TRAMITE_PUBLICO,Set.of(EstadoProyecto.CON_OBSERVACIONES,EstadoProyecto.APROBADO,EstadoProyecto.NO_VIABLE,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.CON_OBSERVACIONES,Set.of(EstadoProyecto.EN_TRAMITE_PUBLICO,EstadoProyecto.NO_VIABLE,EstadoProyecto.CANCELADO)),
    Map.entry(EstadoProyecto.APROBADO,Set.of(EstadoProyecto.CERRADO)),
    Map.entry(EstadoProyecto.NO_VIABLE,Set.of(EstadoProyecto.CERRADO)),
    Map.entry(EstadoProyecto.CERRADO,Set.of()),Map.entry(EstadoProyecto.CANCELADO,Set.of()));
  public void validar(EstadoProyecto actual,EstadoProyecto nuevo,boolean requiereTramite,long obligatorios,long pendientes,String comentario){if(!TRANSICIONES.getOrDefault(actual,Set.of()).contains(nuevo))throw new ReglaNegocioException("Transición no permitida de "+actual+" a "+nuevo);if(nuevo==EstadoProyecto.CANCELADO&&StringUtils.isBlank(comentario))throw new ReglaNegocioException("La cancelación exige un comentario");if(nuevo==EstadoProyecto.EN_COTIZACION&&(obligatorios==0||pendientes>0))throw new ReglaNegocioException("Todos los requerimientos obligatorios deben estar completos antes de cotizar");if(actual==EstadoProyecto.EXPEDIENTE_FINAL){if(requiereTramite&&nuevo!=EstadoProyecto.EN_TRAMITE_PUBLICO&&nuevo!=EstadoProyecto.CANCELADO)throw new ReglaNegocioException("El proyecto requiere trámite público");if(!requiereTramite&&nuevo==EstadoProyecto.EN_TRAMITE_PUBLICO)throw new ReglaNegocioException("El proyecto no requiere trámite público");}}
  public Set<EstadoProyecto> siguientes(EstadoProyecto actual){return Set.copyOf(TRANSICIONES.getOrDefault(actual,Set.of()));}
}
