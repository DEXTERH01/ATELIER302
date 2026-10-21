# ATELIER Gestión Digital

Monorepositorio para la plataforma web de gestión de proyectos arquitectónicos de ATELIER.

## Requisitos

- Java 21
- Node.js 22.22.2 o superior (22.19 ejecuta la suite actual, pero `jsdom` declara 22.22.2 como mínimo)
- PostgreSQL 15 o superior (Supabase en producción)
- Maven no es necesario: el proyecto incluye Maven Wrapper

## Backend local

Configura Java 21 solo para la sesión de PowerShell y exporta las variables de `backend/.env.example`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd backend
.\mvnw-java21.ps1 spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger: `http://localhost:8080/swagger-ui.html`.

Para probar comprobantes configura una cuenta Cloudinary en la sesión (`CLOUDINARY_CLOUD_NAME`,
`CLOUDINARY_API_KEY` y `CLOUDINARY_API_SECRET`). `MAX_UPLOAD_MB` limita la petición HTTP y
`MAX_UPLOAD_BYTES` aplica la validación de negocio; ambos deben representar el mismo límite.

## Frontend local

```powershell
cd frontend
Copy-Item .env.example .env
npm ci
npm run dev
```

Vite sirve `http://localhost:5173` y redirige `/api` a `http://localhost:8080`.

## Credenciales ficticias de desarrollo

Se cargan únicamente con el perfil `dev`. Contraseña común: `AtelierDemo2026!`.

| Rol | Correo |
|---|---|
| ADMINISTRADOR | `admin@atelier.demo` |
| ENCARGADO | `encargado@atelier.demo` |
| ESPECIALISTA | `arquitecta@atelier.demo` |
| CLIENTE | `cliente@atelier.demo` |

No uses estas credenciales ni la migración demo en producción.

## Base académica completa en Supabase

El archivo `database/02_datos_demo_supabase.sql` carga una demostración integral sobre una
base que ya tenga aplicadas las migraciones Flyway V1 a V9. Todos sus datos personales,
documentos y operaciones son sintéticos. La contraseña común es `AtelierDemo2026!`.

| Rol | Correo principal |
|---|---|
| ADMINISTRADOR | `admin@atelier.demo` |
| ENCARGADO | `diego.ramirez@atelier.demo` |
| ENCARGADO | `valeria.castro@atelier.demo` |
| ESPECIALISTA | `lucia.torres@atelier.demo` |
| CLIENTE | `elena.rojas@atelier.demo` |

La semilla incluye más especialistas y clientes, 17 proyectos distribuidos por todos los
estados, y sus relaciones comerciales, técnicas, documentales y de trámite. Se ejecuta una
sola vez desde **Supabase > SQL Editor** después de confirmar que Render arrancó con V9.

### Vaciar y reconstruir la base académica

Esta operación destruye todos los datos del esquema `public`. Antes de ejecutarla confirma
el nombre del proyecto Supabase y que no contiene información que debas conservar.

```sql
drop schema public cascade;
create schema public;
grant all on schema public to postgres;
grant all on schema public to service_role;
grant usage on schema public to anon, authenticated;
```

Después ejecuta **Manual Deploy > Deploy latest commit** en Render. Flyway recreará V1 a
V9. Cuando `/actuator/health` responda `UP`, ejecuta
`database/02_datos_demo_supabase.sql` completo en SQL Editor.

Los correos `@atelier.demo` son identificadores de acceso y no buzones. Java no bloquea
direcciones Gmail: actualmente solo se valida su formato. Para confirmar propiedad de un
correo real hará falta implementar posteriormente envío y verificación por token.

## Verificar la sesión

1. Inicia sesión y comprueba en DevTools que `ATELIER_REFRESH` es `HttpOnly` y que no hay tokens en `localStorage`.
2. Llama `POST /api/v1/auth/refresh`: debe rotar la cookie y devolver otro access token.
3. Reutiliza el refresh anterior con un cliente HTTP: debe devolver `401` y revocar la familia completa.
4. Ejecuta logout: debe borrar la cookie y una renovación posterior debe devolver `401`.
5. Repite en Netlify y confirma que el navegador llama `/api`, no directamente a Render.

El proxy de Netlify usa el host definido en `frontend/netlify.toml`; debe coincidir con el servicio Render definitivo.

## Variables de producción y CORS

En Render configura `SPRING_PROFILES_ACTIVE=prod`, `REFRESH_COOKIE_SECURE=true` y las credenciales de PostgreSQL, JWT y Cloudinary. Para este despliegue, la variable CORS debe ser exactamente:

```text
CORS_ALLOWED_ORIGINS=https://atelier-gestion-digital.netlify.app,http://localhost:5173
```

