import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import { listarClientes } from '../../../lib/api/directories';
export function ClientsPage() {
  const [q, setQ] = useState('');
  const datos = useQuery({ queryKey: ['clientes', q], queryFn: () => listarClientes(q) });
  return (
    <>
      <PageHeader eyebrow="Directorio" title="Clientes" />
      <section className="mx-auto max-w-7xl p-5 md:p-9">
        <input
          aria-label="Buscar clientes"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Buscar por documento o correo"
          className="mb-4 w-full rounded-lg border border-atelier-stone bg-white px-4 py-3"
        />
        <div className="overflow-x-auto rounded-xl border border-atelier-stone bg-white">
          <table className="w-full min-w-[700px] text-left text-sm">
            <thead className="bg-atelier-paper">
              <tr>
                <th className="p-4">Nombre</th>
                <th className="p-4">Documento</th>
                <th className="p-4">Correo</th>
                <th className="p-4">Distrito</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-atelier-stone">
              {datos.data?.contenido.map((c) => (
                <tr key={c.id}>
                  <td className="p-4 font-medium">{c.nombreCompleto}</td>
                  <td className="p-4">
                    {c.tipoDocumento} {c.numeroDocumento}
                  </td>
                  <td className="p-4">{c.correo ?? 'Sin acceso'}</td>
                  <td className="p-4">{c.distrito ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {datos.isPending && <p className="p-5">Cargando…</p>}
          {datos.data?.contenido.length === 0 && <p className="p-5 text-neutral-500">No hay resultados.</p>}
        </div>
      </section>
    </>
  );
}
