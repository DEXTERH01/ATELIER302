import { useMutation,useQuery,useQueryClient } from '@tanstack/react-query';
import { Layers3 } from 'lucide-react';
import type { FormEvent } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import * as api from '../api/especialidadesApi';
const campo='rounded-lg border border-atelier-stone bg-white px-3 py-2.5';
export function SpecialtiesPage(){
  const qc=useQueryClient();const lista=useQuery({queryKey:['especialidades'],queryFn:api.listar});
  const refrescar=async()=>qc.invalidateQueries({queryKey:['especialidades']});
  const crear=useMutation({mutationFn:api.crear,onSuccess:refrescar});
  const actualizar=useMutation({mutationFn:({id,...input}:{id:number;nombre:string;descripcion?:string;activa:boolean})=>api.actualizar(id,input),onSuccess:refrescar});
  function submit(e:FormEvent<HTMLFormElement>){e.preventDefault();const f=new FormData(e.currentTarget);crear.mutate({nombre:String(f.get('nombre')),descripcion:String(f.get('descripcion')||'')||undefined});e.currentTarget.reset();}
  return <><PageHeader eyebrow="Configuración técnica" title="Especialidades"/><section className="mx-auto grid max-w-6xl gap-5 p-5 md:p-9 lg:grid-cols-[1fr_360px]"><div className="grid content-start gap-3">{lista.data?.map(x=><article key={x.id} className="flex items-center gap-4 rounded-xl border border-atelier-stone bg-white p-4"><span className="rounded-lg bg-atelier-paper p-2 text-atelier-accent"><Layers3/></span><div className="flex-1"><h2 className="font-semibold">{x.nombre}</h2><p className="text-sm text-neutral-600">{x.descripcion||'Sin descripción'}</p></div><button onClick={()=>actualizar.mutate({...x,descripcion:x.descripcion||undefined,activa:!x.activa})} className={`rounded-full px-3 py-1 text-xs font-semibold ${x.activa?'bg-emerald-100 text-emerald-800':'bg-neutral-200 text-neutral-700'}`}>{x.activa?'Activa':'Inactiva'}</button></article>)}</div><form onSubmit={submit} className="grid content-start gap-3 rounded-xl border border-atelier-stone bg-white p-5"><h2 className="text-lg font-semibold">Nueva especialidad</h2><input name="nombre" required maxLength={80} placeholder="Nombre" className={campo}/><textarea name="descripcion" maxLength={250} placeholder="Descripción" className={campo}/>{crear.isError&&<p className="text-sm text-red-700">No se pudo crear; verifica que el nombre sea único.</p>}<button className="rounded-lg bg-atelier-accent px-4 py-2.5 font-semibold text-white">Crear especialidad</button></form></section></>;
}
