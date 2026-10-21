import { api } from '../../../lib/api/client';
import type { EventoAuditoria, Pagina } from '../../../types/domain';

export async function listarAuditoria(pagina = 0) {
  const { data } = await api.get<Pagina<EventoAuditoria>>('/auditoria', { params: { pagina, tamano: 30 } });
  return data;
}
