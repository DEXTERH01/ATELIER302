import { AlertCircle, Building2, FolderKanban } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../../../components/layout/PageHeader';
import { useAuth } from '../../auth/AuthContext';
import { obtenerResumen } from '../api/dashboardApi';
import { estadoEtiquetas } from '../../proyectos/projectState';
import type { EstadoProyecto } from '../../../types/domain';
export function DashboardPage() {
  const { usuario } = useAuth();
  const resumen = useQuery({ queryKey: ['dashboard'], queryFn: obtenerResumen });
  const data = resumen.data;
  const cards = [
    { label: 'Proyectos visibles', value: data?.totalProyectos ?? 0, icon: FolderKanban },
    { label: 'Requerimientos pendientes', value: data?.requerimientosPendientes ?? 0, icon: AlertCircle },
    { label: 'Clientes', value: data?.totalClientes ?? 0, icon: Building2 },
  ];
  return (
    <>
      <PageHeader eyebrow={usuario?.rol ?? ''} title={`Bienvenido, ${usuario?.nombreCompleto ?? ''}`} />
      <section className="mx-auto max-w-7xl p-5 md:p-9">
        {resumen.isError && (
          <p role="alert" className="rounded-lg bg-red-50 p-4 text-red-800">
            No fue posible cargar el resumen.
          </p>
        )}
        <div className="grid gap-4 sm:grid-cols-3">
          {cards.map(({ label, value, icon: Icon }) => (
            <article key={label} className="rounded-xl border border-atelier-stone bg-white p-5 shadow-sm">
              <Icon className="text-atelier-accent" />
              <p className="mt-5 text-3xl font-semibold">{resumen.isPending ? '—' : value}</p>
              <p className="mt-1 text-sm text-neutral-600">{label}</p>
            </article>
          ))}
        </div>
        <section className="mt-7 rounded-xl border border-atelier-stone bg-white p-5">
          <h2 className="text-lg font-semibold">Distribución por estado</h2>
          <div className="mt-4 grid gap-2 sm:grid-cols-2 xl:grid-cols-3">
            {Object.entries(data?.porEstado ?? {}).map(([estado, total]) => (
              <div
                key={estado}
                className="flex items-center justify-between rounded-lg bg-atelier-paper px-3 py-2 text-sm"
              >
                <span>{estadoEtiquetas[estado as EstadoProyecto]}</span>
                <strong>{total}</strong>
              </div>
            ))}
            {data && Object.keys(data.porEstado).length === 0 && (
              <p className="text-neutral-500">Todavía no hay proyectos.</p>
            )}
          </div>
        </section>
      </section>
    </>
  );
}
