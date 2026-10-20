# ADR-001 — Autenticación administrada por el backend

## Decisión

Spring Security administra correo/contraseña, BCrypt, JWT y refresh tokens. Supabase se usa exclusivamente como PostgreSQL administrado.

## Consecuencias

- El frontend no conoce claves de Supabase ni Cloudinary.
- La autorización por rol y propiedad se verifica en backend.
- Los refresh tokens se rotan; reutilizar uno revocado invalida toda su familia.
- Los proxies de Vite y Netlify mantienen la cookie como primera parte.

