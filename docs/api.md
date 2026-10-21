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
- `POST /proyectos/{id}/expediente/completar`: gestor; exige propuesta `PROYECTO_FINAL` conforme y al menos un documento vigente de categoría `EXPEDIENTE`.

El comprobante admite PDF, JPG, JPEG y PNG con extensión y MIME concordantes. Exceder `MAX_UPLOAD_BYTES` responde `400 INVALID_FILE`. La API almacena `secure_url`, `public_id`, MIME, tamaño, categoría y `resource_type`; si la transacción falla, solicita a Cloudinary eliminar el recurso recién cargado.

## Ejecución técnica

- `GET /especialidades`: cualquier usuario autenticado.
- `POST /especialidades` y `PATCH /especialidades/{id}`: solo `ADMINISTRADOR`.
- `GET|POST /proyectos/{id}/asignaciones` y `PATCH /asignaciones/{id}`: la consulta respeta visibilidad; la escritura requiere `ADMINISTRADOR` o `ENCARGADO`.
- `GET|POST /asignaciones/{id}/tareas` y `PATCH /tareas/{id}`: los gestores crean; el especialista responsable actualiza avance sin cambiar la estructura de la tarea.
- `GET|POST /proyectos/{id}/estudios` y `PATCH /estudios/{id}`: los gestores planifican y validan; el responsable puede actualizar avance y resultado, pero no validar ni marcar `NO_APLICA`.

Las asignaciones y tareas usan transiciones centrales y estados terminales. Una asignación solo se finaliza cuando todas sus tareas están completadas o canceladas. Un estudio requerido no admite `NO_APLICA`; completar cualquier estudio exige resultado, fecha y documento vigente. Mientras exista un estudio requerido sin validar se bloquea `EN_ESTUDIOS → EN_DISENO`.

## Documentos del expediente

- `GET|POST /expedientes/{id}/documentos`: lista o carga `multipart/form-data` con `tipoDocumento`, `categoria`, `estudioId` o `tramiteId` opcional según la categoría, `visibleCliente` y `archivo`.
- `GET|DELETE /documentos/{id}`: obtiene o retira el documento.

Las categorías operativas son `EXPEDIENTE`, `ESTUDIO`, `TRAMITE` y `RESOLUCION`. `TRAMITE` y `RESOLUCION` exigen `tramiteId` y rol gestor; `ESTUDIO` exige `estudioId`. El especialista solo adjunta documentos al proyecto asignado y, para un estudio, debe ser su responsable; únicamente un gestor puede autorizar visibilidad al cliente. El cliente propietario recibe solo documentos vigentes y autorizados.

Se admiten PDF, JPG/JPEG y PNG con extensión y MIME concordantes. La carpeta es `atelier/{ambiente}/proyectos/{codigo}/expediente` o `.../estudios`. Cada nueva versión retira lógicamente la anterior de la misma serie y estudio opcional. Si la transacción revierte se elimina el recurso recién subido; una eliminación explícita solo marca el registro no vigente después de que Cloudinary confirme la operación.

## Propuestas y observaciones del cliente

- `GET /proyectos/{id}/propuestas`: versiones visibles; el cliente no recibe borradores.
- `POST /proyectos/{id}/propuestas`: `multipart/form-data` con `tipo`, `descripcion` opcional y `archivo`; gestores o especialista asignado, únicamente en `EN_DISENO`.
- `GET /propuestas/{id}`: consulta sujeta a propiedad o asignación.
- `POST /propuestas/{id}/enviar`: solo gestor; cambia el proyecto a `REVISION_CLIENTE`.
- `POST /propuestas/{id}/respuesta`: cliente propietario; cuerpo `{conforme, observacion}`. Una respuesta no conforme exige observación.
- `GET|POST /propuestas/{id}/observaciones`: consulta visible o creación por el cliente propietario.
- `PATCH /observaciones-cliente/{id}`: gestor; cuerpo `{estado, adicional}`.

Cada `tipo` (`ANTEPROYECTO` o `PROYECTO_FINAL`) conserva una secuencia de versiones y rondas independiente. Las rondas 1 y 2 no pueden marcarse adicionales. La ronda 3 y siguientes pueden vincular una cotización `ADICIONAL` pasando `observacionClienteId` al endpoint de creación de cotizaciones. Un pago para esta cotización debe usar `tipo=ADICIONAL`; su validación no reinicia el estado técnico ni crea otro expediente.

La vista previa admite PDF, JPG/JPEG y PNG y persiste `secure_url`, `public_id`, nombre, MIME, tamaño y `resource_type`. Si PostgreSQL revierte, se ejecuta la compensación Cloudinary. Una propuesta observada pasa a `REEMPLAZADA` cuando se envía su nueva versión.

## Trámites públicos y cierre

- `GET|POST /proyectos/{id}/tramites`: consulta visible o preparación por gestor.
- `GET|PATCH /tramites/{id}`: consulta o actualización; `PREPARACION → PRESENTADO` exige expediente `COMPLETO` y número externo, y cambia el proyecto a `EN_TRAMITE_PUBLICO`.
- `GET|POST /tramites/{id}/observaciones`: consulta o registro manual de una observación de entidad. El proyecto pasa a `CON_OBSERVACIONES`.
- `POST /observaciones-entidad/{id}/subsanar`: registra respuesta y fecha. Al atender la última pendiente, el trámite vuelve a revisión y el proyecto a `EN_TRAMITE_PUBLICO`.
- `POST /tramites/{id}/resolver`: cuerpo `{aprobado, fechaRespuesta, observacionGeneral}`. Aprobar exige cero observaciones pendientes y una resolución vigente asociada; el resultado produce `APROBADO` o `NO_VIABLE`.

Las operaciones de trámite son exclusivas de `ADMINISTRADOR` y `ENCARGADO`; clientes y especialistas solo consultan proyectos dentro de su alcance. Los cambios del trámite, proyecto, historial y auditoría comparten una transacción. Para cerrar, el gestor utiliza la transición del proyecto desde `APROBADO` o `NO_VIABLE`; el expediente queda `ARCHIVADO` en la misma transacción.

## Reportes y auditoría

- `GET /reportes/proyectos.xlsx`, `GET /reportes/pagos.xlsx` y `GET /reportes/carga-tecnica.xlsx`: descargan libros XLSX con cabecera, filtro y formato monetario. Solo `ADMINISTRADOR` y `ENCARGADO`. Cada descarga genera un evento `EXPORT` de auditoría.
- `GET /auditoria?pagina=0&tamano=30`: eventos ordenados del más reciente al más antiguo. Solo `ADMINISTRADOR`.

La auditoría registra inicios de sesión y mutaciones de negocio. No expone hashes de contraseña, access tokens ni refresh tokens.

