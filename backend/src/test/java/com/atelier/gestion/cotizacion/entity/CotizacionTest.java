package com.atelier.gestion.cotizacion.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.proyecto.entity.Proyecto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class CotizacionTest {
  @Test void calculaImportesYAdelantoConDosDecimales() {
    Cotizacion c=new Cotizacion(mock(Proyecto.class),mock(Empleado.class),"COT-2026-0001-I01",
        (short)1,TipoCotizacion.INICIAL,new BigDecimal("50.00"));
    c.actualizar(new BigDecimal("50.00"),null,List.of(
        new DetalleCotizacion("Diseño",null,new BigDecimal("2.00"),new BigDecimal("100.005"))),
        new BigDecimal("18.00"));
    assertThat(c.getSubtotal()).isEqualByComparingTo("200.02");
    assertThat(c.getImpuesto()).isEqualByComparingTo("36.00");
    assertThat(c.getTotal()).isEqualByComparingTo("236.02");
    assertThat(c.adelantoRequerido()).isEqualByComparingTo("118.01");
  }

  @Test void envioOtorgaExactamenteDiezDiasDeVigencia() {
    Cotizacion c=new Cotizacion(mock(Proyecto.class),mock(Empleado.class),"COT-2026-0001-I01",
        (short)1,TipoCotizacion.INICIAL,new BigDecimal("50.00"));
    OffsetDateTime ahora=OffsetDateTime.of(2026,10,20,12,0,0,0,ZoneOffset.UTC);
    c.enviar(ahora);
    assertThat(c.getEstado()).isEqualTo(EstadoCotizacion.ENVIADA);
    assertThat(c.getVenceEn()).isEqualTo(ahora.plusDays(10));
  }
}
