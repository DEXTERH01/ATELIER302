import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import { listarEmpleados } from '../../../lib/api/directories';
export function EmployeesPage() {
  const [q, setQ] = useState('');
  const datos = useQuery({ queryKey: ['empleados', q], queryFn: () => listarEmpleados(q) });
  return (
    <>
      <PageHeader eyebrow="Administración" title="Empleados" />
      <section className="mx-auto max-w-7xl p-5 md:p-9">
        <input
          aria-label="Buscar empleados"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Buscar por cargo"
          className="mb-4 w-full rounded-lg border border-atelier-stone bg-white px-4 py-3"
        />
        <div className="overflow-x-auto rounded-xl border border-atelier-stone bg-white">
          <table className="w-full min-w-[700px] text-left text-sm">
            <thead className="bg-atelier-paper">
              <tr>
                <th className="p-4">Nombre</th>
                <th className="p-4">Cargo</th>
                <th className="p-4">Tipo</th>
                <th className="p-4">Disponibilidad</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-atelier-stone">
              {datos.data?.contenido.map((e) => (
                <tr key={e.id}>
                  <td className="p-4">
                    <strong>{e.nombreCompleto}</strong>
                    <br />
                    <span className="text-xs text-neutral-500">{e.correo}</span>
                  </td>
                  <td className="p-4">{e.cargo}</td>
                  <td className="p-4">{e.tipo}</td>
                  <td className="p-4">{e.disponible ? 'Disponible' : 'No disponible'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {datos.isPending && <p className="p-5">Cargando…</p>}
        </div>
      </section>
    </>
  );
}
