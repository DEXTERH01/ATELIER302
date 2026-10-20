package com.atelier.gestion.proyecto.service;

import static com.atelier.gestion.proyecto.entity.EstadoProyecto.*;
import static org.assertj.core.api.Assertions.*;

import com.atelier.gestion.common.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class PoliticaEstadosProyectoTest {
  private final PoliticaEstadosProyecto politica = new PoliticaEstadosProyecto();

  @Test void permiteAvanceInicialConRequerimientosCompletos() {
    assertThatCode(() -> politica.validar(SOLICITUD_REGISTRADA, EN_COTIZACION,
        false, 2, 0, null)).doesNotThrowAnyException();
  }

  @Test void impideCotizarConRequerimientosPendientes() {
    assertThatThrownBy(() -> politica.validar(SOLICITUD_REGISTRADA, EN_COTIZACION,
        false, 2, 1, null)).isInstanceOf(ReglaNegocioException.class)
        .hasMessageContaining("requerimientos");
  }

  @Test void cancelacionSiempreExigeComentario() {
    assertThatThrownBy(() -> politica.validar(EN_COTIZACION, CANCELADO,
        false, 0, 0, " ")).isInstanceOf(ReglaNegocioException.class)
        .hasMessageContaining("comentario");
  }

  @Test void estadosTerminalesNoTienenSalidas() {
    assertThat(politica.siguientes(CERRADO)).isEmpty();
    assertThat(politica.siguientes(CANCELADO)).isEmpty();
  }

  @Test void expedienteRespetaLaNecesidadDeTramite() {
    assertThatCode(() -> politica.validar(EXPEDIENTE_FINAL, EN_TRAMITE_PUBLICO,
        true, 0, 0, null)).doesNotThrowAnyException();
    assertThatThrownBy(() -> politica.validar(EXPEDIENTE_FINAL, APROBADO,
        true, 0, 0, null)).isInstanceOf(ReglaNegocioException.class);
    assertThatCode(() -> politica.validar(EXPEDIENTE_FINAL, CERRADO,
        false, 0, 0, null)).doesNotThrowAnyException();
    assertThatThrownBy(() -> politica.validar(EXPEDIENTE_FINAL, EN_TRAMITE_PUBLICO,
        false, 0, 0, null)).isInstanceOf(ReglaNegocioException.class);
  }
}
