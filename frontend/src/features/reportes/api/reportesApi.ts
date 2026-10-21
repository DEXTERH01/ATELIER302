import { api } from '../../../lib/api/client';

const archivos = {
  proyectos: { url: '/reportes/proyectos.xlsx', nombre: 'proyectos-por-estado.xlsx' },
  pagos: { url: '/reportes/pagos.xlsx', nombre: 'pagos.xlsx' },
  cargaTecnica: { url: '/reportes/carga-tecnica.xlsx', nombre: 'carga-tecnica.xlsx' },
} as const;

export async function descargarReporte(tipo: keyof typeof archivos) {
  const archivo = archivos[tipo];
  const { data } = await api.get<Blob>(archivo.url, { responseType: 'blob' });
  const url = URL.createObjectURL(data);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = archivo.nombre;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
}
