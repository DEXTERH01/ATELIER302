import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { FileCheck2, Plus, ReceiptText, Send } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import type { Proyecto } from '../../../types/domain';
import { useAuth } from '../../auth/AuthContext';
import * as api from '../api/cotizacionesApi';

const campo = 'rounded-lg border border-atelier-stone bg-white px-3 py-2.5';
const moneda = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
const fecha = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'medium', timeStyle: 'short', timeZone: 'America/Lima',
});

export function ProjectCommercialPanel({ proyecto }: { proyecto: Proyecto }) {
  const { usuario } = useAuth();
  const qc = useQueryClient();
  const [filas, setFilas] = useState([0]);
  const [rechazoId, setRechazoId] = useState<string | null>(null);
  const [motivoRechazo, setMotivoRechazo] = useState('');
  const gestor = usuario?.rol === 'ADMINISTRADOR' || usuario?.rol === 'ENCARGADO';
  const cliente = usuario?.rol === 'CLIENTE';
  const cotizaciones = useQuery({
    queryKey: ['proyecto', proyecto.id, 'cotizaciones'],
    queryFn: () => api.listar(proyecto.id),
  });
  const expediente = useQuery({
    queryKey: ['proyecto', proyecto.id, 'expediente'],
    queryFn: () => api.obtenerExpediente(proyecto.id),
    retry: false,
    enabled: ['PROYECTO_ACTIVO', 'EN_ESTUDIOS', 'EN_DISENO', 'REVISION_CLIENTE', 'EXPEDIENTE_FINAL', 'EN_TRAMITE_PUBLICO', 'CON_OBSERVACIONES', 'APROBADO', 'NO_VIABLE', 'CERRADO'].includes(proyecto.estado),
  });
  const refrescar = async () => {
    await Promise.all([
      qc.invalidateQueries({ queryKey: ['proyecto', proyecto.id] }),
      qc.invalidateQueries({ queryKey: ['proyecto', proyecto.id, 'cotizaciones'] }),
      qc.invalidateQueries({ queryKey: ['proyecto', proyecto.id, 'historial'] }),
      qc.invalidateQueries({ queryKey: ['proyecto', proyecto.id, 'expediente'] }),
    ]);
  };
  const crear = useMutation({ mutationFn: (input: api.CotizacionInput) => api.crear(proyecto.id, input), onSuccess: refrescar });
  const enviar = useMutation({ mutationFn: api.enviar, onSuccess: refrescar });
  const renovar = useMutation({ mutationFn: api.renovar, onSuccess: refrescar });
  const responder = useMutation({
    mutationFn: ({ id, aceptar, observacion }: { id: string; aceptar: boolean; observacion?: string }) => api.responder(id, aceptar, observacion),
    onSuccess: refrescar,
  });

  function crearCotizacion(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    crear.mutate({
      tipo: String(f.get('tipo')) as api.CotizacionInput['tipo'],
      porcentajeImpuesto: Number(f.get('porcentajeImpuesto')),
      porcentajeAdelanto: Number(f.get('porcentajeAdelanto')),
      observacion: String(f.get('observacion') || '') || undefined,
      detalles: filas.map((_, i) => ({
        concepto: String(f.get(`concepto-${i}`)),
        descripcion: String(f.get(`descripcion-${i}`) || '') || undefined,
        cantidad: Number(f.get(`cantidad-${i}`)),
        precioUnitario: Number(f.get(`precio-${i}`)),
      })),
    });
  }

  return (
    <section className="mt-4 grid gap-4">
      {expediente.data && (
        <article className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-emerald-900">
          <FileCheck2 />
          <div><p className="font-semibold">Expediente {expediente.data.codigo}</p><p className="text-sm">Creado automáticamente al validar el adelanto.</p></div>
        </article>
      )}
      {cotizaciones.isPending && <p className="rounded-xl bg-white p-5">Cargando cotizaciones…</p>}
      {cotizaciones.data?.map((c) => {
        const vencida = !!c.venceEn && Date.now() > new Date(c.venceEn).getTime();
        return (
          <article key={c.id} className="rounded-xl border border-atelier-stone bg-white p-5">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div><p className="text-xs font-semibold uppercase tracking-wider text-neutral-500">{c.tipo} · versión {c.version}</p><h3 className="mt-1 text-lg font-semibold">{c.numero}</h3></div>
              <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-900">{c.estado}</span>
            </div>
            <div className="mt-4 overflow-x-auto">
              <table className="w-full min-w-[520px] text-left text-sm"><thead className="border-b border-atelier-stone text-xs uppercase text-neutral-500"><tr><th className="py-2">Concepto</th><th>Cantidad</th><th>Precio</th><th className="text-right">Importe</th></tr></thead><tbody>{c.detalles.map((d) => <tr key={`${c.id}-${d.concepto}`} className="border-b border-neutral-100"><td className="py-2"><strong>{d.concepto}</strong>{d.descripcion && <span className="block text-xs text-neutral-500">{d.descripcion}</span>}</td><td>{d.cantidad}</td><td>{moneda.format(d.precioUnitario)}</td><td className="text-right">{moneda.format(d.importe)}</td></tr>)}</tbody></table>
            </div>
            <dl className="ml-auto mt-4 grid max-w-sm grid-cols-2 gap-2 text-sm"><dt>Subtotal</dt><dd className="text-right">{moneda.format(c.subtotal)}</dd><dt>Impuesto</dt><dd className="text-right">{moneda.format(c.impuesto)}</dd><dt className="font-semibold">Total</dt><dd className="text-right font-semibold">{moneda.format(c.total)}</dd><dt>Adelanto ({c.porcentajeAdelanto}%)</dt><dd className="text-right">{moneda.format(c.adelantoRequerido)}</dd></dl>
            {c.venceEn && <p className={`mt-4 text-sm ${vencida && c.estado === 'ENVIADA' ? 'font-semibold text-red-700' : 'text-neutral-500'}`}>Vigencia hasta {fecha.format(new Date(c.venceEn))}</p>}
            <div className="mt-4 flex flex-wrap gap-2">
              {gestor && c.estado === 'BORRADOR' && <button onClick={() => enviar.mutate(c.id)} disabled={enviar.isPending} className="inline-flex items-center gap-2 rounded-lg bg-atelier-accent px-4 py-2 font-semibold text-white"><Send size={16} /> Enviar al cliente</button>}
              {gestor && c.estado === 'ENVIADA' && vencida && <button onClick={() => renovar.mutate(c.id)} disabled={renovar.isPending} className="rounded-lg bg-atelier-ink px-4 py-2 font-semibold text-white">Crear renovación</button>}
              {cliente && c.estado === 'ENVIADA' && !vencida && <><button onClick={() => responder.mutate({ id: c.id, aceptar: true })} className="rounded-lg bg-emerald-700 px-4 py-2 font-semibold text-white">Aceptar</button><button onClick={() => setRechazoId(c.id)} className="rounded-lg border border-red-700 px-4 py-2 font-semibold text-red-700">Rechazar</button></>}
            </div>
            {rechazoId === c.id && <form onSubmit={(e) => { e.preventDefault(); responder.mutate({ id: c.id, aceptar: false, observacion: motivoRechazo }, { onSuccess: () => { setRechazoId(null); setMotivoRechazo(''); } }); }} className="mt-3 grid gap-2 rounded-lg border border-red-200 bg-red-50 p-3"><label className="text-sm font-semibold text-red-900">Motivo del rechazo<textarea value={motivoRechazo} onChange={(e) => setMotivoRechazo(e.target.value)} required maxLength={2000} className={`${campo} mt-1 w-full`} /></label><div className="flex gap-2"><button className="rounded-lg bg-red-700 px-4 py-2 font-semibold text-white">Confirmar rechazo</button><button type="button" onClick={() => setRechazoId(null)} className="px-4 py-2 font-semibold">Cancelar</button></div></form>}
            {c.estado === 'ACEPTADA' && <PaymentSection cotizacionId={c.id} tipoCotizacion={c.tipo} puedeRegistrar={cliente && (c.tipo==='ADICIONAL'?proyecto.estado==='EN_DISENO':['ESPERANDO_PAGO', 'PAGO_EN_REVISION'].includes(proyecto.estado))} onChange={refrescar} />}
          </article>
        );
      })}
      {cotizaciones.data?.length === 0 && <p className="rounded-xl bg-white p-5 text-neutral-500">Aún no hay cotizaciones.</p>}
      {gestor && proyecto.estado === 'EN_COTIZACION' && (
        <form onSubmit={crearCotizacion} className="grid gap-4 rounded-xl border border-atelier-stone bg-white p-5">
          <div><p className="text-xs font-semibold uppercase tracking-wider text-atelier-accent">Nueva versión</p><h2 className="mt-1 text-xl font-semibold">Preparar cotización</h2></div>
          <div className="grid gap-3 sm:grid-cols-3"><label className="grid gap-1 text-sm">Tipo<select name="tipo" className={campo}><option value="INICIAL">Inicial</option></select></label><label className="grid gap-1 text-sm">Impuesto %<input name="porcentajeImpuesto" type="number" min="0" max="100" step="0.01" defaultValue="18" required className={campo} /></label><label className="grid gap-1 text-sm">Adelanto %<input name="porcentajeAdelanto" type="number" min="0" max="100" step="0.01" defaultValue="50" required className={campo} /></label></div>
          {filas.map((fila, i) => <fieldset key={fila} className="grid gap-3 rounded-lg bg-atelier-paper p-4 sm:grid-cols-2"><legend className="px-1 text-sm font-semibold">Partida {i + 1}</legend><input name={`concepto-${i}`} required maxLength={180} placeholder="Concepto" className={campo} /><input name={`descripcion-${i}`} maxLength={500} placeholder="Descripción" className={campo} /><input name={`cantidad-${i}`} type="number" min="0.01" step="0.01" defaultValue="1" required className={campo} /><input name={`precio-${i}`} type="number" min="0" step="0.01" placeholder="Precio unitario" required className={campo} /></fieldset>)}
          <button type="button" onClick={() => setFilas((actual) => [...actual, Math.max(...actual) + 1])} className="inline-flex w-fit items-center gap-2 text-sm font-semibold text-atelier-accent"><Plus size={16} /> Agregar partida</button>
          <textarea name="observacion" maxLength={2000} placeholder="Observaciones comerciales" className={campo} />
          {crear.isError && <p role="alert" className="text-sm text-red-700">No fue posible crear la cotización. Revisa los datos y el estado del proyecto.</p>}
          <button disabled={crear.isPending} className="rounded-lg bg-atelier-ink px-4 py-3 font-semibold text-white">{crear.isPending ? 'Guardando…' : 'Guardar borrador'}</button>
        </form>
      )}
    </section>
  );
}

