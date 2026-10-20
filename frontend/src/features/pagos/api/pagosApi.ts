import { api } from '../../../lib/api/client';
import type { Pago } from '../../../types/domain';

export const listarPendientes = () => api.get<Pago[]>('/pagos').then((r) => r.data);
export const validar = (id: string) => api.post<Pago>(`/pagos/${id}/validar`).then((r) => r.data);
export const rechazar = (id: string, motivo: string) =>
  api.post<Pago>(`/pagos/${id}/rechazar`, { motivo }).then((r) => r.data);
