# API v1

Prefijo: `/api/v1`. JSON salvo cargas multipart. Fechas ISO 8601 y errores uniformes con `correlationId`.

## Autenticación

- `POST /auth/register-client`: registra siempre un usuario `CLIENTE`, crea su perfil y abre sesión.
- `POST /auth/login`: abre sesión y establece la cookie de refresh.
- `POST /auth/refresh`: rota la cookie y entrega un access token.
- `POST /auth/logout`: revoca la familia y elimina la cookie.
- `GET /auth/me`: devuelve el usuario autenticado.

La contraseña de registro exige 12-72 caracteres, mayúscula, minúscula, número, símbolo y ausencia de espacios. El cuerpo de registro no incluye rol.