No uses comillas ni una barra final en el dominio. Si CORS no coincide, el backend puede responder `403 Invalid CORS request`; el formulario ya diferencia ese problema de una contraseña incorrecta. Netlify no necesita `VITE_API_BASE_URL` si se usa el proxy del archivo `frontend/netlify.toml`.

## Funcionalidad de Fase 2

- Los clientes crean proyectos únicamente para su propio perfil. Administradores y encargados pueden seleccionar cliente y encargado.
- Cada proyecto recibe un código atómico `PROY-AAAA-NNNN` y conserva todo cambio de estado en su historial.
- Solo administradores y encargados ejecutan transiciones. Los especialistas ven proyectos asignados y los clientes solo los propios.
- Los clientes pueden completar sus datos y requerimientos mientras el proyecto esté en `SOLICITUD_REGISTRADA` o `DATOS_INCOMPLETOS`.
- El dashboard, los proyectos y los directorios visibles se consultan desde la navegación adaptable del frontend.

## Funcionalidad de Fase 3

- Administradores y encargados crean cotizaciones versionadas con partidas, impuesto y adelanto calculados con `BigDecimal`.
- Al enviar una cotización se fija una vigencia inmutable de 10 días. El cliente propietario puede aceptarla o rechazarla; una cotización vencida se renueva creando otra versión.
- El cliente carga comprobantes PDF/JPG/PNG desde la pestaña **Comercial**. Se conservan `secure_url`, `public_id`, MIME, tamaño, categoría y `resource_type` de Cloudinary.
- Solo el administrador valida o rechaza pagos desde **Pagos**. Alcanzar el adelanto requerido activa el proyecto y crea un único expediente dentro de la misma transacción.
- La transición genérica no permite saltar los estados comerciales. Un proyecto nunca se activa sin adelanto validado.

### Recorrido manual de aceptación

1. Como encargado, lleva un proyecto con requerimientos completos a `EN_COTIZACION`, crea el borrador y envíalo.
2. Como cliente propietario, acepta la cotización y registra un adelanto con comprobante válido.
3. Como administrador, abre **Pagos** y valida el comprobante. Comprueba que el proyecto quede `PROYECTO_ACTIVO` y muestre su expediente.
4. Repite el envío con un archivo de extensión/MIME incompatible o mayor al límite: la API debe responder `400 INVALID_FILE`.
5. Fuerza un fallo de base de datos después de una carga en un entorno de prueba y verifica en los logs que la compensación elimine el `public_id` de Cloudinary.

## Funcionalidad de Fase 4

- El administrador mantiene el catálogo de especialidades. Administradores y encargados asignan el equipo técnico, crean tareas y planifican estudios.
- Cada especialista accede solo a proyectos con una asignación activa y puede actualizar únicamente sus propias tareas y estudios. Las asignaciones finalizadas no pueden reactivarse.
- Un estudio obligatorio lleva el proyecto a `EN_ESTUDIOS`. Para validarlo como completado se exige resultado, fecha y un entregable vigente; el último estudio obligatorio validado inicia `EN_DISENO` y registra el historial en la misma transacción.
- Las pestañas **Equipo y tareas**, **Estudios** y **Expediente** reúnen la ejecución técnica. El cliente ve únicamente documentos marcados expresamente como visibles.
- Los documentos PDF/JPG/PNG guardan URL segura, `public_id`, MIME, tamaño, categoría y `resource_type`. Un rollback compensa la carga; al retirar un archivo se exige primero la confirmación de Cloudinary y después se actualiza PostgreSQL.

### Recorrido manual de aceptación técnica

1. Como administrador, crea una especialidad; como encargado, asigna un especialista y crea una tarea desde **Equipo y tareas**.
2. Como especialista asignado, inicia y completa la tarea. Comprueba que no puedas alterar título, fechas o prioridad ni operar sobre asignaciones ajenas.
3. Como encargado, registra un estudio obligatorio: el proyecto debe pasar a `EN_ESTUDIOS`. Intenta validarlo sin archivo y confirma el rechazo.
4. Sube un PDF desde **Expediente**, categoría `ESTUDIO`, asociado al estudio; valida el estudio y comprueba el paso automático a `EN_DISENO` cuando no queden obligatorios pendientes.
5. Autoriza como gestor un documento del expediente y confirma que el cliente propietario pueda abrirlo. Los archivos no autorizados no deben aparecer para el cliente.
6. Simula una respuesta fallida de eliminación de Cloudinary: la API debe devolver `422` y el documento debe permanecer vigente.

## Funcionalidad de Fase 5

