import { api } from '../../../lib/api/client';
import type { EstadoTramite,ObservacionEntidad,TramitePublico } from '../../../types/domain';

export interface TramiteInput { responsableId:string;entidad:string;tipoTramite:string;numeroExpedienteExterno?:string;observacionGeneral?:string }
export const listar=(proyectoId:string)=>api.get<TramitePublico[]>(`/proyectos/${proyectoId}/tramites`).then(r=>r.data);
export const crear=(proyectoId:string,input:TramiteInput)=>api.post<TramitePublico>(`/proyectos/${proyectoId}/tramites`,input).then(r=>r.data);
export const actualizar=(tramite:TramitePublico,estado:EstadoTramite,fechaPresentacion?:string)=>api.patch<TramitePublico>(`/tramites/${tramite.id}`,{responsableId:tramite.responsableId,entidad:tramite.entidad,tipoTramite:tramite.tipoTramite,numeroExpedienteExterno:tramite.numeroExpedienteExterno,observacionGeneral:tramite.observacionGeneral,estado,fechaPresentacion}).then(r=>r.data);
export const listarObservaciones=(id:string)=>api.get<ObservacionEntidad[]>(`/tramites/${id}/observaciones`).then(r=>r.data);
export const observar=(id:string,input:{descripcion:string;fechaNotificacion:string;fechaLimite?:string})=>api.post<ObservacionEntidad>(`/tramites/${id}/observaciones`,input).then(r=>r.data);
export const subsanar=(id:string,input:{respuesta:string;fechaSubsanacion:string})=>api.post<ObservacionEntidad>(`/observaciones-entidad/${id}/subsanar`,input).then(r=>r.data);
export const resolver=(id:string,input:{aprobado:boolean;fechaRespuesta:string;observacionGeneral:string})=>api.post<TramitePublico>(`/tramites/${id}/resolver`,input).then(r=>r.data);
export const completarExpediente=(proyectoId:string)=>api.post(`/proyectos/${proyectoId}/expediente/completar`).then(r=>r.data);
