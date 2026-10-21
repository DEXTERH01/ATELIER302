import { useMutation,useQuery,useQueryClient } from '@tanstack/react-query';
import { CheckCircle2,Eye,FilePenLine,Send,UploadCloud } from 'lucide-react';
import type { FormEvent } from 'react';
import type { ObservacionCliente,Propuesta,Proyecto } from '../../../types/domain';
import { useAuth } from '../../auth/AuthContext';
import * as cotizacionesApi from '../../cotizaciones/api/cotizacionesApi';
import * as api from '../api/propuestasApi';
import { etiquetaRonda } from '../proposalRounds';

const campo='rounded-lg border border-atelier-stone bg-white px-3 py-2.5';
const fecha=new Intl.DateTimeFormat('es-PE',{dateStyle:'medium',timeStyle:'short',timeZone:'America/Lima'});

export function ProposalsPanel({proyecto}:{proyecto:Proyecto}){
  const {usuario}=useAuth();const qc=useQueryClient();
  const gestor=usuario?.rol==='ADMINISTRADOR'||usuario?.rol==='ENCARGADO';
  const creador=gestor||usuario?.rol==='ESPECIALISTA';const cliente=usuario?.rol==='CLIENTE';
  const propuestas=useQuery({queryKey:['proyecto',proyecto.id,'propuestas'],queryFn:()=>api.listar(proyecto.id)});
  const refrescar=async()=>{await Promise.all([
    qc.invalidateQueries({queryKey:['proyecto',proyecto.id,'propuestas']}),
    qc.invalidateQueries({queryKey:['proyecto',proyecto.id]}),
    qc.invalidateQueries({queryKey:['proyecto',proyecto.id,'historial']}),
    qc.invalidateQueries({queryKey:['proyecto',proyecto.id,'cotizaciones']}),
  ]);};
  const crear=useMutation({mutationFn:(form:FormData)=>api.crear(proyecto.id,form),onSuccess:refrescar});
  const enviar=useMutation({mutationFn:api.enviar,onSuccess:refrescar});
  const responder=useMutation({mutationFn:({id,conforme,observacion}:{id:string;conforme:boolean;observacion?:string})=>api.responder(id,conforme,observacion),onSuccess:refrescar});
  function submit(e:FormEvent<HTMLFormElement>){e.preventDefault();const form=e.currentTarget;crear.mutate(new FormData(form),{onSuccess:()=>form.reset()});}
  return <section className="mt-4 grid gap-4">
    {propuestas.isPending&&<p className="rounded-xl bg-white p-5">Cargando propuestas…</p>}
    {propuestas.data?.map(p=><ProposalCard key={p.id} propuesta={p} proyecto={proyecto} gestor={gestor} cliente={cliente} enviar={()=>enviar.mutate(p.id)} responder={(conforme,observacion)=>responder.mutate({id:p.id,conforme,observacion})} refrescar={refrescar}/>)}
    {propuestas.data?.length===0&&<p className="rounded-xl bg-white p-5 text-neutral-500">Aún no hay propuestas arquitectónicas.</p>}
    {creador&&proyecto.estado==='EN_DISENO'&&<form onSubmit={submit} className="grid gap-3 rounded-xl border border-atelier-stone bg-white p-5 sm:grid-cols-2">
      <div className="sm:col-span-2"><p className="text-xs font-semibold uppercase tracking-wider text-atelier-accent">Nueva versión</p><h2 className="mt-1 text-lg font-semibold">Publicar avance de diseño</h2></div>
      <select name="tipo" className={campo}><option value="ANTEPROYECTO">Anteproyecto</option><option value="PROYECTO_FINAL">Proyecto final</option></select>
      <label className="grid gap-1 text-sm">Vista previa PDF, JPG o PNG<input name="archivo" type="file" required accept="application/pdf,image/jpeg,image/png" className={campo}/></label>
      <textarea name="descripcion" maxLength={5000} placeholder="Descripción de la versión y cambios incluidos" className={`${campo} sm:col-span-2`}/>
      {crear.isError&&<p role="alert" className="text-sm text-red-700 sm:col-span-2">No fue posible crear la propuesta. Verifica el archivo y los pagos adicionales requeridos.</p>}
      <button disabled={crear.isPending} className="inline-flex items-center justify-center gap-2 rounded-lg bg-atelier-ink px-4 py-2.5 font-semibold text-white sm:col-span-2"><UploadCloud size={17}/>{crear.isPending?'Subiendo…':'Guardar propuesta'}</button>
    </form>}
  </section>;
}

