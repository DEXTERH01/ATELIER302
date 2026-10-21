import { api } from '../../../lib/api/client';
import type { EstadoObservacionCliente,ObservacionCliente,Propuesta } from '../../../types/domain';

export const listar=(proyectoId:string)=>api.get<Propuesta[]>(`/proyectos/${proyectoId}/propuestas`).then(r=>r.data);
export const crear=(proyectoId:string,form:FormData)=>api.post<Propuesta>(`/proyectos/${proyectoId}/propuestas`,form,{headers:{'Content-Type':'multipart/form-data'}}).then(r=>r.data);
export const enviar=(id:string)=>api.post<Propuesta>(`/propuestas/${id}/enviar`).then(r=>r.data);
export const responder=(id:string,conforme:boolean,observacion?:string)=>api.post<Propuesta>(`/propuestas/${id}/respuesta`,{conforme,observacion}).then(r=>r.data);
export const listarObservaciones=(id:string)=>api.get<ObservacionCliente[]>(`/propuestas/${id}/observaciones`).then(r=>r.data);
export const actualizarObservacion=(id:string,estado:EstadoObservacionCliente,adicional:boolean)=>api.patch<ObservacionCliente>(`/observaciones-cliente/${id}`,{estado,adicional}).then(r=>r.data);
