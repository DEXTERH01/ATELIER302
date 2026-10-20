import { z } from 'zod';
export const loginSchema = z.object({
  correo: z.email('Correo inválido'),
  password: z.string().min(1, 'La contraseña es obligatoria'),
});
export const registerSchema = z.object({
  correo: z.email('Correo inválido'),
  password: z
    .string()
    .min(12, 'Mínimo 12 caracteres')
    .max(72, 'Máximo 72 caracteres')
    .regex(/^\S+$/, 'No debe contener espacios')
    .regex(/[a-z]/, 'Incluye una minúscula')
    .regex(/[A-Z]/, 'Incluye una mayúscula')
    .regex(/\d/, 'Incluye un número')
    .regex(/[^A-Za-z0-9]/, 'Incluye un símbolo'),
  nombres: z.string().min(1, 'Los nombres son obligatorios'),
  apellidos: z.string().min(1, 'Los apellidos son obligatorios'),
  telefono: z.string().optional(),
  tipoPersona: z.enum(['NATURAL', 'JURIDICA']),
  tipoDocumento: z.enum(['DNI', 'CE', 'RUC', 'PASAPORTE']),
  numeroDocumento: z.string().min(6, 'Documento inválido').max(20),
  razonSocial: z.string().optional(),
  direccion: z.string().optional(),
  distrito: z.string().optional(),
});
export type LoginForm = z.infer<typeof loginSchema>;
export type RegisterForm = z.infer<typeof registerSchema>;