function ProposalCard({propuesta:p,proyecto,gestor,cliente,enviar,responder,refrescar}:{propuesta:Propuesta;proyecto:Proyecto;gestor:boolean;cliente:boolean;enviar:()=>void;responder:(conforme:boolean,observacion?:string)=>void;refrescar:()=>Promise<void>}){
  function observar(e:FormEvent<HTMLFormElement>){e.preventDefault();const f=new FormData(e.currentTarget);responder(false,String(f.get('observacion')));}
  return <article className="rounded-xl border border-atelier-stone bg-white p-5">
    <div className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-xs font-semibold uppercase tracking-wider text-atelier-accent">{p.tipo.replace('_',' ')} · versión {p.numeroVersion}</p><h3 className="mt-1 text-lg font-semibold">{p.vistaPreviaNombre||'Propuesta arquitectónica'}</h3><p className="text-xs text-neutral-500">{p.creador} · {fecha.format(new Date(p.creadoEn))}</p></div><span className={`rounded-full px-3 py-1 text-xs font-semibold ${p.estado==='CONFORME'?'bg-emerald-100 text-emerald-800':p.estado==='OBSERVADA'?'bg-red-100 text-red-800':'bg-amber-100 text-amber-900'}`}>{p.estado}</span></div>
    {p.descripcion&&<p className="mt-4 rounded-lg bg-atelier-paper p-3 text-sm text-neutral-700">{p.descripcion}</p>}
    <div className="mt-4 flex flex-wrap gap-2"><a href={p.vistaPreviaUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-lg border border-atelier-stone px-4 py-2 font-semibold text-atelier-accent"><Eye size={16}/>Abrir propuesta</a>{gestor&&p.estado==='BORRADOR'&&proyecto.estado==='EN_DISENO'&&<button onClick={enviar} className="inline-flex items-center gap-2 rounded-lg bg-atelier-accent px-4 py-2 font-semibold text-white"><Send size={16}/>Enviar al cliente</button>}</div>
    {cliente&&p.estado==='ENVIADA'&&<div className="mt-4 grid gap-3 border-t border-atelier-stone pt-4"><button onClick={()=>responder(true)} className="inline-flex items-center justify-center gap-2 rounded-lg bg-emerald-700 px-4 py-2 font-semibold text-white"><CheckCircle2 size={17}/>Dar conformidad</button><form onSubmit={observar} className="grid gap-2"><textarea name="observacion" required maxLength={5000} placeholder="Describe las correcciones solicitadas" className={campo}/><button className="rounded-lg border border-red-700 px-4 py-2 font-semibold text-red-700">Registrar observaciones</button></form></div>}
    <Observations propuesta={p} proyecto={proyecto} gestor={gestor} refrescar={refrescar}/>
  </article>;
}

function Observations({propuesta,proyecto,gestor,refrescar}:{propuesta:Propuesta;proyecto:Proyecto;gestor:boolean;refrescar:()=>Promise<void>}){
  const qc=useQueryClient();const observaciones=useQuery({queryKey:['propuesta',propuesta.id,'observaciones'],queryFn:()=>api.listarObservaciones(propuesta.id)});
  const actualizar=useMutation({mutationFn:({o,estado,adicional}:{o:ObservacionCliente;estado:ObservacionCliente['estado'];adicional:boolean})=>api.actualizarObservacion(o.id,estado,adicional),onSuccess:async()=>{await qc.invalidateQueries({queryKey:['propuesta',propuesta.id,'observaciones']});await refrescar();}});
  const cotizar=useMutation({mutationFn:({o,input}:{o:ObservacionCliente;input:cotizacionesApi.CotizacionInput})=>cotizacionesApi.crear(proyecto.id,{...input,tipo:'ADICIONAL',observacionClienteId:o.id}),onSuccess:async()=>{await qc.invalidateQueries({queryKey:['propuesta',propuesta.id,'observaciones']});await refrescar();}});
  function crearCotizacion(e:FormEvent<HTMLFormElement>,o:ObservacionCliente){e.preventDefault();const f=new FormData(e.currentTarget);cotizar.mutate({o,input:{tipo:'ADICIONAL',porcentajeImpuesto:Number(f.get('impuesto')),porcentajeAdelanto:Number(f.get('adelanto')),observacion:`Ronda adicional ${o.numeroRonda}: ${o.descripcion}`,detalles:[{concepto:String(f.get('concepto')),cantidad:1,precioUnitario:Number(f.get('precio'))}]}});}
  if(!observaciones.data?.length)return null;
  return <div className="mt-5 grid gap-3 border-t border-atelier-stone pt-4"><h4 className="flex items-center gap-2 font-semibold"><FilePenLine size={17}/>Observaciones del cliente</h4>{observaciones.data.map(o=><div key={o.id} className="rounded-lg bg-atelier-paper p-4"><div className="flex flex-wrap justify-between gap-2"><strong>Ronda {o.numeroRonda} · {o.estado}</strong><span className={`text-xs font-semibold ${o.requiereCotizacionAdicional?'text-amber-800':'text-emerald-700'}`}>{etiquetaRonda(o.numeroRonda,o.adicional)}</span></div><p className="mt-2 text-sm">{o.descripcion}</p>{gestor&&o.estado==='PENDIENTE'&&<div className="mt-3 flex flex-wrap gap-2">{o.numeroRonda>2&&!o.adicional&&<button onClick={()=>actualizar.mutate({o,estado:'PENDIENTE',adicional:true})} className="rounded-lg border border-amber-700 px-3 py-1.5 text-sm font-semibold text-amber-800">Marcar adicional</button>}<button onClick={()=>actualizar.mutate({o,estado:'ATENDIDA',adicional:o.adicional})} className="rounded-lg border border-emerald-700 px-3 py-1.5 text-sm font-semibold text-emerald-700">Marcar atendida</button><button onClick={()=>actualizar.mutate({o,estado:'RECHAZADA',adicional:o.adicional})} className="rounded-lg border border-red-700 px-3 py-1.5 text-sm font-semibold text-red-700">Rechazar</button></div>}{gestor&&o.adicional&&!o.cotizacionAdicionalId&&<form onSubmit={e=>crearCotizacion(e,o)} className="mt-3 grid gap-2 rounded-lg border border-amber-200 bg-white p-3 sm:grid-cols-2"><input name="concepto" required maxLength={180} defaultValue={`Cambios adicionales - ronda ${o.numeroRonda}`} className={`${campo} sm:col-span-2`}/><input name="precio" type="number" min="0.01" step="0.01" required placeholder="Precio" className={campo}/><input name="impuesto" type="number" min="0" max="100" step="0.01" defaultValue="18" className={campo}/><label className="grid gap-1 text-xs sm:col-span-2">Adelanto %<input name="adelanto" type="number" min="0" max="100" step="0.01" defaultValue="100" className={campo}/></label><button className="rounded-lg bg-atelier-ink px-4 py-2 font-semibold text-white sm:col-span-2">Crear cotización adicional</button></form>}{o.cotizacionAdicionalId&&<p className="mt-2 text-xs font-semibold text-atelier-accent">Cotización adicional vinculada</p>}</div>)}</div>;
}
