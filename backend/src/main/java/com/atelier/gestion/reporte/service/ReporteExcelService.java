package com.atelier.gestion.reporte.service;

import com.atelier.gestion.asignacion.repository.AsignacionRepository;
import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.pago.repository.PagoRepository;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.security.SesionActual;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteExcelService {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
  private final ProyectoRepository proyectos;
  private final PagoRepository pagos;
  private final AsignacionRepository asignaciones;
  private final EstudioTecnicoRepository estudios;
  private final SesionActual sesion;
  private final AuditoriaService auditoria;

  public ReporteExcelService(ProyectoRepository proyectos, PagoRepository pagos,
      AsignacionRepository asignaciones, EstudioTecnicoRepository estudios, SesionActual sesion,
      AuditoriaService auditoria) {
    this.proyectos = proyectos; this.pagos = pagos; this.asignaciones = asignaciones;
    this.estudios = estudios; this.sesion = sesion; this.auditoria = auditoria;
  }

  @Transactional
  public byte[] proyectosPorEstado() {
    var filas = proyectos.findAllByOrderByFechaSolicitudDesc().stream().map(p -> List.<Object>of(
        p.getCodigo(), p.getNombre(), nombre(p.getCliente().getUsuario().getNombres(), p.getCliente().getUsuario().getApellidos()),
        p.getEncargado() == null ? "Sin asignar" : nombre(p.getEncargado().getUsuario().getNombres(), p.getEncargado().getUsuario().getApellidos()),
        p.getTipoProyecto(), p.getEstado().name(), fecha(p.getFechaSolicitud()), fecha(p.getFechaFinEstimada()),
        p.isRequiereTramitePublico() ? "Sí" : "No")).toList();
    return exportar("Proyectos por estado", List.of("Código", "Proyecto", "Cliente", "Encargado", "Tipo", "Estado", "Solicitud", "Fin estimado", "Trámite público"), filas, "PROYECTOS_POR_ESTADO");
  }

  @Transactional
  public byte[] pagos() {
    var filas = pagos.findAllByOrderByPagadoEnDesc().stream().map(p -> List.<Object>of(
        p.getCotizacion().getProyecto().getCodigo(), p.getCotizacion().getNumero(), p.getCotizacion().getProyecto().getNombre(),
        p.getTipo().name(), p.getMetodo().name(), p.getMonto(), p.getMoneda(), p.getEstado().name(),
        p.getNumeroOperacion() == null ? "" : p.getNumeroOperacion(), p.getPagadoEn().toLocalDate().format(FECHA),
        p.getValidadoEn() == null ? "" : p.getValidadoEn().toLocalDate().format(FECHA))).toList();
    return exportar("Pagos", List.of("Proyecto", "Cotización", "Nombre", "Tipo", "Método", "Monto", "Moneda", "Estado", "Operación", "Pagado", "Validado"), filas, "PAGOS");
  }

  @Transactional
  public byte[] cargaTecnica() {
    var filasAsignaciones = asignaciones.findAllByOrderByFechaAsignacionDesc().stream().map(a -> List.<Object>of(
        "ASIGNACIÓN", a.getProyecto().getCodigo(), a.getProyecto().getNombre(),
        nombre(a.getEmpleado().getUsuario().getNombres(), a.getEmpleado().getUsuario().getApellidos()),
        a.getEspecialidad().getNombre(), a.getRolProyecto(), a.getEstado().name(), fecha(a.getFechaAsignacion()), fecha(a.getFechaFin()))).toList();
    var filasEstudios = estudios.findAllByOrderByFechaSolicitudDesc().stream().map(e -> List.<Object>of(
        "ESTUDIO", e.getProyecto().getCodigo(), e.getProyecto().getNombre(),
        e.getResponsable() == null ? "Sin responsable" : nombre(e.getResponsable().getUsuario().getNombres(), e.getResponsable().getUsuario().getApellidos()),
        e.getTipo(), e.isRequerido() ? "Obligatorio" : "Opcional", e.getEstado().name(), fecha(e.getFechaSolicitud()), fecha(e.getFechaResultado()))).toList();
    var filas = new java.util.ArrayList<List<Object>>(filasAsignaciones); filas.addAll(filasEstudios);
    return exportar("Carga técnica", List.of("Registro", "Proyecto", "Nombre", "Responsable", "Especialidad / tipo", "Rol / requisito", "Estado", "Inicio / solicitud", "Fin / resultado"), filas, "CARGA_TECNICA");
  }

  private byte[] exportar(String hoja, List<String> encabezados, List<List<Object>> filas, String tipo) {
    try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Sheet sheet = libro.createSheet(hoja);
      sheet.createFreezePane(0, 1); sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, encabezados.size() - 1));
      CellStyle estiloCabecera = libro.createCellStyle(); Font fuente = libro.createFont(); fuente.setBold(true); fuente.setColor(IndexedColors.WHITE.getIndex()); estiloCabecera.setFont(fuente); estiloCabecera.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex()); estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      CellStyle dinero = libro.createCellStyle(); dinero.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));
      Row cabecera = sheet.createRow(0);
      for (int i = 0; i < encabezados.size(); i++) { Cell celda = cabecera.createCell(i); celda.setCellValue(encabezados.get(i)); celda.setCellStyle(estiloCabecera); }
      for (int fila = 0; fila < filas.size(); fila++) {
        Row row = sheet.createRow(fila + 1); List<Object> valores = filas.get(fila);
        for (int columna = 0; columna < valores.size(); columna++) {
          Cell celda = row.createCell(columna); Object valor = valores.get(columna);
          if (valor instanceof BigDecimal monto) { celda.setCellValue(monto.doubleValue()); celda.setCellStyle(dinero); } else celda.setCellValue(valor == null ? "" : valor.toString());
        }
      }
      for (int i = 0; i < encabezados.size(); i++) sheet.autoSizeColumn(i); 
      libro.write(salida); registrar(tipo, filas.size()); return salida.toByteArray();
    } catch (IOException e) { throw new IllegalStateException("No se pudo generar el archivo Excel", e); }
  }

  private void registrar(String reporte, int filas) { auditoria.registrar(sesion.usuario(), "reportes", null, "EXPORT", Map.of("reporte", reporte, "formato", "XLSX", "filas", filas)); }
  private String fecha(LocalDate fecha) { return fecha == null ? "" : fecha.format(FECHA); }
  private String nombre(String nombres, String apellidos) { return nombres + " " + apellidos; }
}
