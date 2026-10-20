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

## Verificar la sesión

1. Inicia sesión y comprueba en DevTools que `ATELIER_REFRESH` es `HttpOnly` y que no hay tokens en `localStorage`.
2. Llama `POST /api/v1/auth/refresh`: debe rotar la cookie y devolver otro access token.
3. Reutiliza el refresh anterior con un cliente HTTP: debe devolver `401` y revocar la familia completa.
4. Ejecuta logout: debe borrar la cookie y una renovación posterior debe devolver `401`.
5. Repite en Netlify y confirma que el navegador llama `/api`, no directamente a Render.

El proxy de Netlify usa el host definido en `frontend/netlify.toml`; debe coincidir con el servicio Render definitivo.

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

