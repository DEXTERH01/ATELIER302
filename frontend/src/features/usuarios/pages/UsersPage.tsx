import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { PageHeader } from '../../../components/layout/PageHeader';
import { cambiarEstadoUsuario, listarUsuarios } from '../../../lib/api/directories';
export function UsersPage() {
  const [q, setQ] = useState('');
  const qc = useQueryClient();
  const datos = useQuery({ queryKey: ['usuarios', q], queryFn: () => listarUsuarios(q) });
  const estado = useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) => cambiarEstadoUsuario(id, activo),
    onSuccess: async () => qc.invalidateQueries({ queryKey: ['usuarios'] }),
  });
  return (
    <>
      <PageHeader eyebrow="Seguridad" title="Usuarios" />
      <section className="mx-auto max-w-7xl p-5 md:p-9">
        <input
          aria-label="Buscar usuarios"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Buscar por correo o nombre"
          className="mb-4 w-full rounded-lg border border-atelier-stone bg-white px-4 py-3"
        />
        <div className="overflow-x-auto rounded-xl border border-atelier-stone bg-white">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="bg-atelier-paper">
              <tr>
                <th className="p-4">Usuario</th>
                <th className="p-4">Rol</th>
                <th className="p-4">Estado</th>
                <th className="p-4">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-atelier-stone">
              {datos.data?.contenido.map((u) => (
                <tr key={u.id}>
                  <td className="p-4">
                    <strong>
                      {u.nombres} {u.apellidos}
                    </strong>
                    <br />
                    <span className="text-xs text-neutral-500">{u.correo}</span>
                  </td>
                  <td className="p-4">{u.rol}</td>
                  <td className="p-4">{u.activo ? 'Activo' : 'Inactivo'}</td>
                  <td className="p-4">
                    <button
                      disabled={estado.isPending}
                      className="rounded-md border border-atelier-stone px-3 py-2 font-medium"
                      onClick={() => estado.mutate({ id: u.id, activo: !u.activo })}
                    >
                      {u.activo ? 'Desactivar' : 'Activar'}
                    </button>
                  </td>
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
