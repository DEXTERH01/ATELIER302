import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { FormField } from '../../../components/ui/FormField';
import { useAuth } from '../AuthContext';
import { registerSchema, type RegisterForm } from '../schemas/authSchemas';
import { AuthCard } from './LoginPage';
export function RegisterPage() {
  const auth = useAuth();
  const nav = useNavigate();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: { tipoPersona: 'NATURAL', tipoDocumento: 'DNI' },
  });
  const submit = handleSubmit(async (v) => {
    try {
      await auth.register(v);
      nav('/dashboard');
    } catch {
      setError('root', {
        message: 'No pudimos completar el registro; revisa si el correo o documento ya existe',
      });
    }
  });
  return (
    <AuthCard title="Registro de cliente">
      <form onSubmit={submit} className="grid gap-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Nombres" error={errors.nombres?.message} {...register('nombres')} />
          <FormField label="Apellidos" error={errors.apellidos?.message} {...register('apellidos')} />
        </div>
        <FormField label="Correo" type="email" error={errors.correo?.message} {...register('correo')} />
        <FormField
          label="Contraseña segura"
          type="password"
          error={errors.password?.message}
          {...register('password')}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-1 text-sm font-medium">
            Tipo de persona
            <select
              className="rounded-md border border-atelier-stone bg-white px-3 py-2.5"
              {...register('tipoPersona')}
            >
              <option value="NATURAL">Natural</option>
              <option value="JURIDICA">Jurídica</option>
            </select>
          </label>
          <label className="grid gap-1 text-sm font-medium">
            Documento
            <select
              className="rounded-md border border-atelier-stone bg-white px-3 py-2.5"
              {...register('tipoDocumento')}
            >
              <option>DNI</option>
              <option>CE</option>
              <option>RUC</option>
              <option>PASAPORTE</option>
            </select>
          </label>
        </div>
        <FormField
          label="Número de documento"
          error={errors.numeroDocumento?.message}
          {...register('numeroDocumento')}
        />
        <FormField label="Teléfono (opcional)" {...register('telefono')} />
        {errors.root && (
          <p role="alert" className="text-red-700">
            {errors.root.message}
          </p>
        )}
        <button
          className="rounded-md bg-atelier-accent px-4 py-3 font-semibold text-white disabled:opacity-60"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>
    </AuthCard>
  );
}
