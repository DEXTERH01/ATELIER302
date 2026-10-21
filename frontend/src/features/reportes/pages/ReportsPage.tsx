import { Download, FileSpreadsheet, Landmark, UsersRound } from 'lucide-react';
import { useState } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import { descargarReporte } from '../api/reportesApi';

const reportes = [
  { tipo: 'proyectos' as const, titulo: 'Proyectos por estado', descripcion: 'Códigos, responsables, fechas y estado operativo de cada proyecto.', icono: FileSpreadsheet },
  { tipo: 'pagos' as const, titulo: 'Pagos y validaciones', descripcion: 'Comprobantes registrados, importes, método y estado de validación.', icono: Landmark },
  { tipo: 'cargaTecnica' as const, titulo: 'Carga técnica', descripcion: 'Asignaciones y estudios técnicos para seguimiento del equipo.', icono: UsersRound },
];

export function ReportsPage() {
  const [descargando, setDescargando] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  async function descargar(tipo: (typeof reportes)[number]['tipo']) {
    setError(null); setDescargando(tipo);
    try { await descargarReporte(tipo); } catch { setError('No fue posible generar el reporte. Intenta nuevamente.'); } finally { setDescargando(null); }
  }
  return <><PageHeader eyebrow="Control y análisis" title="Reportes" />
    <section className="mx-auto grid max-w-7xl gap-4 p-5 md:grid-cols-3 md:p-9">
      {error && <p role="alert" className="rounded-lg bg-red-50 p-4 text-red-800 md:col-span-3">{error}</p>}
      {reportes.map(({ tipo, titulo, descripcion, icono: Icono }) => <article key={tipo} className="flex min-h-60 flex-col rounded-xl border border-atelier-stone bg-white p-6 shadow-sm">
        <Icono className="text-atelier-accent" size={28} aria-hidden="true" />
        <h2 className="mt-5 text-lg font-semibold">{titulo}</h2><p className="mt-2 text-sm leading-6 text-neutral-600">{descripcion}</p>
        <button onClick={() => void descargar(tipo)} disabled={descargando !== null} className="mt-auto inline-flex items-center justify-center gap-2 rounded-lg bg-atelier-accent px-4 py-2.5 font-semibold text-white disabled:opacity-60">
          <Download size={17} aria-hidden="true" />{descargando === tipo ? 'Generando…' : 'Descargar Excel'}
        </button>
      </article>)}
    </section>
  </>;
}