function PaymentSection({ cotizacionId, tipoCotizacion, puedeRegistrar, onChange }: { cotizacionId: string; tipoCotizacion: 'INICIAL'|'ADICIONAL'; puedeRegistrar: boolean; onChange: () => Promise<void> }) {
  const qc = useQueryClient();
  const pagos = useQuery({ queryKey: ['cotizacion', cotizacionId, 'pagos'], queryFn: () => api.listarPagos(cotizacionId) });
  const registrar = useMutation({ mutationFn: (form: FormData) => api.registrarPago(cotizacionId, form), onSuccess: async () => { await qc.invalidateQueries({ queryKey: ['cotizacion', cotizacionId, 'pagos'] }); await onChange(); } });
  function submit(e: FormEvent<HTMLFormElement>) { e.preventDefault(); const form = new FormData(e.currentTarget); registrar.mutate(form); }
  return <div className="mt-5 border-t border-atelier-stone pt-5"><h4 className="flex items-center gap-2 font-semibold"><ReceiptText size={18} /> Comprobantes</h4><div className="mt-3 grid gap-2">{pagos.data?.map((p) => <div key={p.id} className="flex flex-wrap items-center justify-between gap-2 rounded-lg bg-atelier-paper px-3 py-2 text-sm"><span>{p.tipo} · {moneda.format(p.monto)} · {fecha.format(new Date(p.pagadoEn))}</span><span className={`font-semibold ${p.estado === 'VALIDADO' ? 'text-emerald-700' : p.estado === 'RECHAZADO' ? 'text-red-700' : 'text-amber-800'}`}>{p.estado}</span><a href={p.comprobanteUrl} target="_blank" rel="noreferrer" className="text-atelier-accent underline">Ver archivo</a>{p.motivoRechazo && <span className="w-full text-red-700">{p.motivoRechazo}</span>}</div>)}</div>{puedeRegistrar && <form onSubmit={submit} className="mt-4 grid gap-3 rounded-lg border border-atelier-stone p-4 sm:grid-cols-2"><select name="tipo" className={campo}>{tipoCotizacion==='ADICIONAL'?<option value="ADICIONAL">Adicional</option>:<><option value="ADELANTO">Adelanto</option><option value="SALDO">Saldo</option></>}</select><select name="metodo" className={campo}><option value="TRANSFERENCIA">Transferencia</option><option value="DEPOSITO">Depósito</option><option value="TARJETA">Tarjeta</option><option value="EFECTIVO">Efectivo</option><option value="OTRO">Otro</option></select><input name="monto" type="number" min="0.01" step="0.01" required placeholder="Monto" className={campo} /><input name="numeroOperacion" maxLength={80} placeholder="Número de operación" className={campo} /><label className="grid gap-1 text-sm sm:col-span-2">Comprobante PDF, JPG o PNG<input name="archivo" type="file" required accept="application/pdf,image/jpeg,image/png" className={campo} /></label>{registrar.isError && <p role="alert" className="text-sm text-red-700 sm:col-span-2">No se pudo registrar el comprobante.</p>}<button disabled={registrar.isPending} className="rounded-lg bg-atelier-accent px-4 py-2.5 font-semibold text-white sm:col-span-2">{registrar.isPending ? 'Subiendo…' : 'Registrar pago'}</button></form>}</div>;
}
