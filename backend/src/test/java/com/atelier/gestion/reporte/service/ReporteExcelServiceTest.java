package com.atelier.gestion.reporte.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atelier.gestion.asignacion.repository.AsignacionRepository;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.pago.repository.PagoRepository;
import com.atelier.gestion.proyecto.entity.EstadoProyecto;
import com.atelier.gestion.proyecto.entity.Proyecto;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.Usuario;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ReporteExcelServiceTest {
  private final ProyectoRepository proyectos = mock(ProyectoRepository.class);
  private final PagoRepository pagos = mock(PagoRepository.class);
  private final AsignacionRepository asignaciones = mock(AsignacionRepository.class);
  private final EstudioTecnicoRepository estudios = mock(EstudioTecnicoRepository.class);
  private final SesionActual sesion = mock(SesionActual.class);
  private final AuditoriaService auditoria = mock(AuditoriaService.class);
  private final ReporteExcelService service = new ReporteExcelService(proyectos, pagos, asignaciones, estudios, sesion, auditoria);

  @Test void generaLibroConCabeceraDatosYAuditoria() throws Exception {
    Proyecto proyecto = mock(Proyecto.class); Cliente cliente = mock(Cliente.class); Usuario clienteUsuario = mock(Usuario.class);
    when(proyecto.getCodigo()).thenReturn("PROY-2026-0001"); when(proyecto.getNombre()).thenReturn("Casa Rivera");
    when(proyecto.getCliente()).thenReturn(cliente); when(cliente.getUsuario()).thenReturn(clienteUsuario);
    when(clienteUsuario.getNombres()).thenReturn("Ana"); when(clienteUsuario.getApellidos()).thenReturn("Rivera");
    when(proyecto.getEncargado()).thenReturn(null); when(proyecto.getTipoProyecto()).thenReturn("Vivienda");
    when(proyecto.getEstado()).thenReturn(EstadoProyecto.PROYECTO_ACTIVO); when(proyecto.getFechaSolicitud()).thenReturn(LocalDate.of(2026, 10, 21));
    when(proyecto.getFechaFinEstimada()).thenReturn(LocalDate.of(2027, 3, 20)); when(proyecto.isRequiereTramitePublico()).thenReturn(true);
    when(proyectos.findAllByOrderByFechaSolicitudDesc()).thenReturn(List.of(proyecto)); when(sesion.usuario()).thenReturn(mock(Usuario.class));

    byte[] archivo = service.proyectosPorEstado();

    assertThat(archivo).isNotEmpty();
    try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
      assertThat(libro.getSheetAt(0).getSheetName()).isEqualTo("Proyectos por estado");
      assertThat(libro.getSheetAt(0).getRow(0).getCell(0).getStringCellValue()).isEqualTo("Código");
      assertThat(libro.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()).isEqualTo("PROY-2026-0001");
      assertThat(libro.getSheetAt(0).getRow(1).getCell(5).getStringCellValue()).isEqualTo("PROYECTO_ACTIVO");
    }
    verify(auditoria).registrar(any(), eq("reportes"), isNull(), eq("EXPORT"), argThat(detalle -> "PROYECTOS_POR_ESTADO".equals(detalle.get("reporte"))));
  }
}
