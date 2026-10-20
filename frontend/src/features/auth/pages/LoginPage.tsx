import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { FormField } from '../../../components/ui/FormField';
import { AtelierLogo } from '../../../components/brand/AtelierLogo';
import { useAuth } from '../AuthContext';
import { loginSchema, type LoginForm } from '../schemas/authSchemas';
export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });
  const submit = handleSubmit(async (values) => {
    try {
      await auth.login(values);
      navigate('/dashboard');
    } catch {
      setError('root', { message: 'Correo o contraseña incorrectos' });
    }
  });
  return (
    <AuthCard title="Iniciar sesión">
      <form onSubmit={submit} className="grid gap-4">
        <FormField
          label="Correo"
          type="email"
          autoComplete="email"
          error={errors.correo?.message}
          {...register('correo')}
        />
        <FormField
          label="Contraseña"
          type="password"
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
        />
        {errors.root && (
          <p role="alert" className="text-red-700">
            {errors.root.message}
          </p>
        )}
        <button
          className="rounded-md bg-atelier-accent px-4 py-3 font-semibold text-white disabled:opacity-60"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Ingresando…' : 'Ingresar'}
        </button>
        <p className="text-sm">
          ¿Primera vez?{' '}
          <Link className="font-semibold text-atelier-accent underline" to="/registro">
            Crear cuenta de cliente
          </Link>
        </p>
      </form>
    </AuthCard>
  );
}
export function AuthCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <main className="grid min-h-screen place-items-center p-5">
      <section className="w-full max-w-md rounded-xl border border-atelier-stone bg-white p-7 shadow-xl">
        <Link to="/" className="block w-64 max-w-full" aria-label="Volver al inicio">
          <AtelierLogo className="h-auto w-full" />
        </Link>
        <h1 className="mb-6 mt-6 text-3xl font-semibold">{title}</h1>
        {children}
      </section>
    </main>
  );
}