- Gestores y especialistas asignados crean versiones de anteproyecto o proyecto final con vista previa PDF/JPG/PNG almacenada en `atelier/{ambiente}/proyectos/{codigo}/propuestas`.
- Solo un gestor envía el borrador. El envío cambia el proyecto de `EN_DISENO` a `REVISION_CLIENTE`; el cliente propietario puede dar conformidad o registrar observaciones.
- Las rondas 1 y 2 están incluidas. Desde la ronda 3 el gestor puede mantenerla incluida o marcarla como adicional y crear una cotización `ADICIONAL` vinculada.
- Una ronda marcada adicional no puede considerarse atendida ni originar la siguiente versión hasta que su cotización esté aceptada y alcance el pago adicional validado requerido.
- La conformidad de un anteproyecto devuelve el proyecto a diseño para preparar la versión final. La conformidad del proyecto final genera `EXPEDIENTE_FINAL` con historial transaccional.
- `REVISION_CLIENTE` y `EXPEDIENTE_FINAL` no pueden alcanzarse mediante el endpoint genérico de transiciones.

### Recorrido manual de aceptación de propuestas

1. En un proyecto `EN_DISENO`, crea y envía una propuesta. Comprueba que el cliente no vea borradores y que el proyecto pase a `REVISION_CLIENTE` al enviarla.
2. Como cliente propietario, registra una observación. Comprueba que sea ronda 1 incluida y que el proyecto vuelva a `EN_DISENO`.
3. Repite con nuevas versiones hasta la ronda 3. Las dos primeras deben indicar **Incluida**; la tercera debe indicar **Evaluar adicional**.
4. Como encargado, marca la tercera ronda adicional y crea su cotización. Como cliente, acéptala y registra un pago `ADICIONAL`; como administrador, valídalo.
5. Antes del pago validado, intenta atender la observación o crear otra versión: la API debe rechazarlo. Después de validarlo, ambas operaciones deben quedar habilitadas.
6. Da conformidad a una propuesta `PROYECTO_FINAL` y verifica el estado `EXPEDIENTE_FINAL` y su entrada de historial.

## Funcionalidad de Fase 6

- El gestor completa el expediente final únicamente cuando existe una propuesta final conforme y al menos un documento vigente de expediente.
- Los trámites se preparan desde `EXPEDIENTE_FINAL`; presentarlos exige expediente `COMPLETO` y número externo. La presentación cambia el proyecto a `EN_TRAMITE_PUBLICO` con historial.
- Cada observación oficial conserva número correlativo, notificación, plazo opcional, respuesta y fecha de subsanación. El proyecto alterna transaccionalmente entre `CON_OBSERVACIONES` y `EN_TRAMITE_PUBLICO`.
- La aprobación queda bloqueada mientras existan observaciones pendientes o falte un documento `RESOLUCION` asociado al trámite. Una respuesta negativa produce `NO_VIABLE`.
- El cierre se realiza desde `APROBADO` o `NO_VIABLE` y cierra también el expediente. El cliente propietario puede consultar trámites, observaciones y documentos autorizados, pero no alterarlos.

### Recorrido manual de aceptación de trámite y cierre

1. En un proyecto `EXPEDIENTE_FINAL`, sube al menos un documento de categoría **Expediente** y pulsa **Marcar completo** en la pestaña **Trámites**.
2. Prepara un trámite con entidad, responsable, tipo y número externo; preséntalo y comprueba el estado `EN_TRAMITE_PUBLICO`.
3. Registra una observación de la entidad y verifica `CON_OBSERVACIONES`. Registra su subsanación y comprueba el retorno a `EN_TRAMITE_PUBLICO`.
4. Intenta aprobar sin resolución: la API debe rechazarlo. Adjunta un PDF/JPG/PNG de categoría **Resolución / licencia** y vuelve a aprobar.
5. Verifica `APROBADO`, su historial y la visibilidad del documento para el cliente cuando fue autorizado.
6. Ejecuta la transición a `CERRADO` y confirma que el expediente también quede cerrado. Repite en otro trámite con resultado **No viable** para validar esa rama.

## Funcionalidad de Fase 7 y cierre

- Los gestores descargan tres reportes Excel: proyectos por estado, pagos y carga técnica. Cada libro incluye cabecera corporativa, filtro, primera fila congelada y formato monetario.
- Los administradores consultan la auditoría de operaciones y exportaciones; los demás roles no reciben acceso a esta información.
- La navegación es adaptable desde 320 px, los controles poseen foco visible y las tablas amplias se desplazan horizontalmente sin cortar información.
- El recorrido integral, los roles involucrados y la evidencia esperada están en [docs/validacion-integral.md](docs/validacion-integral.md).

## Validación

```powershell
cd backend
.\mvnw-java21.ps1 verify
cd ..\frontend
npm ci
npm run lint
npm test
npm run build
```

Testcontainers se ejecuta en CI, donde Docker está disponible.

