import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ExternalLink, ShieldCheck } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import * as api from '../api/pagosApi';

const moneda = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
const fecha = new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'America/Lima' });
export function PaymentsPage() {
  const qc = useQueryClient();
  const [rechazoId, setRechazoId] = useState<string | null>(null);
  const pagos = useQuery({ queryKey: ['pagos', 'pendientes'], queryFn: api.listarPendientes });
  const refrescar = async () => qc.invalidateQueries({ queryKey: ['pagos', 'pendientes'] });
  const validar = useMutation({ mutationFn: api.validar, onSuccess: refrescar });
  const rechazar = useMutation({ mutationFn: ({ id, motivo }: { id: string; motivo: string }) => api.rechazar(id, motivo), onSuccess: refrescar });
  function confirmarRechazo(e: FormEvent<HTMLFormElement>, id: string) { e.preventDefault(); const f=new FormData(e.currentTarget); rechazar.mutate({ id, motivo:String(f.get('motivo')) },{ onSuccess:()=>setRechazoId(null) }); }
  return <><PageHeader eyebrow="Control financiero" title="Pagos por validar" /><section className="mx-auto grid max-w-7xl gap-4 p-5 md:p-9">{pagos.isPending && <p>Cargando pagos…</p>}{pagos.data?.map((p) => <article key={p.id} className="grid gap-4 rounded-xl border border-atelier-stone bg-white p-5 md:grid-cols-[1fr_auto] md:items-center"><div><div className="flex flex-wrap items-center gap-2"><ShieldCheck className="text-atelier-accent" size={20} /><h2 className="font-semibold">{p.tipo} · {moneda.format(p.monto)}</h2></div><p className="mt-2 text-sm text-neutral-600">Operación {p.numeroOperacion || 'sin número'} · {fecha.format(new Date(p.pagadoEn))}</p><p className="mt-1 text-xs text-neutral-500">{p.comprobanteTipoMime} · {(p.comprobanteTamanoBytes / 1024).toFixed(1)} KB</p><a href={p.comprobanteUrl} target="_blank" rel="noreferrer" className="mt-3 inline-flex items-center gap-1 text-sm font-semibold text-atelier-accent">Abrir comprobante <ExternalLink size={14} /></a></div><div className="flex flex-wrap gap-2"><button disabled={validar.isPending} onClick={() => validar.mutate(p.id)} className="rounded-lg bg-emerald-700 px-4 py-2 font-semibold text-white">Validar</button><button disabled={rechazar.isPending} onClick={() => setRechazoId(p.id)} className="rounded-lg border border-red-700 px-4 py-2 font-semibold text-red-700">Rechazar</button></div>{rechazoId===p.id&&<form onSubmit={(e)=>confirmarRechazo(e,p.id)} className="grid gap-2 border-t border-red-200 pt-3 md:col-span-2"><label className="text-sm font-semibold text-red-900">Motivo<textarea name="motivo" required maxLength={500} className="mt-1 w-full rounded-lg border border-red-200 bg-red-50 px-3 py-2" /></label><div className="flex gap-2"><button className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white">Confirmar rechazo</button><button type="button" onClick={()=>setRechazoId(null)} className="px-4 py-2 font-semibold">Cancelar</button></div></form>}</article>)}{pagos.data?.length === 0 && <div className="rounded-xl border border-atelier-stone bg-white p-8 text-center text-neutral-600">No hay pagos pendientes de validación.</div>}</section></>;
}
