# Arquitectura

ATELIER Gestión Digital es un monolito modular con una SPA React/TypeScript y una API REST Spring Boot. PostgreSQL/Supabase es la única fuente de datos; Cloudinary almacena archivos. El navegador nunca recibe credenciales de esas plataformas.

La autorización se aplica en Spring Security y en los servicios de negocio. RLS no se usa como frontera de autorización en esta versión: el usuario de conexión de Render debe ser propietario de las tablas o tener permisos equivalentes para migraciones y operaciones de la aplicación.

El access token JWT es breve y vive en memoria. El refresh token es opaco, rotativo y se guarda como cookie de primera parte; la base conserva solo SHA-256. Netlify y Vite redirigen `/api` para conservar el comportamiento de la cookie.

Los PDF y demás documentos se suben mediante el adaptador Cloudinary con `resource_type=auto` (o `raw` si el proveedor lo requiere). Siempre se persisten `secure_url`, `public_id`, MIME, tamaño y categoría. El adaptador compensará una carga si falla la transacción.

Las migraciones Flyway son la fuente ejecutable del esquema. `database/` conserva sin cambios los entregables originales.

## Estados y autorización de proyectos

La matriz de estados vive en `PoliticaEstadosProyecto`; controladores y clientes no pueden saltarla. `CERRADO` y `CANCELADO` son terminales, cancelar exige comentario y entrar a cotización exige todos los requerimientos obligatorios completos. Desde `EXPEDIENTE_FINAL`, la ruta depende de `requiereTramitePublico`.

Una transición obtiene un bloqueo pesimista, valida la regla, actualiza `proyectos` y agrega `historial_estados` dentro de la misma transacción. El correlativo anual utiliza `INSERT ... ON CONFLICT ... DO UPDATE RETURNING`, por lo que dos solicitudes concurrentes no reciben el mismo número.

La visibilidad se aplica también en servicios: gestores ven todo, clientes solo lo propio y especialistas únicamente proyectos presentes en sus asignaciones. Ningún endpoint permite eliminar físicamente un proyecto.

