# Validación funcional integral

Ejecuta este recorrido sobre Supabase de demostración y Cloudinary configurado en Render. No reutilices un proyecto demostrativo ya cerrado: crea uno nuevo para no alterar la evidencia académica existente. Las cuentas demo tienen la contraseña `AtelierDemo2026!`.

Antes de iniciar, confirma que `https://atelier-gestion-digital-api.onrender.com/actuator/health` responde `UP` y que Render tiene `CORS_ALLOWED_ORIGINS=https://atelier-gestion-digital.netlify.app,http://localhost:5173`.

| Paso | Rol | Operación y evidencia esperada |
|---|---|---|
| 1 | Visitante / cliente nuevo | Registra un cliente desde `/registro`, inicia sesión, cierra sesión y vuelve a iniciar. En DevTools, `ATELIER_REFRESH` debe ser `HttpOnly`; no debe existir token en `localStorage`. |
| 2 | Cliente | Crea un proyecto con requerimientos. Debe recibir un código `PROY-AAAA-NNNN` y solo verlo desde su propia sesión. |
| 3 | Encargado / cliente / administrador | Encargado completa requerimientos, envía cotización; cliente acepta y carga PDF/JPG/PNG; administrador valida. El proyecto pasa a `PROYECTO_ACTIVO` y se crea un solo expediente. |
| 4 | Encargado / especialista | Crea especialidad, asignación, tarea y estudio obligatorio. Especialista modifica solo su tarea/estudio; tras entregar documento y validar el estudio el proyecto llega a `EN_DISENO`. |
| 5 | Encargado / cliente | Carga y envía propuesta; cliente registra observación. Repite hasta la tercera ronda y comprueba que se ofrece cotización adicional. La conformidad de `PROYECTO_FINAL` produce `EXPEDIENTE_FINAL`. |
| 6 | Encargado / administrador | Carga documento de expediente, completa expediente, prepara/presenta trámite, registra observación y subsanación, adjunta resolución y aprueba o marca no viable. Cierra el proyecto y confirma expediente `ARCHIVADO`. |
| 7 | Todos los roles | Cliente no consulta proyectos ajenos; especialista no edita asignaciones ajenas; cliente no valida pagos ni modifica trámite; encargado no accede a usuarios ni auditoría; administrador consulta auditoría y descarga los tres XLSX. |

## Evidencia Cloudinary

Después de cada carga, verifica en la respuesta de la API o en la pestaña correspondiente que se guardaron URL segura, `public_id`, MIME, tamaño y categoría. En Cloudinary revisa que los PDF estén en `raw` o `auto` y que el `public_id` coincida con PostgreSQL. Para una eliminación, comprueba primero que Cloudinary confirma la operación; solo entonces el documento queda retirado en la plataforma.

## Evidencia de permisos

Para cada intento denegado, registra el código HTTP esperado: `401` sin sesión, `403` por rol o propiedad y `422` por regla de negocio. La API devuelve además `correlationId`, útil para localizar el evento en logs de Render.

## Reporte de cierre

Guarda capturas de la auditoría, del historial del proyecto cerrado, de los tres archivos Excel descargados y de un recurso en Cloudinary. La prueba se considera cerrada cuando los siete pasos se completan sin `500`, los documentos quedan trazables y no existe acceso horizontal entre clientes.
