import { api, refreshAccessToken } from '../../../lib/api/client';
import { tokenStore } from '../../../lib/auth/tokenStore';
import type { AuthResponse, UsuarioSesion } from '../../../types/auth';
export type LoginInput = { correo: string; password: string };
export type RegisterInput = LoginInput & {
  nombres: string;
  apellidos: string;
  telefono?: string;
  tipoPersona: 'NATURAL' | 'JURIDICA';
  tipoDocumento: 'DNI' | 'CE' | 'RUC' | 'PASAPORTE';
  numeroDocumento: string;
  razonSocial?: string;
  direccion?: string;
  distrito?: string;
};
export async function login(input: LoginInput) {
  const { data } = await api.post<AuthResponse>('/auth/login', input);
  tokenStore.set(data.accessToken);
  return data;
}
export async function registerClient(input: RegisterInput) {
  const { data } = await api.post<AuthResponse>('/auth/register-client', input);
  tokenStore.set(data.accessToken);
  return data;
}
export async function restoreSession() {
  await refreshAccessToken();
  const { data } = await api.get<UsuarioSesion>('/auth/me');
  return data;
}
export async function logout() {
  try {
    await api.post('/auth/logout');
  } finally {
    tokenStore.set(null);
  }
}
