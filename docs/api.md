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

## Cotizaciones

- `GET|POST /proyectos/{id}/cotizaciones`: lista visible o crea un borrador; crear requiere `ADMINISTRADOR` o `ENCARGADO`.
- `GET|PATCH /cotizaciones/{id}`: consulta o edita únicamente un borrador.
- `POST /cotizaciones/{id}/enviar`: gestor; fija `emitidaEn` y `venceEn` a 10 días.
- `POST /cotizaciones/{id}/responder`: cliente propietario; `{aceptar, observacion}`. El rechazo exige observación.
- `POST /cotizaciones/{id}/renovar`: gestor; solo para una versión enviada y vencida. Marca la anterior `VENCIDA` y crea un nuevo borrador.

Todos los importes usan dos decimales y `HALF_UP`. Las partidas son obligatorias y el total se deriva de subtotal más impuesto.

## Pagos y expediente

- `GET|POST /cotizaciones/{id}/pagos`: lista visible o registra un comprobante; registrar es exclusivo del cliente propietario y usa `multipart/form-data` con `tipo`, `metodo`, `monto`, `numeroOperacion` opcional, `pagadoEn` opcional y `archivo`.
- `GET /pagos`: pagos pendientes, solo `ADMINISTRADOR`.
- `POST /pagos/{id}/validar`: solo `ADMINISTRADOR`; acumula adelantos validados.
- `POST /pagos/{id}/rechazar`: solo `ADMINISTRADOR`; cuerpo `{motivo}` obligatorio.
- `GET|POST /proyectos/{id}/expediente`: consulta visible o creación manual idempotente por gestor. La ruta normal lo crea automáticamente cuando el adelanto alcanza lo requerido.

El comprobante admite PDF, JPG, JPEG y PNG con extensión y MIME concordantes. Exceder `MAX_UPLOAD_BYTES` responde `400 INVALID_FILE`. La API almacena `secure_url`, `public_id`, MIME, tamaño, categoría y `resource_type`; si la transacción falla, solicita a Cloudinary eliminar el recurso recién cargado.

