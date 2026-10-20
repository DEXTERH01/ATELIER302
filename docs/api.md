# API v1

Prefijo: `/api/v1`. JSON salvo cargas multipart. Fechas ISO 8601 y errores uniformes con `correlationId`.

## Autenticación

- `POST /auth/register-client`: registra siempre un usuario `CLIENTE`, crea su perfil y abre sesión.
- `POST /auth/login`: abre sesión y establece la cookie de refresh.
- `POST /auth/refresh`: rota la cookie y entrega un access token.
- `POST /auth/logout`: revoca la familia y elimina la cookie.
- `GET /auth/me`: devuelve el usuario autenticado.

La contraseña de registro exige 12-72 caracteres, mayúscula, minúscula, número, símbolo y ausencia de espacios. El cuerpo de registro no incluye rol.

## Usuarios, clientes y empleados

- `GET|POST /usuarios`, `GET|PATCH /usuarios/{id}` y `PATCH /usuarios/{id}/estado`: solo `ADMINISTRADOR`.
- `GET /empleados`: `ADMINISTRADOR` o `ENCARGADO`; `POST /empleados` y `PATCH /empleados/{id}`: solo `ADMINISTRADOR`.
- `GET|POST /clientes`: `ADMINISTRADOR` o `ENCARGADO`.
- `GET|PATCH /clientes/{id}` y `GET /clientes/{id}/proyectos`: gestores o el cliente propietario.

## Proyectos

- `GET /proyectos`: devuelve todos para gestores, propios para clientes y asignados para especialistas.
- `POST /proyectos`: gestores o clientes; el backend ignora cualquier `clienteId`/`encargadoId` enviado por un cliente y utiliza su perfil.
- `GET|PATCH /proyectos/{id}`: sujeto a visibilidad y propiedad. El cliente solo modifica datos en los estados iniciales.
- `POST /proyectos/{id}/transiciones`: solo gestores; valida la matriz central y guarda proyecto e historial en una transacción.
- `GET /proyectos/{id}/historial`.
- `GET|POST /proyectos/{id}/requerimientos`.
- `PATCH|DELETE /requerimientos/{id}`.
- `GET /dashboard/resumen`: métricas filtradas por el alcance del usuario.

Los listados usan `pagina`, `tamano` y `q`, y responden `{contenido,pagina,tamano,totalElementos,totalPaginas}`. No existe endpoint para eliminar proyectos físicamente.

