import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../../../components/layout/PageHeader';
import { listarAuditoria } from '../api/auditoriaApi';

const fecha = new Intl.DateTimeFormat('es-PE', { dateStyle: 'short', timeStyle: 'medium', timeZone: 'America/Lima' });
export function AuditPage() {
  const auditoria = useQuery({ queryKey: ['auditoria', 0], queryFn: () => listarAuditoria() });
  return <><PageHeader eyebrow="Trazabilidad" title="Auditoría" />
    <section className="mx-auto max-w-7xl p-5 md:p-9">
      {auditoria.isPending && <p>Cargando historial…</p>}
      {auditoria.isError && <p role="alert" className="rounded-lg bg-red-50 p-4 text-red-800">No fue posible cargar la auditoría.</p>}
      {auditoria.data && <div className="overflow-x-auto rounded-xl border border-atelier-stone bg-white shadow-sm"><table className="w-full min-w-[760px] text-left text-sm"><thead className="bg-atelier-paper text-xs uppercase tracking-wide text-neutral-600"><tr><th className="px-4 py-3">Fecha</th><th className="px-4 py-3">Usuario</th><th className="px-4 py-3">Acción</th><th className="px-4 py-3">Recurso</th><th className="px-4 py-3">Detalle</th></tr></thead><tbody className="divide-y divide-atelier-stone">{auditoria.data.contenido.map((evento) => <tr key={evento.id}><td className="whitespace-nowrap px-4 py-3 text-neutral-600">{fecha.format(new Date(evento.creadoEn))}</td><td className="px-4 py-3"><p className="font-medium">{evento.usuario}</p><p className="text-xs text-neutral-500">{evento.correo ?? 'Proceso del sistema'}</p></td><td className="px-4 py-3"><span className="rounded-full bg-atelier-paper px-2 py-1 text-xs font-semibold">{evento.accion}</span></td><td className="px-4 py-3">{evento.tablaAfectada}</td><td className="max-w-xs px-4 py-3 text-xs text-neutral-600">{Object.keys(evento.detalle ?? {}).length ? JSON.stringify(evento.detalle) : '—'}</td></tr>)}</tbody></table>{auditoria.data.contenido.length === 0 && <p className="p-8 text-center text-neutral-600">Aún no hay eventos registrados.</p>}</div>}
    </section>
  </>;
}
