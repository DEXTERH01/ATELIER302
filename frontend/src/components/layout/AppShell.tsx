import {
  BriefcaseBusiness,
  Building2,
  FolderKanban,
  LayoutDashboard,
  LogOut,
  Menu,
  Users,
  X,
} from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../features/auth/AuthContext';
import type { Rol } from '../../types/auth';

const links: { to: string; label: string; icon: typeof LayoutDashboard; roles?: Rol[] }[] = [
  { to: '/dashboard', label: 'Resumen', icon: LayoutDashboard },
  { to: '/proyectos', label: 'Proyectos', icon: FolderKanban },
  { to: '/clientes', label: 'Clientes', icon: Building2, roles: ['ADMINISTRADOR', 'ENCARGADO'] },
  { to: '/empleados', label: 'Empleados', icon: BriefcaseBusiness, roles: ['ADMINISTRADOR'] },
  { to: '/usuarios', label: 'Usuarios', icon: Users, roles: ['ADMINISTRADOR'] },
];
export function AppShell() {
  const { usuario, logout } = useAuth();
  const [abierto, setAbierto] = useState(false);
  const disponibles = links.filter((l) => !l.roles || l.roles.includes(usuario!.rol));
  return (
    <div className="min-h-screen bg-atelier-paper lg:grid lg:grid-cols-[250px_1fr]">
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-atelier-stone bg-white px-4 py-3 lg:hidden">
        <span className="font-bold tracking-[.25em] text-atelier-accent">ATELIER</span>
        <button aria-label="Abrir navegación" className="rounded-md p-2" onClick={() => setAbierto(true)}>
          <Menu />
        </button>
      </header>
      {abierto && (
        <button
          aria-label="Cerrar navegación"
          className="fixed inset-0 z-30 bg-black/30 lg:hidden"
          onClick={() => setAbierto(false)}
        />
      )}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-[280px] flex-col border-r border-atelier-stone bg-white p-5 transition-transform lg:sticky lg:top-0 lg:h-screen lg:w-auto lg:translate-x-0 ${abierto ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <div className="flex items-center justify-between">
          <span className="font-bold tracking-[.28em] text-atelier-accent">ATELIER</span>
          <button aria-label="Cerrar navegación" className="p-2 lg:hidden" onClick={() => setAbierto(false)}>
            <X />
          </button>
        </div>
        <div className="mt-8 border-y border-atelier-stone py-4">
          <p className="font-semibold">{usuario?.nombreCompleto}</p>
          <p className="mt-1 text-xs uppercase tracking-wider text-neutral-500">{usuario?.rol}</p>
        </div>
        <nav className="mt-6 grid gap-1">
          {disponibles.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setAbierto(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium ${isActive ? 'bg-atelier-accent text-white' : 'text-neutral-700 hover:bg-atelier-paper'}`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
        <button
          className="mt-auto flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-neutral-700 hover:bg-atelier-paper"
          onClick={() => void logout()}
        >
          <LogOut size={18} />
          Cerrar sesión
        </button>
      </aside>
      <main className="min-w-0">
        <Outlet />
      </main>
    </div>
  );
}
