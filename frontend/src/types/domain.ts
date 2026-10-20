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
