import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, CheckCircle2, ClipboardList, History, Landmark, Trash2 } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { PageHeader } from '../../../components/layout/PageHeader';
import { StatusBadge } from '../../../components/ui/StatusBadge';
import { useAuth } from '../../auth/AuthContext';
import type { EstadoProyecto } from '../../../types/domain';
import * as api from '../api/proyectosApi';
import { estadoEtiquetas, siguientesEstados } from '../projectState';
import { ProjectCommercialPanel } from '../../cotizaciones/components/ProjectCommercialPanel';

const campo = 'rounded-lg border border-atelier-stone bg-white px-3 py-2.5';
const fechaHora = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'America/Lima',
});
export function ProjectDetailPage() {
  const { id = '' } = useParams();
  const { usuario } = useAuth();
  const qc = useQueryClient();
  const [tab, setTab] = useState<'requerimientos' | 'comercial' | 'historial'>('requerimientos');
  const proyecto = useQuery({ queryKey: ['proyecto', id], queryFn: () => api.obtener(id), enabled: !!id });
  const requisitos = useQuery({
    queryKey: ['proyecto', id, 'requerimientos'],
    queryFn: () => api.listarRequerimientos(id),
    enabled: !!id,
  });
  const historial = useQuery({
    queryKey: ['proyecto', id, 'historial'],
    queryFn: () => api.obtenerHistorial(id),
    enabled: !!id,
  });
  const invalidar = async () => {
    await qc.invalidateQueries({ queryKey: ['proyecto', id] });
    await qc.invalidateQueries({ queryKey: ['dashboard'] });
  };
  const transicion = useMutation({
    mutationFn: ({ estado, comentario }: { estado: EstadoProyecto; comentario?: string }) =>
      api.transicionar(id, estado, comentario),
    onSuccess: invalidar,
  });
  const crearReq = useMutation({
    mutationFn: (input: api.RequerimientoInput) => api.crearRequerimiento(id, input),
    onSuccess: async () => qc.invalidateQueries({ queryKey: ['proyecto', id, 'requerimientos'] }),
  });
  const actualizarReq = useMutation({
    mutationFn: ({ reqId, input }: { reqId: string; input: api.RequerimientoInput }) =>
      api.actualizarRequerimiento(reqId, input),
    onSuccess: async () => qc.invalidateQueries({ queryKey: ['proyecto', id, 'requerimientos'] }),
  });
  const eliminarReq = useMutation({
    mutationFn: api.eliminarRequerimiento,
    onSuccess: async () => qc.invalidateQueries({ queryKey: ['proyecto', id, 'requerimientos'] }),
  });
  const gestor = usuario?.rol === 'ADMINISTRADOR' || usuario?.rol === 'ENCARGADO';
  const editable =
    gestor ||
    (usuario?.rol === 'CLIENTE' &&
      ['SOLICITUD_REGISTRADA', 'DATOS_INCOMPLETOS'].includes(proyecto.data?.estado ?? ''));
  function submitReq(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    crearReq.mutate({
      categoria: String(f.get('categoria')),
      descripcion: String(f.get('descripcion')),
      prioridad: String(f.get('prioridad')) as api.RequerimientoInput['prioridad'],
      obligatorio: f.get('obligatorio') === 'on',
      completado: f.get('completado') === 'on',
    });
    e.currentTarget.reset();
  }
  function cambiarEstado(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    transicion.mutate({
      estado: String(f.get('estado')) as EstadoProyecto,
      comentario: String(f.get('comentario') || '') || undefined,
    });
  }
  if (proyecto.isPending) return <p className="p-9">Cargando proyecto…</p>;
  if (!proyecto.data) return <p className="p-9 text-red-700">No fue posible cargar el proyecto.</p>;
  const p = proyecto.data;
  const siguientes = siguientesEstados(p.estado, p.requiereTramitePublico);
  return (
    <>
      <PageHeader eyebrow={p.codigo} title={p.nombre}>
        <Link
          className="inline-flex items-center gap-2 text-sm font-semibold text-atelier-accent"
          to="/proyectos"
        >
          <ArrowLeft size={17} />
          Volver
        </Link>
      </PageHeader>
      <section className="mx-auto grid max-w-7xl gap-6 p-5 md:p-9 xl:grid-cols-[1fr_340px]">
        <div className="min-w-0">
          <article className="rounded-xl border border-atelier-stone bg-white p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <StatusBadge estado={p.estado} />
              <span className="text-sm text-neutral-500">Solicitud: {p.fechaSolicitud}</span>
            </div>
            <dl className="mt-6 grid gap-5 sm:grid-cols-2">
              <Dato label="Cliente" value={p.cliente} />
              <Dato label="Encargado" value={p.encargado ?? 'Sin asignar'} />
              <Dato label="Tipo" value={p.tipoProyecto} />
              <Dato label="Ubicación" value={p.ubicacion ?? 'No indicada'} />
              <Dato label="Área" value={p.areaTerreno ? `${p.areaTerreno} m²` : 'No indicada'} />
              <Dato label="Trámite público" value={p.requiereTramitePublico ? 'Sí' : 'No'} />
            </dl>
            {p.descripcion && (
              <p className="mt-6 border-t border-atelier-stone pt-5 text-neutral-700">{p.descripcion}</p>
            )}
          </article>
          <div className="mt-6 flex gap-2 border-b border-atelier-stone">
            <button
              className={`flex items-center gap-2 px-4 py-3 text-sm font-semibold ${tab === 'comercial' ? 'border-b-2 border-atelier-accent text-atelier-accent' : ''}`}
              onClick={() => setTab('comercial')}
            >
              <Landmark size={17} />
              Comercial
            </button>
            <button
              className={`flex items-center gap-2 px-4 py-3 text-sm font-semibold ${tab === 'requerimientos' ? 'border-b-2 border-atelier-accent text-atelier-accent' : ''}`}
              onClick={() => setTab('requerimientos')}
            >
              <ClipboardList size={17} />
              Requerimientos
            </button>
            <button
              className={`flex items-center gap-2 px-4 py-3 text-sm font-semibold ${tab === 'historial' ? 'border-b-2 border-atelier-accent text-atelier-accent' : ''}`}
              onClick={() => setTab('historial')}
            >
              <History size={17} />
              Historial
            </button>
          </div>
          {tab === 'requerimientos' ? (
            <section className="mt-4 grid gap-3">
              {requisitos.data?.map((r) => (
                <article
                  key={r.id}
                  className="flex flex-col gap-3 rounded-xl border border-atelier-stone bg-white p-4 sm:flex-row sm:items-center"
                >
                  <button
                    disabled={!editable || actualizarReq.isPending}
                    aria-label={r.completado ? 'Marcar pendiente' : 'Marcar completado'}
                    onClick={() =>
                      actualizarReq.mutate({ reqId: r.id, input: { ...r, completado: !r.completado } })
                    }
                    className="text-atelier-accent disabled:opacity-40"
                  >
                    <CheckCircle2
                      className={r.completado ? 'fill-emerald-100 text-emerald-700' : 'text-neutral-400'}
                    />
                  </button>
                  <div className="flex-1">
                    <div className="flex flex-wrap gap-2">
                      <strong>{r.categoria}</strong>
                      <span className="text-xs uppercase text-neutral-500">
                        {r.prioridad} · {r.obligatorio ? 'Obligatorio' : 'Opcional'}
                      </span>
                    </div>
                    <p className="mt-1 text-sm text-neutral-700">{r.descripcion}</p>
                  </div>
                  {editable && (
                    <button
                      aria-label="Eliminar requerimiento"
                      className="text-red-700"
                      onClick={() => eliminarReq.mutate(r.id)}
                    >
                      <Trash2 size={18} />
                    </button>
                  )}
                </article>
              ))}
              {requisitos.data?.length === 0 && (
                <p className="rounded-xl bg-white p-5 text-neutral-500">No hay requerimientos registrados.</p>
              )}
              {editable && (
                <form
                  onSubmit={submitReq}
                  className="mt-2 grid gap-3 rounded-xl border border-atelier-stone bg-white p-4 sm:grid-cols-2"
                >
                  <h2 className="font-semibold sm:col-span-2">Agregar requerimiento</h2>
                  <input name="categoria" required maxLength={60} placeholder="Categoría" className={campo} />
                  <select name="prioridad" className={campo}>
                    <option>MEDIA</option>
                    <option>ALTA</option>
                    <option>BAJA</option>
                  </select>
                  <textarea
                    name="descripcion"
                    required
                    maxLength={5000}
                    placeholder="Descripción"
                    className={`${campo} sm:col-span-2`}
                  />
                  <label className="flex items-center gap-2 text-sm">
                    <input name="obligatorio" type="checkbox" defaultChecked />
                    Obligatorio
                  </label>
                  <label className="flex items-center gap-2 text-sm">
                    <input name="completado" type="checkbox" />
                    Ya completado
                  </label>
                  <button className="rounded-lg bg-atelier-ink px-4 py-2.5 font-semibold text-white sm:col-span-2">
                    Agregar
                  </button>
                </form>
              )}
            </section>
          ) : tab === 'comercial' ? (
            <ProjectCommercialPanel proyecto={p} />
          ) : (
            <section className="mt-4 grid gap-3">
              {historial.data?.map((h) => (
                <article key={h.id} className="rounded-xl border border-atelier-stone bg-white p-4">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <strong>
                      {h.estadoAnterior ? `${estadoEtiquetas[h.estadoAnterior]} → ` : ''}
                      {estadoEtiquetas[h.estadoNuevo]}
                    </strong>
                    <time className="text-xs text-neutral-500">
                      {fechaHora.format(new Date(h.cambiadoEn))}
                    </time>
                  </div>
                  <p className="mt-1 text-sm text-neutral-600">
                    {h.responsable}
                    {h.comentario ? ` · ${h.comentario}` : ''}
                  </p>
                </article>
              ))}
            </section>
          )}
        </div>
        <aside>
          {gestor && siguientes.length > 0 && (
            <form
              onSubmit={cambiarEstado}
              className="sticky top-5 grid gap-3 rounded-xl border border-atelier-stone bg-white p-5"
            >
              <h2 className="text-lg font-semibold">Cambiar estado</h2>
              <select name="estado" className={campo}>
                {siguientes.map((e) => (
                  <option key={e} value={e}>
                    {estadoEtiquetas[e]}
                  </option>
                ))}
              </select>
              <textarea
                name="comentario"
                maxLength={500}
                placeholder="Comentario (obligatorio al cancelar)"
                className={campo}
              />
              {transicion.isError && (
                <p role="alert" className="text-sm text-red-700">
                  La transición no está permitida o existen requisitos pendientes.
                </p>
              )}
              <button
                disabled={transicion.isPending}
                className="rounded-lg bg-atelier-accent px-4 py-2.5 font-semibold text-white"
              >
                {transicion.isPending ? 'Actualizando…' : 'Confirmar transición'}
              </button>
            </form>
          )}
        </aside>
      </section>
    </>
  );
}
function Dato({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wider text-neutral-500">{label}</dt>
      <dd className="mt-1 font-medium">{value}</dd>
    </div>
  );
}
