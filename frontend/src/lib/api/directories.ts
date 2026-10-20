import { api } from './client';
import type { Cliente, Empleado, Pagina, Usuario } from '../../types/domain';
export async function listarClientes(q = '') {
  return (await api.get<Pagina<Cliente>>('/clientes', { params: { q, tamano: 100 } })).data;
}
export async function listarEmpleados(q = '') {
  return (await api.get<Pagina<Empleado>>('/empleados', { params: { q, tamano: 100 } })).data;
}
export async function listarUsuarios(q = '') {
  return (await api.get<Pagina<Usuario>>('/usuarios', { params: { q, tamano: 100 } })).data;
}
export async function cambiarEstadoUsuario(id: string, activo: boolean) {
  return (await api.patch<Usuario>(`/usuarios/${id}/estado`, { activo })).data;
}
