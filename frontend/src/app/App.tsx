import {Link,Route,Routes} from 'react-router-dom';
import {ProtectedRoute} from './router/ProtectedRoute';
import {LoginPage} from '../features/auth/pages/LoginPage';
import {RegisterPage} from '../features/auth/pages/RegisterPage';
import {DashboardPage} from '../features/dashboard/pages/DashboardPage';
function Home(){return <main className="min-h-screen p-6 md:p-12"><nav className="mx-auto flex max-w-6xl items-center justify-between"><strong className="tracking-[.3em] text-atelier-accent">ATELIER</strong><Link className="rounded-md border border-atelier-ink px-4 py-2" to="/login">Ingresar</Link></nav><section className="mx-auto grid min-h-[75vh] max-w-6xl content-center"><p className="uppercase tracking-[.25em] text-atelier-accent">Arquitectura con trazabilidad</p><h1 className="mt-4 max-w-4xl text-5xl font-semibold leading-tight md:text-7xl">Cada proyecto, claro desde la solicitud hasta la entrega.</h1><p className="mt-6 max-w-2xl text-lg text-neutral-600">Gestión digital segura para clientes, especialistas y el equipo ATELIER.</p></section></main>;}
export function App(){return <Routes><Route path="/" element={<Home/>}/><Route path="/login" element={<LoginPage/>}/><Route path="/registro" element={<RegisterPage/>}/><Route path="/dashboard" element={<ProtectedRoute><DashboardPage/></ProtectedRoute>}/><Route path="*" element={<main className="p-8"><h1>Página no encontrada</h1><Link to="/">Volver al inicio</Link></main>}/></Routes>;}

