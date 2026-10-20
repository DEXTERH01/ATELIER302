import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Plus, Search } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { PageHeader } from '../../../components/layout/PageHeader';
import { StatusBadge } from '../../../components/ui/StatusBadge';
import { useAuth } from '../../auth/AuthContext';
import { listarClientes, listarEmpleados } from '../../../lib/api/directories';
import * as proyectosApi from '../api/proyectosApi';

const campo = 'rounded-lg border border-atelier-stone bg-white px-3 py-2.5';
export function ProjectsPage() {
  const { usuario } = useAuth();
  const queryClient = useQueryClient();
  const [q, setQ] = useState('');
  const [nuevo, setNuevo] = useState(false);
  const proyectos = useQuery({ queryKey: ['proyectos', q], queryFn: () => proyectosApi.listar(q) });
  const gestor = usuario?.rol === 'ADMINISTRADOR' || usuario?.rol === 'ENCARGADO';
  const clientes = useQuery({
    queryKey: ['clientes', 'selector'],
    queryFn: () => listarClientes(),
    enabled: gestor,
  });
  const empleados = useQuery({
    queryKey: ['empleados', 'selector'],
    queryFn: () => listarEmpleados(),
    enabled: gestor,
  });
  const crear = useMutation({
    mutationFn: proyectosApi.crear,
    onSuccess: async () => {
      setNuevo(false);
      await queryClient.invalidateQueries({ queryKey: ['proyectos'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    crear.mutate({
      clienteId: (f.get('clienteId') as string) || undefined,
      encargadoId: (f.get('encargadoId') as string) || undefined,
      nombre: String(f.get('nombre')),
      descripcion: String(f.get('descripcion') || '') || undefined,
      tipoProyecto: String(f.get('tipoProyecto')),
      ubicacion: String(f.get('ubicacion') || '') || undefined,
      areaTerreno: f.get('areaTerreno') ? Number(f.get('areaTerreno')) : undefined,
      requiereTramitePublico: f.get('requiereTramitePublico') === 'on',
      fechaFinEstimada: String(f.get('fechaFinEstimada') || '') || undefined,
    });
  }
  return (
    <>
      <PageHeader eyebrow="Gestión central" title="Proyectos">
        <button
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-atelier-accent px-4 py-2.5 font-semibold text-white"
          onClick={() => setNuevo((v) => !v)}
        >
          <Plus size={18} />
          {nuevo ? 'Cerrar' : 'Nuevo proyecto'}
        </button>
      </PageHeader>
      <section className="mx-auto max-w-7xl p-5 md:p-9">
        {nuevo && (
          <form
            onSubmit={submit}
            className="mb-7 grid gap-4 rounded-xl border border-atelier-stone bg-white p-5 md:grid-cols-2 xl:grid-cols-3"
          >
            <h2 className="text-xl font-semibold md:col-span-2 xl:col-span-3">Registrar solicitud</h2>
            {gestor && (
              <label className="grid gap-1 text-sm font-medium">
                Cliente
                <select name="clienteId" required className={campo}>
                  <option value="">Seleccionar</option>
                  {clientes.data?.contenido.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nombreCompleto} · {c.numeroDocumento}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <label className="grid gap-1 text-sm font-medium">
              Nombre
              <input name="nombre" required maxLength={180} className={campo} />
            </label>
            <label className="grid gap-1 text-sm font-medium">
              Tipo de proyecto
              <input name="tipoProyecto" required maxLength={60} className={campo} />
            </label>
            <label className="grid gap-1 text-sm font-medium">
              Ubicación
              <input name="ubicacion" maxLength={250} className={campo} />
            </label>
            <label className="grid gap-1 text-sm font-medium">
              Área del terreno (m²)
              <input name="areaTerreno" type="number" min="0.01" step="0.01" className={campo} />
            </label>
            <label className="grid gap-1 text-sm font-medium">
              Fecha estimada
              <input name="fechaFinEstimada" type="date" className={campo} />
            </label>
            {gestor && (
              <label className="grid gap-1 text-sm font-medium">
                Encargado
                <select name="encargadoId" className={campo}>
                  <option value="">Sin asignar</option>
                  {empleados.data?.contenido.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.nombreCompleto} · {e.cargo}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <label className="flex items-center gap-2 self-end rounded-lg bg-atelier-paper px-3 py-3 text-sm">
              <input name="requiereTramitePublico" type="checkbox" />
              Requiere trámite público
            </label>
            <label className="grid gap-1 text-sm font-medium md:col-span-2 xl:col-span-3">
              Descripción
              <textarea name="descripcion" rows={3} maxLength={5000} className={campo} />
            </label>
            {crear.isError && (
              <p role="alert" className="text-sm text-red-700 md:col-span-2 xl:col-span-3">
                No se pudo registrar. Verifica los datos y vuelve a intentar.
              </p>
            )}
            <button
              disabled={crear.isPending}
              className="rounded-lg bg-atelier-ink px-4 py-2.5 font-semibold text-white disabled:opacity-60"
            >
              {crear.isPending ? 'Guardando…' : 'Registrar proyecto'}
            </button>
          </form>
        )}
        <div className="mb-4 flex items-center gap-2 rounded-lg border border-atelier-stone bg-white px-3">
          <Search size={18} className="text-neutral-500" />
          <input
            aria-label="Buscar proyectos"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Buscar por código o nombre"
            className="w-full bg-transparent py-3 outline-none"
          />
        </div>
        <div className="overflow-hidden rounded-xl border border-atelier-stone bg-white">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="bg-atelier-paper text-xs uppercase tracking-wider text-neutral-600">
                <tr>
                  <th className="px-4 py-3">Código</th>
                  <th className="px-4 py-3">Proyecto</th>
                  <th className="px-4 py-3">Cliente</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Encargado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-atelier-stone">
                {proyectos.data?.contenido.map((p) => (
                  <tr key={p.id} className="hover:bg-atelier-paper/60">
                    <td className="px-4 py-3 font-mono text-xs">
                      <Link className="font-semibold text-atelier-accent underline" to={`/proyectos/${p.id}`}>
                        {p.codigo}
                      </Link>
                    </td>
                    <td className="px-4 py-3 font-medium">{p.nombre}</td>
                    <td className="px-4 py-3">{p.cliente}</td>
                    <td className="px-4 py-3">
                      <StatusBadge estado={p.estado} />
                    </td>
                    <td className="px-4 py-3">{p.encargado ?? 'Sin asignar'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {proyectos.isPending && <p className="p-6 text-center text-neutral-500">Cargando proyectos…</p>}
          {proyectos.data?.contenido.length === 0 && (
            <p className="p-6 text-center text-neutral-500">No se encontraron proyectos.</p>
          )}
          {proyectos.isError && (
            <p role="alert" className="p-6 text-center text-red-700">
              No fue posible cargar los proyectos.
            </p>
          )}
        </div>
      </section>
    </>
  );
}
