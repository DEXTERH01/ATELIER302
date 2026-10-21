# Arquitectura

ATELIER Gestión Digital es un monolito modular con una SPA React/TypeScript y una API REST Spring Boot. PostgreSQL/Supabase es la única fuente de datos; Cloudinary almacena archivos. El navegador nunca recibe credenciales de esas plataformas.

La autorización se aplica en Spring Security y en los servicios de negocio. RLS no se usa como frontera de autorización en esta versión: el usuario de conexión de Render debe ser propietario de las tablas o tener permisos equivalentes para migraciones y operaciones de la aplicación.

El access token JWT es breve y vive en memoria. El refresh token es opaco, rotativo y se guarda como cookie de primera parte; la base conserva solo SHA-256. Netlify y Vite redirigen `/api` para conservar el comportamiento de la cookie.

Los PDF y demás documentos se suben mediante el adaptador Cloudinary con `resource_type=auto` (o `raw` si el proveedor lo requiere). Siempre se persisten `secure_url`, `public_id`, MIME, tamaño y categoría. El adaptador compensará una carga si falla la transacción.

Las migraciones Flyway son la fuente ejecutable del esquema. `database/` conserva sin cambios los entregables originales.

## Estados y autorización de proyectos

La matriz de estados vive en `PoliticaEstadosProyecto`; controladores y clientes no pueden saltarla. `CERRADO` y `CANCELADO` son terminales, cancelar exige comentario y entrar a cotización exige todos los requerimientos obligatorios completos. Desde `EXPEDIENTE_FINAL`, la ruta depende de `requiereTramitePublico`.

Una transición obtiene un bloqueo pesimista, valida la regla, actualiza `proyectos` y agrega `historial_estados` dentro de la misma transacción. El correlativo anual utiliza `INSERT ... ON CONFLICT ... DO UPDATE RETURNING`, por lo que dos solicitudes concurrentes no reciben el mismo número.

Los estados `COTIZACION_ENVIADA`, `ESPERANDO_PAGO`, `PAGO_EN_REVISION` y `PROYECTO_ACTIVO` no se aceptan desde la transición genérica: pertenecen al flujo comercial. Cotizaciones y pagos bloquean las filas relevantes, conservan versiones anteriores y escriben el historial en la misma transacción. La validación suma únicamente pagos `ADELANTO` en estado `VALIDADO`; al alcanzar el porcentaje requerido crea el expediente mediante una operación idempotente protegida por el bloqueo del proyecto y una restricción única.

La carga Cloudinary ocurre antes de confirmar la transacción. Se registra una sincronización transaccional que elimina el `public_id` usando el `resource_type` real cuando el commit no se completa. Para retirar un documento, Cloudinary debe confirmar primero la eliminación; si no lo hace, PostgreSQL conserva el documento vigente y devuelve un error de negocio para reintento o conciliación.

## Agregado de ejecución técnica

`Proyecto` gobierna asignaciones, tareas y estudios. Los servicios bloquean o validan el proyecto y aplican una matriz de permisos adicional a la seguridad HTTP: los gestores administran la planificación; el especialista trabaja solo en su asignación activa y el cliente conserva acceso de lectura acotado.

Crear un estudio obligatorio desde `PROYECTO_ACTIVO` produce `EN_ESTUDIOS` e historial dentro de la misma transacción. Un estudio solo queda `COMPLETADO` si tiene resultado, fecha y documento vigente. Al validar el último requerido, el servicio bloquea el proyecto, cambia a `EN_DISENO` y agrega el historial; la transición manual aplica la misma guarda.

Los documentos se versionan por expediente, tipo, categoría y estudio opcional. El registro anterior permanece para trazabilidad pero deja de estar vigente. Las relaciones se conservan y ningún flujo de esta fase elimina físicamente proyectos, asignaciones, tareas, estudios o documentos.

## Agregado de propuestas y revisiones

Las propuestas son versiones inmutables por proyecto y tipo. El bloqueo pesimista del proyecto serializa la asignación de versión, el envío, la respuesta y el número de ronda. Las vistas previas se almacenan mediante el mismo puerto Cloudinary y la misma compensación transaccional utilizada por documentos y pagos.

El flujo de estado pertenece al servicio de propuestas: enviar produce `EN_DISENO → REVISION_CLIENTE`; observar devuelve a `EN_DISENO`; la conformidad de anteproyecto continúa en diseño y la conformidad de proyecto final produce `EXPEDIENTE_FINAL`. El endpoint genérico no puede producir `REVISION_CLIENTE` ni `EXPEDIENTE_FINAL`.

`observaciones_cliente.id_cotizacion_adicional` conserva la trazabilidad comercial de una ronda posterior a las dos incluidas. La cotización y su pago adicional no cambian el estado del proyecto. Antes de continuar, el backend verifica cotización aceptada y suma de pagos `ADICIONAL` validados contra el adelanto requerido, siempre con `BigDecimal`.

La visibilidad se aplica también en servicios: gestores ven todo, clientes solo lo propio y especialistas únicamente proyectos presentes en sus asignaciones. Ningún endpoint permite eliminar físicamente un proyecto.

## Agregado de trámite público y cierre

`TramitePublico` concentra la presentación, revisión, observaciones y resolución manual recibida de una entidad externa. El trámite y sus observaciones usan bloqueos pesimistas; la subsanación bloquea primero el trámite para serializar respuestas concurrentes. Los estados públicos del proyecto no se aceptan por la transición genérica cuando `requiereTramitePublico=true`.

Antes de presentar, el expediente debe estar `COMPLETO`. Esa marca exige una propuesta `PROYECTO_FINAL` conforme y un documento vigente de categoría `EXPEDIENTE`. Una observación genera `CON_OBSERVACIONES`; la última subsanación pendiente devuelve el proyecto a `EN_TRAMITE_PUBLICO`. Aprobar exige además una resolución vigente vinculada mediante `documentos.id_tramite`; rechazar produce `NO_VIABLE`. Cada cambio guarda historial y auditoría en la misma transacción.

La transición final del proyecto a `CERRADO` archiva también el expediente digital (`ARCHIVADO`). Los documentos de categorías `TRAMITE` y `RESOLUCION` se versionan por trámite, mantienen los metadatos Cloudinary y solo pueden ser cargados por gestores.

## Observabilidad, reportes y cierre

La auditoría de aplicación registra el actor, recurso, acción y detalle funcional sin almacenar secretos. Solo los administradores pueden consultarla. Los roles de gestión exportan tres reportes XLSX mediante Apache POI: proyectos por estado, pagos y carga técnica. Cada exportación se audita como `EXPORT`.

En producción, `CORS_ALLOWED_ORIGINS` debe contener el origen exacto de Netlify, sin una barra final. La SPA usa el proxy `/api`, por lo que el navegador conserva la cookie de refresh como cookie de primera parte; Supabase y Cloudinary permanecen exclusivamente del lado de Render.

