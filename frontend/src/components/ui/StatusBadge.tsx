import { estadoEtiquetas } from '../../features/proyectos/projectState';
import type { EstadoProyecto } from '../../types/domain';
export function StatusBadge({ estado }: { estado: EstadoProyecto }) {
  const terminal = estado === 'CERRADO' || estado === 'APROBADO';
  const alerta = estado === 'CANCELADO' || estado === 'NO_VIABLE' || estado === 'CON_OBSERVACIONES';
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${terminal ? 'bg-emerald-100 text-emerald-800' : alerta ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-900'}`}
    >
      {estadoEtiquetas[estado]}
    </span>
  );
}
