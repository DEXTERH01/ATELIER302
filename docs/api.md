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

## Ejecución técnica

- `GET /especialidades`: cualquier usuario autenticado.
- `POST /especialidades` y `PATCH /especialidades/{id}`: solo `ADMINISTRADOR`.
- `GET|POST /proyectos/{id}/asignaciones` y `PATCH /asignaciones/{id}`: la consulta respeta visibilidad; la escritura requiere `ADMINISTRADOR` o `ENCARGADO`.
- `GET|POST /asignaciones/{id}/tareas` y `PATCH /tareas/{id}`: los gestores crean; el especialista responsable actualiza avance sin cambiar la estructura de la tarea.
- `GET|POST /proyectos/{id}/estudios` y `PATCH /estudios/{id}`: los gestores planifican y validan; el responsable puede actualizar avance y resultado, pero no validar ni marcar `NO_APLICA`.

Las asignaciones y tareas usan transiciones centrales y estados terminales. Una asignación solo se finaliza cuando todas sus tareas están completadas o canceladas. Un estudio requerido no admite `NO_APLICA`; completar cualquier estudio exige resultado, fecha y documento vigente. Mientras exista un estudio requerido sin validar se bloquea `EN_ESTUDIOS → EN_DISENO`.

## Documentos del expediente

- `GET|POST /expedientes/{id}/documentos`: lista o carga `multipart/form-data` con `tipoDocumento`, `categoria`, `estudioId` opcional, `visibleCliente` y `archivo`.
- `GET|DELETE /documentos/{id}`: obtiene o retira el documento.

Las categorías habilitadas en esta fase son `EXPEDIENTE` y `ESTUDIO`. El especialista solo adjunta documentos al proyecto asignado y, para un estudio, debe ser su responsable; únicamente un gestor puede autorizar visibilidad al cliente. El cliente propietario recibe solo documentos vigentes y autorizados.

Se admiten PDF, JPG/JPEG y PNG con extensión y MIME concordantes. La carpeta es `atelier/{ambiente}/proyectos/{codigo}/expediente` o `.../estudios`. Cada nueva versión retira lógicamente la anterior de la misma serie y estudio opcional. Si la transacción revierte se elimina el recurso recién subido; una eliminación explícita solo marca el registro no vigente después de que Cloudinary confirme la operación.

