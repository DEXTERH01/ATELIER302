import { api } from '../../../lib/api/client';
import type { Especialidad } from '../../../types/domain';
export const listar=()=>api.get<Especialidad[]>('/especialidades').then(r=>r.data);
export const crear=(input:{nombre:string;descripcion?:string})=>api.post<Especialidad>('/especialidades',input).then(r=>r.data);
export const actualizar=(id:number,input:{nombre:string;descripcion?:string;activa:boolean})=>api.patch<Especialidad>(`/especialidades/${id}`,input).then(r=>r.data);
