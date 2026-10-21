import { api } from '../../../lib/api/client';
import type { Asignacion, CategoriaDocumento, Documento, EstadoAsignacion, EstadoEstudio, EstadoTarea, Estudio, Expediente, PrioridadTarea, Tarea } from '../../../types/domain';
export const listarAsignaciones=(proyectoId:string)=>api.get<Asignacion[]>(`/proyectos/${proyectoId}/asignaciones`).then(r=>r.data);
export const crearAsignacion=(proyectoId:string,input:{empleadoId:string;especialidadId:number;rolProyecto:string;fechaAsignacion?:string})=>api.post<Asignacion>(`/proyectos/${proyectoId}/asignaciones`,input).then(r=>r.data);
export const actualizarAsignacion=(id:string,input:{rolProyecto:string;estado:EstadoAsignacion;fechaFin?:string})=>api.patch<Asignacion>(`/asignaciones/${id}`,input).then(r=>r.data);
export const listarTareas=(asignacionId:string)=>api.get<Tarea[]>(`/asignaciones/${asignacionId}/tareas`).then(r=>r.data);
export interface TareaInput { titulo:string;descripcion?:string;prioridad:PrioridadTarea;estado?:EstadoTarea;fechaInicio?:string;fechaLimite?:string }
export const crearTarea=(asignacionId:string,input:TareaInput)=>api.post<Tarea>(`/asignaciones/${asignacionId}/tareas`,input).then(r=>r.data);
export const actualizarTarea=(id:string,input:Required<Pick<TareaInput,'titulo'|'prioridad'>> & Omit<TareaInput,'titulo'|'prioridad'> & {estado:EstadoTarea})=>api.patch<Tarea>(`/tareas/${id}`,input).then(r=>r.data);
export const listarEstudios=(proyectoId:string)=>api.get<Estudio[]>(`/proyectos/${proyectoId}/estudios`).then(r=>r.data);
export interface EstudioInput { responsableId?:string;tipo:string;proveedor?:string;requerido:boolean;estado?:EstadoEstudio;fechaSolicitud?:string;fechaResultado?:string;resultado?:string;costo?:number }
export const crearEstudio=(proyectoId:string,input:EstudioInput)=>api.post<Estudio>(`/proyectos/${proyectoId}/estudios`,input).then(r=>r.data);
export const actualizarEstudio=(id:string,input:EstudioInput & {estado:EstadoEstudio})=>api.patch<Estudio>(`/estudios/${id}`,input).then(r=>r.data);
export const obtenerExpediente=(proyectoId:string)=>api.get<Expediente>(`/proyectos/${proyectoId}/expediente`).then(r=>r.data);
export const listarDocumentos=(expedienteId:string)=>api.get<Documento[]>(`/expedientes/${expedienteId}/documentos`).then(r=>r.data);
export const subirDocumento=(expedienteId:string,form:FormData)=>api.post<Documento>(`/expedientes/${expedienteId}/documentos`,form,{headers:{'Content-Type':'multipart/form-data'}}).then(r=>r.data);
export const eliminarDocumento=(id:string)=>api.delete(`/documentos/${id}`);
export type { CategoriaDocumento };
