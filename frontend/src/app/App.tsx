import { Link, Route, Routes } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { ProtectedRoute } from './router/ProtectedRoute';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { RegisterPage } from '../features/auth/pages/RegisterPage';
import { AppShell } from '../components/layout/AppShell';
import { AtelierLogo } from '../components/brand/AtelierLogo';
const DashboardPage = lazy(() =>
  import('../features/dashboard/pages/DashboardPage').then((m) => ({ default: m.DashboardPage })),
);
const ProjectsPage = lazy(() =>
  import('../features/proyectos/pages/ProjectsPage').then((m) => ({ default: m.ProjectsPage })),
);
const ProjectDetailPage = lazy(() =>
  import('../features/proyectos/pages/ProjectDetailPage').then((m) => ({ default: m.ProjectDetailPage })),
);
const ClientsPage = lazy(() =>
  import('../features/clientes/pages/ClientsPage').then((m) => ({ default: m.ClientsPage })),
);
const EmployeesPage = lazy(() =>
  import('../features/empleados/pages/EmployeesPage').then((m) => ({ default: m.EmployeesPage })),
);
const UsersPage = lazy(() =>
  import('../features/usuarios/pages/UsersPage').then((m) => ({ default: m.UsersPage })),
);
const PaymentsPage = lazy(() =>
  import('../features/pagos/pages/PaymentsPage').then((m) => ({ default: m.PaymentsPage })),
);
const SpecialtiesPage = lazy(() =>
  import('../features/especialidades/pages/SpecialtiesPage').then((m) => ({ default: m.SpecialtiesPage })),
);
const ReportsPage = lazy(() =>
  import('../features/reportes/pages/ReportsPage').then((m) => ({ default: m.ReportsPage })),
);
const AuditPage = lazy(() =>
  import('../features/auditoria/pages/AuditPage').then((m) => ({ default: m.AuditPage })),
);
function Home() {
  return (
    <main className="min-h-screen p-6 md:p-12">
      <nav className="mx-auto flex max-w-6xl items-center justify-between">
        <AtelierLogo className="h-auto w-48 md:w-60" />
        <Link className="rounded-md border border-atelier-ink px-4 py-2" to="/login">
          Ingresar
        </Link>
      </nav>
      <section className="mx-auto grid min-h-[72vh] max-w-6xl content-center">
        <div className="mb-8 h-px w-20 bg-atelier-accent" />
        <p className="uppercase tracking-[.25em] text-atelier-accent">Arquitectura con trazabilidad</p>
        <h1 className="mt-4 max-w-4xl text-5xl font-semibold leading-tight md:text-7xl">
          Cada proyecto, claro desde la solicitud hasta la entrega.
        </h1>
        <p className="mt-6 max-w-2xl text-lg text-neutral-600">
          Gestión digital segura para clientes, especialistas y el equipo ATELIER.
        </p>
      </section>
    </main>
  );
}
export function App() {
  return (
    <Suspense fallback={<div className="grid min-h-screen place-items-center">Cargando módulo…</div>}>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registro" element={<RegisterPage />} />
        <Route
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/proyectos" element={<ProjectsPage />} />
          <Route path="/proyectos/:id" element={<ProjectDetailPage />} />
          <Route
            path="/pagos"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR']}>
                <PaymentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/especialidades"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR']}>
                <SpecialtiesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/clientes"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR', 'ENCARGADO']}>
                <ClientsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/empleados"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR']}>
                <EmployeesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR']}>
                <UsersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reportes"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR', 'ENCARGADO']}>
                <ReportsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/auditoria"
            element={
              <ProtectedRoute roles={['ADMINISTRADOR']}>
                <AuditPage />
              </ProtectedRoute>
            }
          />
        </Route>
        <Route
          path="*"
          element={
            <main className="p-8">
              <h1>Página no encontrada</h1>
              <Link to="/">Volver al inicio</Link>
            </main>
          }
        />
      </Routes>
    </Suspense>
  );
}
