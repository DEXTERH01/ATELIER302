import type { EstadoProyecto } from '../../types/domain';

export const estadoEtiquetas: Record<EstadoProyecto, string> = {
  SOLICITUD_REGISTRADA: 'Solicitud registrada',
  DATOS_INCOMPLETOS: 'Datos incompletos',
  EN_COTIZACION: 'En cotización',
  COTIZACION_ENVIADA: 'Cotización enviada',
  ESPERANDO_PAGO: 'Esperando pago',
  PAGO_EN_REVISION: 'Pago en revisión',
  PROYECTO_ACTIVO: 'Proyecto activo',
  EN_ESTUDIOS: 'En estudios',
  EN_DISENO: 'En diseño',
  REVISION_CLIENTE: 'Revisión del cliente',
  EXPEDIENTE_FINAL: 'Expediente final',
  EN_TRAMITE_PUBLICO: 'En trámite público',
  CON_OBSERVACIONES: 'Con observaciones',
  APROBADO: 'Aprobado',
  NO_VIABLE: 'No viable',
  CERRADO: 'Cerrado',
  CANCELADO: 'Cancelado',
};
const base: Record<EstadoProyecto, EstadoProyecto[]> = {
  SOLICITUD_REGISTRADA: ['DATOS_INCOMPLETOS', 'EN_COTIZACION', 'CANCELADO'],
  DATOS_INCOMPLETOS: ['SOLICITUD_REGISTRADA', 'EN_COTIZACION', 'CANCELADO'],
  EN_COTIZACION: ['CANCELADO'],
  COTIZACION_ENVIADA: ['CANCELADO'],
  ESPERANDO_PAGO: ['CANCELADO'],
  PAGO_EN_REVISION: ['CANCELADO'],
  PROYECTO_ACTIVO: ['EN_ESTUDIOS', 'EN_DISENO', 'CANCELADO'],
  EN_ESTUDIOS: ['EN_DISENO', 'CANCELADO'],
  EN_DISENO: ['CANCELADO'],
  REVISION_CLIENTE: ['CANCELADO'],
  EXPEDIENTE_FINAL: ['EN_TRAMITE_PUBLICO', 'APROBADO', 'CERRADO', 'CANCELADO'],
  EN_TRAMITE_PUBLICO: ['CON_OBSERVACIONES', 'APROBADO', 'NO_VIABLE', 'CANCELADO'],
  CON_OBSERVACIONES: ['EN_TRAMITE_PUBLICO', 'NO_VIABLE', 'CANCELADO'],
  APROBADO: ['CERRADO'],
  NO_VIABLE: ['CERRADO'],
  CERRADO: [],
  CANCELADO: [],
};
export function siguientesEstados(estado: EstadoProyecto, requiereTramite: boolean): EstadoProyecto[] {
  if (requiereTramite && estado === 'EXPEDIENTE_FINAL') return ['CANCELADO'];
  if (requiereTramite && ['EN_TRAMITE_PUBLICO', 'CON_OBSERVACIONES'].includes(estado)) return ['CANCELADO'];
  if (estado !== 'EXPEDIENTE_FINAL') return base[estado];
  return ['APROBADO', 'CERRADO', 'CANCELADO'];
}
