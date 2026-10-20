import { api } from '../../../lib/api/client';
import type { Cotizacion, Expediente, Pago, TipoCotizacion } from '../../../types/domain';

export interface CotizacionInput {
  tipo: TipoCotizacion;
  porcentajeImpuesto: number;
  porcentajeAdelanto: number;
  observacion?: string;
  detalles: { concepto: string; descripcion?: string; cantidad: number; precioUnitario: number }[];
}
export const listar = (proyectoId: string) =>
  api.get<Cotizacion[]>(`/proyectos/${proyectoId}/cotizaciones`).then((r) => r.data);
export const crear = (proyectoId: string, input: CotizacionInput) =>
  api.post<Cotizacion>(`/proyectos/${proyectoId}/cotizaciones`, input).then((r) => r.data);
export const enviar = (id: string) => api.post<Cotizacion>(`/cotizaciones/${id}/enviar`).then((r) => r.data);
export const renovar = (id: string) => api.post<Cotizacion>(`/cotizaciones/${id}/renovar`).then((r) => r.data);
export const responder = (id: string, aceptar: boolean, observacion?: string) =>
  api.post<Cotizacion>(`/cotizaciones/${id}/responder`, { aceptar, observacion }).then((r) => r.data);
export const listarPagos = (cotizacionId: string) =>
  api.get<Pago[]>(`/cotizaciones/${cotizacionId}/pagos`).then((r) => r.data);
export async function registrarPago(cotizacionId: string, form: FormData) {
  return api
    .post<Pago>(`/cotizaciones/${cotizacionId}/pagos`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data);
}
export const obtenerExpediente = (proyectoId: string) =>
  api.get<Expediente>(`/proyectos/${proyectoId}/expediente`).then((r) => r.data);
