import type { PropsWithChildren } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/AuthContext';
import type { Rol } from '../../types/auth';
export function ProtectedRoute({ children, roles }: PropsWithChildren<{ roles?: Rol[] }>) {
  const { usuario, cargando } = useAuth();
  if (cargando) return <div className="grid min-h-screen place-items-center">Cargando…</div>;
  if (!usuario) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(usuario.rol)) return <Navigate to="/dashboard" replace />;
  return children;
}
