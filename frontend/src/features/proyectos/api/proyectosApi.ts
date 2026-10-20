import { api } from '../../../lib/api/client';
import type { EstadoProyecto, HistorialEstado, Pagina, Proyecto, Requerimiento } from '../../../types/domain';
export type ProyectoInput = {
  clienteId?: string;
  encargadoId?: string;
  nombre: string;
  descripcion?: string;
  tipoProyecto: string;
  ubicacion?: string;
  areaTerreno?: number;
  requiereTramitePublico: boolean;
  fechaFinEstimada?: string;
};
export type RequerimientoInput = {
  categoria: string;
  descripcion: string;
  prioridad: 'BAJA' | 'MEDIA' | 'ALTA';
  obligatorio: boolean;
  completado: boolean;
};
export async function listar(q = '') {
  return (await api.get<Pagina<Proyecto>>('/proyectos', { params: { q } })).data;
}
export async function obtener(id: string) {
  return (await api.get<Proyecto>(`/proyectos/${id}`)).data;
}
export async function crear(input: ProyectoInput) {
  return (await api.post<Proyecto>('/proyectos', input)).data;
}
export async function transicionar(id: string, estado: EstadoProyecto, comentario?: string) {
  return (await api.post<Proyecto>(`/proyectos/${id}/transiciones`, { estado, comentario })).data;
}
export async function listarRequerimientos(id: string) {
  return (await api.get<Requerimiento[]>(`/proyectos/${id}/requerimientos`)).data;
}
export async function crearRequerimiento(id: string, input: RequerimientoInput) {
  return (await api.post<Requerimiento>(`/proyectos/${id}/requerimientos`, input)).data;
}
export async function actualizarRequerimiento(id: string, input: RequerimientoInput) {
  return (await api.patch<Requerimiento>(`/requerimientos/${id}`, input)).data;
}
export async function eliminarRequerimiento(id: string) {
  await api.delete(`/requerimientos/${id}`);
}
export async function obtenerHistorial(id: string) {
  return (await api.get<HistorialEstado[]>(`/proyectos/${id}/historial`)).data;
}
