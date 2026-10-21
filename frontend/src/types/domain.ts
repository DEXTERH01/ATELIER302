export type EstadoProyecto =
  | 'SOLICITUD_REGISTRADA'
  | 'DATOS_INCOMPLETOS'
  | 'EN_COTIZACION'
  | 'COTIZACION_ENVIADA'
  | 'ESPERANDO_PAGO'
  | 'PAGO_EN_REVISION'
  | 'PROYECTO_ACTIVO'
  | 'EN_ESTUDIOS'
  | 'EN_DISENO'
  | 'REVISION_CLIENTE'
  | 'EXPEDIENTE_FINAL'
  | 'EN_TRAMITE_PUBLICO'
  | 'CON_OBSERVACIONES'
  | 'APROBADO'
  | 'NO_VIABLE'
  | 'CERRADO'
  | 'CANCELADO';

export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}
export interface Proyecto {
  id: string;
  codigo: string;
  clienteId: string;
  cliente: string;
  encargadoId: string | null;
  encargado: string | null;
  nombre: string;
  descripcion: string | null;
  tipoProyecto: string;
  ubicacion: string | null;
  areaTerreno: number | null;
  estado: EstadoProyecto;
  requiereTramitePublico: boolean;
  fechaSolicitud: string;
  fechaInicio: string | null;
  fechaFinEstimada: string | null;
  fechaCierre: string | null;
}
export interface Requerimiento {
  id: string;
  categoria: string;
  descripcion: string;
  prioridad: 'BAJA' | 'MEDIA' | 'ALTA';
  obligatorio: boolean;
  completado: boolean;
}
export interface HistorialEstado {
  id: number;
  estadoAnterior: EstadoProyecto | null;
  estadoNuevo: EstadoProyecto;
  usuarioId: string | null;
  responsable: string;
  comentario: string | null;
  cambiadoEn: string;
}
export interface DashboardResumen {
  totalProyectos: number;
  requerimientosPendientes: number;
  totalClientes: number;
  porEstado: Partial<Record<EstadoProyecto, number>>;
}
export interface Cliente {
  id: string;
  usuarioId: string | null;
  correo: string | null;
  nombreCompleto: string;
  tipoPersona: string;
  tipoDocumento: string;
  numeroDocumento: string;
  razonSocial: string | null;
  direccion: string | null;
  distrito: string | null;
}
export interface Empleado {
  id: string;
  usuarioId: string;
  correo: string;
  nombreCompleto: string;
  rol: string;
  codigoColegiatura: string | null;
  cargo: string;
  tipo: string;
  disponible: boolean;
}
export interface Usuario {
  id: string;
  correo: string;
  nombres: string;
  apellidos: string;
  telefono: string | null;
  rol: string;
  activo: boolean;
  ultimoAcceso: string | null;
}
export type EstadoCotizacion = 'BORRADOR' | 'ENVIADA' | 'ACEPTADA' | 'RECHAZADA' | 'VENCIDA' | 'ANULADA';
export type TipoCotizacion = 'INICIAL' | 'ADICIONAL';
export interface DetalleCotizacion {
  id: string | null;
  concepto: string;
  descripcion: string | null;
  cantidad: number;
  precioUnitario: number;
  importe: number;
}
export interface Cotizacion {
  id: string;
  proyectoId: string;
  numero: string;
  version: number;
  tipo: TipoCotizacion;
  subtotal: number;
  impuesto: number;
  total: number;
  porcentajeAdelanto: number;
  adelantoRequerido: number;
  estado: EstadoCotizacion;
  emitidaEn: string | null;
  venceEn: string | null;
  respondidaEn: string | null;
  observacion: string | null;
  detalles: DetalleCotizacion[];
}
export type EstadoPago = 'PENDIENTE' | 'VALIDADO' | 'RECHAZADO';
export interface Pago {
  id: string;
  cotizacionId: string;
  tipo: 'ADELANTO' | 'SALDO' | 'ADICIONAL';
  metodo: 'TRANSFERENCIA' | 'DEPOSITO' | 'TARJETA' | 'EFECTIVO' | 'OTRO';
  monto: number;
  moneda: string;
  numeroOperacion: string | null;
  comprobanteUrl: string;
  comprobantePublicId: string;
  comprobanteTipoMime: string;
  comprobanteTamanoBytes: number;
  comprobanteCategoria: string;
  estado: EstadoPago;
  motivoRechazo: string | null;
  validadoPor: string | null;
  validador: string | null;
  pagadoEn: string;
  validadoEn: string | null;
}
export interface Expediente {
  id: string;
  proyectoId: string;
  codigo: string;
  estado: string;
  creadoEn: string;
  cerradoEn: string | null;
}
export interface Especialidad { id: number; nombre: string; descripcion: string | null; activa: boolean }
export type EstadoAsignacion = 'ACTIVA' | 'FINALIZADA' | 'CANCELADA';
export interface Asignacion {
  id: string; proyectoId: string; empleadoId: string; empleadoUsuarioId: string; empleado: string;
  cargo: string; especialidadId: number; especialidad: string; rolProyecto: string;
  fechaAsignacion: string; fechaFin: string | null; estado: EstadoAsignacion;
}
export type EstadoTarea = 'PENDIENTE' | 'EN_PROCESO' | 'BLOQUEADA' | 'COMPLETADA' | 'CANCELADA';
export type PrioridadTarea = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export interface Tarea {
  id: string; asignacionId: string; proyectoId: string; empleadoId: string; responsable: string;
  especialidad: string; titulo: string; descripcion: string | null; prioridad: PrioridadTarea;
  estado: EstadoTarea; fechaInicio: string | null; fechaLimite: string | null;
  completadaEn: string | null; creadoEn: string;
}
export type EstadoEstudio = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'NO_APLICA';
export interface Estudio {
  id: string; proyectoId: string; responsableId: string | null; responsableUsuarioId: string | null;
  responsable: string | null; tipo: string; proveedor: string | null; requerido: boolean;
  estado: EstadoEstudio; fechaSolicitud: string | null; fechaResultado: string | null;
  resultado: string | null; costo: number | null;
}
export type CategoriaDocumento = 'EXPEDIENTE' | 'ESTUDIO' | 'PROPUESTA' | 'TRAMITE' | 'RESOLUCION';
export interface Documento {
  id: string; expedienteId: string; proyectoId: string; estudioId: string | null; subidoPor: string;
  autor: string; nombre: string; tipoDocumento: string; version: number; url: string; publicId: string;
  formato: string; tipoMime: string; tamanoBytes: number; categoria: CategoriaDocumento;
  resourceType: string; visibleCliente: boolean; vigente: boolean; creadoEn: string;
}
