package com.atelier.gestion.proyecto.service;

import static com.atelier.gestion.proyecto.dto.ProyectoDtos.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.cliente.entity.Cliente;
import com.atelier.gestion.cliente.repository.ClienteRepository;
import com.atelier.gestion.cliente.service.ClienteService;
import com.atelier.gestion.common.api.PaginaRespuesta;
import com.atelier.gestion.common.exception.RecursoNoEncontradoException;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.empleado.entity.Empleado;
import com.atelier.gestion.empleado.service.EmpleadoService;
import com.atelier.gestion.proyecto.entity.*;
import com.atelier.gestion.proyecto.repository.*;
import com.atelier.gestion.security.SesionActual;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProyectoService {
  private static final Set<EstadoProyecto> ESTADOS_EDITABLES_CLIENTE =
      Set.of(EstadoProyecto.SOLICITUD_REGISTRADA, EstadoProyecto.DATOS_INCOMPLETOS);
  private static final Set<EstadoProyecto> ESTADOS_GESTIONADOS_POR_FLUJO =
      Set.of(EstadoProyecto.COTIZACION_ENVIADA, EstadoProyecto.ESPERANDO_PAGO,
          EstadoProyecto.PAGO_EN_REVISION, EstadoProyecto.PROYECTO_ACTIVO);

  private final ProyectoRepository proyectos;
  private final RequerimientoRepository requerimientos;
  private final HistorialEstadoRepository historial;
  private final ClienteRepository clientes;
  private final ClienteService clienteService;
  private final EmpleadoService empleadoService;
  private final CorrelativoProyectoService correlativos;
  private final PoliticaEstadosProyecto politica;
  private final SesionActual sesion;
  private final AuditoriaService auditoria;

  public ProyectoService(ProyectoRepository proyectos, RequerimientoRepository requerimientos,
      HistorialEstadoRepository historial, ClienteRepository clientes, ClienteService clienteService,
      EmpleadoService empleadoService, CorrelativoProyectoService correlativos,
      PoliticaEstadosProyecto politica, SesionActual sesion, AuditoriaService auditoria) {
    this.proyectos = proyectos;
    this.requerimientos = requerimientos;
    this.historial = historial;
    this.clientes = clientes;
    this.clienteService = clienteService;
    this.empleadoService = empleadoService;
    this.correlativos = correlativos;
    this.politica = politica;
    this.sesion = sesion;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public PaginaRespuesta<Respuesta> listar(String q, int pagina, int tamano) {
    PageRequest pageable = pagina(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaSolicitud"));
    String texto = StringUtils.trimToEmpty(q);
    Usuario actual = sesion.usuario();
    Page<Proyecto> resultado = switch (actual.getRol().getNombre()) {
      case "ADMINISTRADOR", "ENCARGADO" ->
          proyectos.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(texto, texto, pageable);
      case "CLIENTE" -> proyectos.buscarDelCliente(actual.getCorreo(), texto, pageable);
      case "ESPECIALISTA" -> proyectos.buscarAsignados(actual.getCorreo(), texto, pageable);
      default -> throw new AccessDeniedException("Rol sin acceso a proyectos");
    };
    return PaginaRespuesta.desde(resultado.map(this::dto));
  }

  @Transactional(readOnly = true)
  public PaginaRespuesta<Respuesta> listarCliente(UUID clienteId, int pagina, int tamano) {
    Cliente cliente = clienteService.buscar(clienteId);
    verificarClienteVisible(cliente);
    return PaginaRespuesta.desde(proyectos.findByClienteId(clienteId,
        pagina(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaSolicitud"))).map(this::dto));
  }

  @Transactional(readOnly = true)
  public Respuesta obtener(UUID id) {
    return dto(buscarVisible(id));
  }

  @Transactional
  public Respuesta crear(Crear req) {
    Usuario actual = sesion.usuario();
    Cliente cliente;
    Empleado encargado = null;
    if ("CLIENTE".equals(actual.getRol().getNombre())) {
      cliente = clienteService.clienteActual();
    } else if (esGestor(actual)) {
      if (req.clienteId() == null) throw new ReglaNegocioException("Debe seleccionar un cliente");
      cliente = clienteService.buscar(req.clienteId());
      encargado = req.encargadoId() == null ? null : empleadoService.buscar(req.encargadoId());
    } else {
      throw new AccessDeniedException("No puede crear proyectos");
    }
    validarFecha(req.fechaFinEstimada());
    Proyecto proyecto = new Proyecto(correlativos.siguiente(), cliente, encargado,
        StringUtils.trim(req.nombre()), StringUtils.trimToNull(req.descripcion()),
        StringUtils.trim(req.tipoProyecto()), StringUtils.trimToNull(req.ubicacion()),
        req.areaTerreno(), req.requiereTramitePublico());
    proyecto.actualizar(proyecto.getNombre(), proyecto.getDescripcion(), proyecto.getTipoProyecto(),
        proyecto.getUbicacion(), proyecto.getAreaTerreno(), proyecto.isRequiereTramitePublico(),
        encargado, req.fechaFinEstimada());
    proyectos.save(proyecto);
    historial.save(new HistorialEstado(proyecto, actual, null,
        EstadoProyecto.SOLICITUD_REGISTRADA, "Proyecto registrado"));
    auditoria.registrar(actual, "proyectos", proyecto.getId(), "INSERT",
        Map.of("codigo", proyecto.getCodigo()));
    return dto(proyecto);
  }

  @Transactional
  public Respuesta actualizar(UUID id, Actualizar req) {
    Proyecto proyecto = proyectos.bloquear(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
    Usuario actual = sesion.usuario();
    verificarVisible(proyecto, actual);
    boolean gestor = esGestor(actual);
    if (!gestor && !("CLIENTE".equals(actual.getRol().getNombre())
        && ESTADOS_EDITABLES_CLIENTE.contains(proyecto.getEstado()))) {
      throw new AccessDeniedException("El proyecto ya no puede ser editado por el cliente");
    }
    validarFecha(req.fechaFinEstimada());
    Empleado encargado = gestor
        ? (req.encargadoId() == null ? null : empleadoService.buscar(req.encargadoId()))
        : proyecto.getEncargado();
    proyecto.actualizar(StringUtils.trim(req.nombre()), StringUtils.trimToNull(req.descripcion()),
        StringUtils.trim(req.tipoProyecto()), StringUtils.trimToNull(req.ubicacion()), req.areaTerreno(),
        req.requiereTramitePublico(), encargado, req.fechaFinEstimada());
    auditoria.registrar(actual, "proyectos", id, "UPDATE", Map.of("operacion", "DATOS"));
    return dto(proyecto);
  }

  @Transactional
  public Respuesta transicionar(UUID id, Transicion req) {
    Usuario actual = sesion.usuario();
    if (!esGestor(actual)) throw new AccessDeniedException("Solo un gestor puede cambiar el estado");
    Proyecto proyecto = proyectos.bloquear(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
    if (ESTADOS_GESTIONADOS_POR_FLUJO.contains(req.estado())) {
      throw new ReglaNegocioException(
          "Este estado solo puede alcanzarse mediante cotización y validación de pagos");
    }
    EstadoProyecto anterior = proyecto.getEstado();
    long obligatorios = requerimientos.countByProyectoIdAndObligatorioTrue(id);
    long pendientes = requerimientos.countByProyectoIdAndObligatorioTrueAndCompletadoFalse(id);
    politica.validar(anterior, req.estado(), proyecto.isRequiereTramitePublico(), obligatorios,
        pendientes, req.comentario());
    proyecto.cambiarEstado(req.estado());
    historial.save(new HistorialEstado(proyecto, actual, anterior, req.estado(),
        StringUtils.trimToNull(req.comentario())));
    auditoria.registrar(actual, "proyectos", id, "UPDATE",
        Map.of("estadoAnterior", anterior.name(), "estadoNuevo", req.estado().name()));
    return dto(proyecto);
  }

  @Transactional(readOnly = true)
  public List<HistorialRespuesta> historial(UUID proyectoId) {
    buscarVisible(proyectoId);
    return historial.findByProyectoIdOrderByCambiadoEnDesc(proyectoId).stream().map(h -> {
      Usuario u = h.getCambiadoPor();
      return new HistorialRespuesta(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
          u == null ? null : u.getId(), u == null ? "Sistema" : u.getNombres() + " " + u.getApellidos(),
          h.getComentario(), h.getCambiadoEn());
    }).toList();
  }

  @Transactional(readOnly = true)
  public List<RequerimientoRespuesta> listarRequerimientos(UUID proyectoId) {
    buscarVisible(proyectoId);
    return requerimientos.findByProyectoIdOrderByObligatorioDescPrioridadDesc(proyectoId).stream()
        .map(this::dto).toList();
  }

  @Transactional
  public RequerimientoRespuesta crearRequerimiento(UUID proyectoId, RequerimientoCrear req) {
    Proyecto proyecto = proyectos.bloquear(proyectoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
    Usuario actual = sesion.usuario();
    verificarEdicionRequerimientos(proyecto, actual);
    Requerimiento r = requerimientos.save(new Requerimiento(proyecto, StringUtils.trim(req.categoria()),
        StringUtils.trim(req.descripcion()), req.prioridad(), req.obligatorio(), req.completado()));
    auditoria.registrar(actual, "requerimientos", r.getId(), "INSERT", Map.of());
    return dto(r);
  }

  @Transactional
  public RequerimientoRespuesta actualizarRequerimiento(UUID id, RequerimientoCrear req) {
    Requerimiento r = requerimientos.findWithProyectoById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Requerimiento no encontrado"));
    Usuario actual = sesion.usuario();
    verificarEdicionRequerimientos(r.getProyecto(), actual);
    r.actualizar(StringUtils.trim(req.categoria()), StringUtils.trim(req.descripcion()), req.prioridad(),
        req.obligatorio(), req.completado());
    auditoria.registrar(actual, "requerimientos", id, "UPDATE", Map.of());
    return dto(r);
  }

  @Transactional
  public void eliminarRequerimiento(UUID id) {
    Requerimiento r = requerimientos.findWithProyectoById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Requerimiento no encontrado"));
    Usuario actual = sesion.usuario();
    verificarEdicionRequerimientos(r.getProyecto(), actual);
    requerimientos.delete(r);
    auditoria.registrar(actual, "requerimientos", id, "DELETE", Map.of());
  }

  @Transactional(readOnly = true)
  public Dashboard dashboard() {
    Usuario actual = sesion.usuario();
    List<Object[]> filas;
    long pendientes;
    long totalClientes;
    switch (actual.getRol().getNombre()) {
      case "ADMINISTRADOR", "ENCARGADO" -> {
        filas = proyectos.contarPorEstado();
        pendientes = requerimientos.countPendientes();
        totalClientes = clientes.count();
      }
      case "CLIENTE" -> {
        filas = proyectos.contarPorEstadoCliente(actual.getCorreo());
        pendientes = requerimientos.countPendientesCliente(actual.getCorreo());
        totalClientes = 1;
      }
      case "ESPECIALISTA" -> {
        filas = proyectos.contarPorEstadoEspecialista(actual.getCorreo());
        pendientes = requerimientos.countPendientesEspecialista(actual.getCorreo());
        totalClientes = 0;
      }
      default -> throw new AccessDeniedException("Rol sin dashboard");
    }
    EnumMap<EstadoProyecto, Long> porEstado = new EnumMap<>(EstadoProyecto.class);
    filas.forEach(f -> porEstado.put((EstadoProyecto) f[0], (Long) f[1]));
    long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
    return new Dashboard(total, pendientes, totalClientes, porEstado);
  }

  private Proyecto buscarVisible(UUID id) {
    Proyecto p = proyectos.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
    verificarVisible(p, sesion.usuario());
    return p;
  }

  private void verificarVisible(Proyecto proyecto, Usuario usuario) {
    if (esGestor(usuario)) return;
    if ("CLIENTE".equals(usuario.getRol().getNombre()) && proyecto.getCliente().getUsuario() != null
        && usuario.getId().equals(proyecto.getCliente().getUsuario().getId())) return;
    if ("ESPECIALISTA".equals(usuario.getRol().getNombre())
        && proyectos.asignadoA(proyecto.getId(), usuario.getCorreo())) return;
    throw new AccessDeniedException("Proyecto ajeno");
  }

  private void verificarClienteVisible(Cliente cliente) {
    Usuario actual = sesion.usuario();
    if (esGestor(actual)) return;
    if ("CLIENTE".equals(actual.getRol().getNombre()) && cliente.getUsuario() != null
        && actual.getId().equals(cliente.getUsuario().getId())) return;
    throw new AccessDeniedException("Cliente ajeno");
  }

  private void verificarEdicionRequerimientos(Proyecto proyecto, Usuario actual) {
    verificarVisible(proyecto, actual);
    if (esGestor(actual)) return;
    if (!"CLIENTE".equals(actual.getRol().getNombre())
        || !ESTADOS_EDITABLES_CLIENTE.contains(proyecto.getEstado())) {
      throw new AccessDeniedException("Los requerimientos ya no pueden ser modificados por el cliente");
    }
  }

  private boolean esGestor(Usuario usuario) {
    return Set.of("ADMINISTRADOR", "ENCARGADO").contains(usuario.getRol().getNombre());
  }

  private void validarFecha(LocalDate fecha) {
    if (fecha != null && fecha.isBefore(LocalDate.now()))
      throw new ReglaNegocioException("La fecha estimada no puede estar en el pasado");
  }

  private PageRequest pagina(int pagina, int tamano, Sort sort) {
    if (pagina < 0 || tamano < 1) throw new ReglaNegocioException("Paginación inválida");
    return PageRequest.of(pagina, Math.min(tamano, 100), sort);
  }

  private Respuesta dto(Proyecto p) {
    Cliente c = p.getCliente();
    String cliente = c.getRazonSocial();
    if (StringUtils.isBlank(cliente) && c.getUsuario() != null)
      cliente = c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos();
    Empleado e = p.getEncargado();
    return new Respuesta(p.getId(), p.getCodigo(), c.getId(), cliente,
        e == null ? null : e.getId(), e == null ? null : e.getUsuario().getNombres() + " " + e.getUsuario().getApellidos(),
        p.getNombre(), p.getDescripcion(), p.getTipoProyecto(), p.getUbicacion(), p.getAreaTerreno(),
        p.getEstado(), p.isRequiereTramitePublico(), p.getFechaSolicitud(), p.getFechaInicio(),
        p.getFechaFinEstimada(), p.getFechaCierre());
  }

  private RequerimientoRespuesta dto(Requerimiento r) {
    return new RequerimientoRespuesta(r.getId(), r.getCategoria(), r.getDescripcion(), r.getPrioridad(),
        r.isObligatorio(), r.isCompletado());
  }
}
