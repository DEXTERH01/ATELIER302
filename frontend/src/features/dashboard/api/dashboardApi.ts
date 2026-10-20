import { api } from '../../../lib/api/client';
import type { DashboardResumen } from '../../../types/domain';
export async function obtenerResumen() {
  return (await api.get<DashboardResumen>('/dashboard/resumen')).data;
}
