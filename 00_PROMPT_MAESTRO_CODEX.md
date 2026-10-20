# PROMPT MAESTRO PARA CODEX — ATELIER GESTIÓN DIGITAL

## Rol que debes asumir

Actúa como arquitecto de software, ingeniero senior full stack, especialista en Java/Spring Boot, React/TypeScript, PostgreSQL, seguridad, UX empresarial, pruebas y despliegue cloud.

Has sido contratado por la empresa **ATELIER** para diseñar e implementar un sistema web empresarial de gestión de proyectos arquitectónicos. Debes trabajar como parte de un equipo profesional: analizar antes de modificar, respetar el alcance, mantener una arquitectura limpia, documentar decisiones, ejecutar pruebas y entregar software funcional, no solamente maquetas.

El sistema reemplazará un proceso actualmente disperso entre documentos, mensajes, hojas de cálculo y seguimiento manual. El proceso futuro ya fue modelado en BPMN y la base de datos inicial ya fue diseñada.

## Objetivo general

Construir una plataforma web responsive que permita administrar de extremo a extremo un proyecto arquitectónico:

1. Registro de solicitud y requerimientos del cliente.
2. Revisión de información y solicitud de datos faltantes.
3. Elaboración, envío, aceptación o rechazo de cotizaciones.
4. Registro y validación manual de comprobantes de pago.
5. Activación del proyecto y creación automática del expediente digital.
6. Asignación de encargados, especialistas y tareas.
7. Registro de estudios técnicos y documentos.
8. Elaboración y revisión de propuestas arquitectónicas.
9. Gestión de observaciones del cliente y cambios adicionales.
10. Preparación del expediente final.
11. Seguimiento de trámites ante entidades públicas.
12. Registro y subsanación de observaciones municipales.
13. Aprobación, cierre y trazabilidad del proyecto.
14. Generación de reportes Excel y registro de auditoría.

## Material de referencia obligatorio

Antes de programar, localiza y revisa estos archivos si están presentes en el repositorio:

- `docs/ATELIER_TOBE_Gestion_Digital.bpmn`
- `database/01_supabase_postgresql.sql`
- `database/02_datos_demo_supabase.sql`
- `database/03_sqlserver.sql`
- `docs/ATELIER_Modelo_Datos_StarUML.mdj`
- `docs/README_MODELO_DATOS.md`

El archivo de PostgreSQL/Supabase es la fuente principal para la implementación. No cambies nombres de tablas, claves ni relaciones sin justificarlo y documentar una migración.

## Alcance de la primera versión

### Incluido

- Autenticación con correo y contraseña.
- Autorización por roles.
- Gestión de usuarios, clientes y empleados.
- Gestión completa de proyectos.
- Requerimientos del cliente.
- Cotizaciones versionadas y detalles.
- Comprobantes de pago y validación manual.
- Expediente digital.
- Integración con Cloudinary para archivos e imágenes.
- Catálogo de especialidades.
- Asignaciones y tareas.
- Estudios técnicos.
- Propuestas y revisiones del cliente.
- Trámites públicos y observaciones de entidades.
- Notificaciones internas.
- Historial de estados.
- Auditoría.
- Paneles por rol.
- Exportación de reportes Excel.
- Documentación OpenAPI/Swagger.
- Datos ficticios de demostración.
- Pruebas unitarias e integrales principales.
- Configuración para Netlify, Render y Supabase.

### Fuera de alcance en esta versión

- Pasarela de pago real.
- Integración real con bancos.
- Integración directa con municipalidades.
- Firma digital certificada.
- Envío real de WhatsApp o SMS.
- Aplicación móvil nativa.
- Microservicios.
- Inteligencia artificial.
- Implementación paralela en PHP.

Los pagos y las respuestas de entidades públicas deben simularse mediante registros internos controlados. La base de datos, autenticación, gestión de proyectos y almacenamiento de documentos sí deben funcionar realmente.

## Arquitectura obligatoria

Implementa un **monolito modular** con frontend y backend separados. No conviertas el sistema en microservicios.

```text
Usuario
   │
   ▼
Frontend React/TypeScript — Netlify
   │ HTTPS / JSON
   ▼
API REST Java Spring Boot — Render
   ├── PostgreSQL — Supabase
   └── Archivos e imágenes — Cloudinary
```

Reglas arquitectónicas:

- El frontend nunca debe conectarse directamente a PostgreSQL.
- El frontend nunca debe conocer la clave secreta de Cloudinary.
- Toda regla de negocio debe ejecutarse en el backend.
- Las contraseñas nunca se guardan en texto plano.
- Los controladores no deben contener lógica de negocio.
- Las entidades JPA no deben enviarse directamente como respuestas JSON.
- Utiliza DTO de entrada y salida.
- Todas las operaciones sensibles deben comprobar permisos.
- Las acciones relevantes deben dejar historial o auditoría.

## Tecnologías

### Backend

- Java 21 LTS.
- Spring Boot 3.x estable compatible con Java 21.
- Maven Wrapper.
- Spring Web.
- Spring Data JPA.
- Spring Security.
- JWT para autenticación.
- Bean Validation.
- PostgreSQL Driver.
- Flyway para migraciones.
- Springdoc OpenAPI/Swagger.
- JUnit 5, Mockito y MockMvc.
- Testcontainers para pruebas de integración cuando el entorno lo permita.

### Librerías Java exigidas por el curso

Debes utilizar y justificar al menos tres; en este proyecto se utilizarán las cuatro:

1. **Google Guava**
   - Validaciones internas con `Preconditions` cuando sean apropiadas.
   - Colecciones inmutables y utilidades de transformación.
   - No reemplazar Bean Validation en los DTO.

2. **Apache POI**
   - Generar reportes `.xlsx` de proyectos, pagos, tareas y estados.
   - Aplicar cabeceras, formatos de fecha, moneda y autoajuste de columnas.

3. **Apache Commons**
   - `commons-lang3` para validación y normalización de texto.
   - `commons-io` cuando sea necesario manipular nombres, extensiones o flujos de archivos.

4. **Logback con SLF4J**
   - Registro estructurado de inicio, advertencias y errores.
   - No registrar contraseñas, tokens, secretos ni documentos completos.
   - Configuración diferenciada para desarrollo y producción.

### Frontend

- React con TypeScript.
- Vite.
- React Router.
- TanStack Query para estado del servidor y caché.
- Axios con interceptor JWT o una capa `fetch` centralizada; escoge una sola opción.
- React Hook Form.
- Zod para validaciones del lado cliente.
- Tailwind CSS para el sistema visual.
- Lucide React para iconos sobrios.
- Vitest y React Testing Library.
- ESLint y Prettier.

### Infraestructura

- Netlify: frontend.
- Render: backend Java.
- Supabase: PostgreSQL.
- Cloudinary: imágenes y documentos.
- Git y GitHub: control de versiones.

## Estructura del repositorio

Utiliza un monorepositorio organizado de esta manera:

```text
atelier-gestion-digital/
├── README.md
├── .gitignore
├── docs/
│   ├── arquitectura.md
│   ├── decisiones/
│   ├── api.md
│   ├── ATELIER_TOBE_Gestion_Digital.bpmn
│   ├── ATELIER_Modelo_Datos_StarUML.mdj
│   └── README_MODELO_DATOS.md
├── database/
│   ├── 01_supabase_postgresql.sql
│   ├── 02_datos_demo_supabase.sql
│   └── 03_sqlserver.sql
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .env.example
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/atelier/gestion/
│       │   └── resources/
│       └── test/
└── frontend/
    ├── package.json
    ├── vite.config.ts
    ├── netlify.toml
    ├── .env.example
    ├── public/
    └── src/
```

## Organización del backend

Usa organización **por funcionalidad**, no una única carpeta global con decenas de controladores o entidades.

```text
com.atelier.gestion
├── AtelierApplication.java
├── common/
│   ├── api/
│   ├── exception/
│   ├── validation/
│   ├── util/
│   └── audit/
├── config/
├── security/
├── auth/
├── usuario/
├── cliente/
├── empleado/
├── proyecto/
├── cotizacion/
├── pago/
├── expediente/
├── especialidad/
├── asignacion/
├── tarea/
├── estudio/
├── propuesta/
├── tramite/
├── notificacion/
└── reporte/
```

Dentro de cada módulo utiliza solamente las carpetas necesarias:

```text
proyecto/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
└── specification/
```

Responsabilidades:

- `controller`: HTTP, códigos de respuesta y validación de DTO.
- `service`: casos de uso, transacciones y reglas de negocio.
- `repository`: acceso a datos.
- `entity`: mapeo JPA.
- `dto`: contratos de entrada y salida.
- `mapper`: conversión entre entidades y DTO.
- `specification`: filtros dinámicos y búsquedas.

## Organización del frontend

Utiliza organización por módulos funcionales:

```text
frontend/src/
├── app/
│   ├── router/
│   ├── providers/
│   └── store/
├── assets/
├── components/
│   ├── ui/
│   ├── layout/
│   └── feedback/
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── usuarios/
│   ├── clientes/
│   ├── proyectos/
│   ├── cotizaciones/
│   ├── pagos/
│   ├── expedientes/
│   ├── tareas/
│   ├── propuestas/
│   ├── tramites/
│   ├── notificaciones/
│   └── reportes/
├── hooks/
├── lib/
│   ├── api/
│   ├── auth/
│   └── validation/
├── pages/
├── types/
├── styles/
└── main.tsx
```

Cada `feature` puede contener:

```text
api/ components/ hooks/ pages/ schemas/ types/
```

No dupliques interfaces, formularios ni llamadas HTTP. Los componentes genéricos deben permanecer en `components/ui`.

## Roles y permisos

### ADMINISTRADOR

- Administrar usuarios y roles.
- Ver todos los proyectos.
- Validar o rechazar pagos.
- Consultar auditoría.
- Generar reportes generales.
- Administrar catálogos.

### ENCARGADO

- Registrar y actualizar clientes.
- Crear y gestionar proyectos.
- Revisar requerimientos.
- Elaborar y enviar cotizaciones.
- Asignar especialistas y tareas.
- Gestionar expedientes, propuestas y trámites.
- Registrar observaciones municipales.
- Cambiar estados permitidos del proyecto.

### ESPECIALISTA

- Consultar únicamente proyectos asignados.
- Actualizar sus tareas.
- Registrar resultados de estudios.
- Subir documentos técnicos.
- Crear propuestas cuando corresponda.

### CLIENTE

- Registrar y completar su solicitud.
- Consultar sus propios proyectos.
- Aceptar o rechazar cotizaciones.
- Registrar comprobantes de pago.
- Revisar propuestas.
- Dar conformidad o registrar observaciones.
- Consultar estado, documentos autorizados y notificaciones.

Aplica autorización tanto en rutas como en servicios. Ocultar un botón en el frontend no sustituye la seguridad del backend.

## Estados del proyecto

Utiliza exactamente estos estados, preferiblemente mediante un `enum` Java:

```text
SOLICITUD_REGISTRADA
DATOS_INCOMPLETOS
EN_COTIZACION
COTIZACION_ENVIADA
ESPERANDO_PAGO
PAGO_EN_REVISION
PROYECTO_ACTIVO
EN_ESTUDIOS
EN_DISENO
REVISION_CLIENTE
EXPEDIENTE_FINAL
EN_TRAMITE_PUBLICO
CON_OBSERVACIONES
APROBADO
NO_VIABLE
CERRADO
CANCELADO
```

No permitas saltos arbitrarios. Implementa una política central de transiciones válidas. Cada transición debe guardar:

- estado anterior;
- estado nuevo;
- usuario responsable;
- fecha y hora;
- comentario opcional.

## Reglas de negocio obligatorias

1. El código del proyecto debe generarse en formato `PROY-AAAA-NNNN`.
2. Una solicitud incompleta no puede pasar a cotización.
3. La cotización debe tener versión, vigencia y detalles.
4. El cliente dispone inicialmente de 10 días para responder una cotización.
5. Una cotización vencida puede renovarse generando una nueva versión; no se sobrescribe la anterior.
6. El proyecto no puede activarse hasta que el adelanto requerido sea validado.
7. El comprobante de pago se almacena en Cloudinary y su URL se registra en `pagos`.
8. Al validar el primer adelanto se crea automáticamente el expediente digital si todavía no existe.
9. Los especialistas solo pueden modificar tareas y elementos de proyectos asignados.
10. Cada propuesta debe conservar su número de versión.
11. Las dos primeras rondas de corrección del cliente se consideran incluidas.
12. Desde la tercera ronda, el encargado puede marcar el cambio como adicional y emitir otra cotización.
13. El expediente final debe estar completo antes de iniciar el trámite público.
14. Cuando el proyecto no requiere trámite público puede pasar de expediente final a aprobado/cerrado mediante la transición definida.
15. Una observación pública debe tener número, fecha, descripción, estado y plazo cuando exista.
16. Un trámite observado no puede aprobarse mientras tenga observaciones pendientes.
17. No elimines físicamente proyectos con historial; utiliza cancelación o estado inactivo.
18. Los archivos se eliminan de Cloudinary solamente mediante un caso de uso autorizado y consistente con la base de datos.
19. Todas las fechas del backend se guardan con zona horaria; la interfaz debe mostrarlas en `America/Lima`.
20. Los importes usan `BigDecimal` en Java, nunca `double`.

## Contrato general de la API

- Prefijo: `/api/v1`.
- Contenido: `application/json`, excepto subida/descarga de archivos.
- Fechas: ISO 8601.
- Paginación: `page`, `size`, `sort`.
- Búsquedas mediante query params.
- Listados grandes siempre paginados.
- Respuesta de error uniforme.

Formato sugerido de error:

```json
{
  "timestamp": "2026-10-20T11:30:00-05:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "La solicitud contiene datos inválidos",
  "path": "/api/v1/proyectos",
  "fieldErrors": {
    "nombre": "El nombre es obligatorio"
  }
}
```

## Endpoints mínimos

### Autenticación

```text
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
```

### Usuarios y empleados

```text
GET    /api/v1/usuarios
POST   /api/v1/usuarios
GET    /api/v1/usuarios/{id}
PATCH  /api/v1/usuarios/{id}
PATCH  /api/v1/usuarios/{id}/estado
GET    /api/v1/empleados
POST   /api/v1/empleados
PATCH  /api/v1/empleados/{id}
GET    /api/v1/especialidades
POST   /api/v1/especialidades
```

### Clientes

```text
GET    /api/v1/clientes
POST   /api/v1/clientes
GET    /api/v1/clientes/{id}
PATCH  /api/v1/clientes/{id}
GET    /api/v1/clientes/{id}/proyectos
```

### Proyectos y requerimientos

```text
GET    /api/v1/proyectos
POST   /api/v1/proyectos
GET    /api/v1/proyectos/{id}
PATCH  /api/v1/proyectos/{id}
POST   /api/v1/proyectos/{id}/transiciones
GET    /api/v1/proyectos/{id}/historial
GET    /api/v1/proyectos/{id}/requerimientos
POST   /api/v1/proyectos/{id}/requerimientos
PATCH  /api/v1/requerimientos/{id}
DELETE /api/v1/requerimientos/{id}
```

### Cotizaciones y pagos

```text
GET    /api/v1/proyectos/{id}/cotizaciones
POST   /api/v1/proyectos/{id}/cotizaciones
GET    /api/v1/cotizaciones/{id}
PATCH  /api/v1/cotizaciones/{id}
POST   /api/v1/cotizaciones/{id}/enviar
POST   /api/v1/cotizaciones/{id}/responder
POST   /api/v1/cotizaciones/{id}/renovar
GET    /api/v1/cotizaciones/{id}/pagos
POST   /api/v1/cotizaciones/{id}/pagos
POST   /api/v1/pagos/{id}/validar
POST   /api/v1/pagos/{id}/rechazar
```

### Expedientes y documentos

```text
GET    /api/v1/proyectos/{id}/expediente
POST   /api/v1/proyectos/{id}/expediente
GET    /api/v1/expedientes/{id}/documentos
POST   /api/v1/expedientes/{id}/documentos
GET    /api/v1/documentos/{id}
DELETE /api/v1/documentos/{id}
```

La carga debe utilizar `multipart/form-data`. Valida tamaño, MIME y extensión. Permite inicialmente PDF, JPG, JPEG y PNG. Define el límite mediante variable de entorno.

### Asignaciones, tareas y estudios

```text
GET    /api/v1/proyectos/{id}/asignaciones
POST   /api/v1/proyectos/{id}/asignaciones
PATCH  /api/v1/asignaciones/{id}
GET    /api/v1/asignaciones/{id}/tareas
POST   /api/v1/asignaciones/{id}/tareas
PATCH  /api/v1/tareas/{id}
GET    /api/v1/proyectos/{id}/estudios
POST   /api/v1/proyectos/{id}/estudios
PATCH  /api/v1/estudios/{id}
```

### Propuestas y observaciones del cliente

```text
GET    /api/v1/proyectos/{id}/propuestas
POST   /api/v1/proyectos/{id}/propuestas
GET    /api/v1/propuestas/{id}
POST   /api/v1/propuestas/{id}/enviar
POST   /api/v1/propuestas/{id}/respuesta
GET    /api/v1/propuestas/{id}/observaciones
POST   /api/v1/propuestas/{id}/observaciones
PATCH  /api/v1/observaciones-cliente/{id}
```

### Trámites públicos

```text
GET    /api/v1/proyectos/{id}/tramites
POST   /api/v1/proyectos/{id}/tramites
GET    /api/v1/tramites/{id}
PATCH  /api/v1/tramites/{id}
GET    /api/v1/tramites/{id}/observaciones
POST   /api/v1/tramites/{id}/observaciones
POST   /api/v1/observaciones-entidad/{id}/subsanar
POST   /api/v1/tramites/{id}/resolver
```

### Notificaciones y reportes

```text
GET    /api/v1/notificaciones
PATCH  /api/v1/notificaciones/{id}/leer
PATCH  /api/v1/notificaciones/leer-todas
GET    /api/v1/reportes/proyectos.xlsx
GET    /api/v1/reportes/pagos.xlsx
GET    /api/v1/reportes/tareas.xlsx
GET    /api/v1/dashboard/resumen
```

## Interfaz y experiencia de usuario

El diseño debe ser corporativo, limpio, sobrio y propio de un estudio de arquitectura.

### Dirección visual

- Fondo principal claro.
- Blanco, gris cálido, grafito y un color acento elegante.
- Tipografía legible y moderna.
- Espaciado consistente.
- Iconos simples; no usar emojis como iconografía del sistema.
- Evitar gradientes excesivos, efectos brillantes y componentes que parezcan generados automáticamente.
- Tablas claras, filtros visibles y formularios divididos por secciones.
- Estados identificables mediante etiquetas discretas y accesibles.
- Diseño responsive desde 360 px hasta escritorio.

### Pantallas mínimas

#### Públicas

- Inicio institucional.
- Servicios.
- Nosotros.
- Contacto.
- Iniciar sesión.
- Registrar solicitud.

#### Privadas comunes

- Dashboard.
- Perfil.
- Notificaciones.
- Detalle de proyecto con pestañas.

#### Administrador/encargado

- Gestión de usuarios.
- Clientes.
- Proyectos.
- Cotizaciones.
- Validación de pagos.
- Expedientes.
- Asignaciones y tareas.
- Trámites.
- Reportes.
- Auditoría.

#### Cliente

- Mis proyectos.
- Detalle y línea de tiempo.
- Cotizaciones pendientes.
- Registro de pago.
- Propuestas para revisión.
- Documentos autorizados.

#### Especialista

- Proyectos asignados.
- Mis tareas.
- Estudios técnicos.
- Carga de entregables.

El detalle del proyecto debe concentrar la información mediante pestañas:

```text
Resumen | Requerimientos | Cotizaciones | Pagos | Equipo y tareas |
Estudios | Propuestas | Expediente | Trámite público | Historial
```

## Base de datos

Utiliza las tablas ya definidas:

```text
roles, usuarios, clientes, empleados, proyectos, requerimientos,
cotizaciones, detalle_cotizacion, pagos, expedientes, documentos,
especialidades, asignaciones, tareas, estudios_tecnicos, propuestas,
observaciones_cliente, tramites_publicos, observaciones_entidad,
notificaciones, historial_estados, auditoria
```

Requisitos:

- PostgreSQL es la base productiva.
- Usa UUID para entidades principales.
- Usa `BigDecimal` para dinero.
- Evita relaciones JPA bidireccionales innecesarias.
- Evita `FetchType.EAGER` en colecciones.
- Evita el problema N+1 mediante consultas específicas o `EntityGraph`.
- Todas las migraciones deben ser versionadas con Flyway.
- No ejecutes `ddl-auto=create` en producción; usa `validate`.
- La versión SQL Server se conserva solo como entregable académico.

## Seguridad

- BCrypt para contraseñas.
- Access token JWT de corta duración.
- Refresh token revocable y almacenado de forma segura.
- CORS limitado a los dominios configurados.
- Límites de tamaño y tipo para archivos.
- Sanitización de nombres de archivo.
- Validación de propiedad: un cliente solo accede a sus proyectos.
- Prevención de asignación masiva mediante DTO específicos.
- No exponer trazas internas en producción.
- Secretos exclusivamente mediante variables de entorno.
- Auditoría de login, cambios de estado, pagos, documentos y exportaciones.
- No incluir credenciales reales en Git.

## Variables de entorno

### Backend `.env.example`

```dotenv
SPRING_PROFILES_ACTIVE=dev
PORT=8080
DATABASE_URL=jdbc:postgresql://HOST:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=change_me
JWT_SECRET=change_me_with_at_least_32_bytes
JWT_ACCESS_EXPIRATION_MINUTES=15
JWT_REFRESH_EXPIRATION_DAYS=7
CORS_ALLOWED_ORIGINS=http://localhost:5173
CLOUDINARY_CLOUD_NAME=change_me
CLOUDINARY_API_KEY=change_me
CLOUDINARY_API_SECRET=change_me
MAX_UPLOAD_MB=10
```

### Frontend `.env.example`

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=ATELIER Gestión Digital
```

No coloques la clave secreta de Cloudinary ni credenciales de base de datos en el frontend.

## Integración con Cloudinary

- Organiza recursos por carpetas:

```text
atelier/{ambiente}/proyectos/{codigoProyecto}/pagos/
atelier/{ambiente}/proyectos/{codigoProyecto}/propuestas/
atelier/{ambiente}/proyectos/{codigoProyecto}/expediente/
atelier/{ambiente}/proyectos/{codigoProyecto}/estudios/
```

- Guarda `secure_url` y `public_id`.
- Si la escritura en Cloudinary funciona y la transacción de base de datos falla, elimina el recurso subido mediante una compensación segura.
- Si se elimina un documento autorizado, elimina primero de Cloudinary y confirma el resultado antes de actualizar la base.
- Registra errores sin exponer secretos.

## Reportes con Apache POI

Implementa al menos tres reportes:

1. Proyectos por estado, cliente, encargado y fechas.
2. Pagos por periodo, método, estado y proyecto.
3. Tareas por especialista, prioridad, estado y vencimiento.

Cada archivo debe contener:

- título;
- fecha de generación;
- filtros utilizados;
- encabezados con estilo;
- fechas y monedas formateadas;
- filtros automáticos;
- columnas autoajustadas;
- nombre de archivo descriptivo.

## Manejo de errores y logs

- Implementa `@RestControllerAdvice`.
- Define excepciones de dominio: recurso no encontrado, conflicto, transición inválida, acceso denegado y archivo inválido.
- Usa un identificador de correlación por solicitud.
- Configura Logback para consola en desarrollo y formato apropiado en producción.
- Usa niveles `INFO`, `WARN` y `ERROR` correctamente.
- No utilices `System.out.println`.

## Pruebas obligatorias

### Backend

- Inicio de sesión válido e inválido.
- Autorización por cada rol.
- Creación de proyecto.
- Rechazo de proyecto con datos incompletos al cotizar.
- Cálculo de total de cotización.
- Vencimiento y renovación.
- Validación y rechazo de pago.
- Creación automática de expediente al validar adelanto.
- Transiciones permitidas y prohibidas.
- Dos rondas incluidas y tercera ronda adicional.
- Restricción de acceso del cliente.
- Carga de archivo válida e inválida.
- Trámite con observaciones pendientes.
- Generación de Excel.

### Frontend

- Renderizado de login y validaciones.
- Protección de rutas.
- Menú según rol.
- Formulario de proyecto.
- Respuesta de cotización.
- Registro de comprobante.
- Manejo visual de carga, vacío y error.
- Cambio de estado y refresco de datos.

## Calidad de código

- Código y nombres técnicos en español coherente o en inglés coherente; no mezclar arbitrariamente.
- Clases pequeñas con responsabilidad clara.
- Evitar duplicación.
- Evitar valores mágicos.
- DTO inmutables cuando sea conveniente.
- Transacciones solamente en la capa de servicio.
- Comentarios para decisiones, no para repetir el código.
- README con instrucciones exactas de instalación y ejecución.
- Compilación sin errores ni advertencias relevantes.
- No dejar endpoints falsos, botones sin acción, `TODO` críticos ni datos codificados en componentes.

## Estrategia de Git

- Rama principal: `main`.
- Rama de integración: `develop`.
- Ramas de trabajo: `feature/nombre-corto`, `fix/nombre-corto`.
- Commits pequeños y descriptivos.
- Nunca versionar `.env`, claves, carpetas de compilación o dependencias instaladas.

## Fases de implementación

No intentes construir todo en un solo cambio. Trabaja por fases y detente al finalizar cada una para entregar un resumen verificable.

### Fase 0 — Inspección y planificación

- Revisar el repositorio y los archivos de referencia.
- Detectar trabajo existente y preservarlo.
- Confirmar versiones disponibles.
- Crear plan técnico y lista de riesgos.
- No modificar código todavía si existe una incompatibilidad importante.

### Fase 1 — Fundaciones

- Crear monorepositorio y configuraciones.
- Inicializar backend y frontend.
- Configurar perfiles, variables y CORS.
- Integrar Flyway y migración inicial.
- Configurar manejo global de errores y logging.
- Implementar autenticación y roles.

### Fase 2 — Clientes y proyectos

- Usuarios, clientes y empleados.
- Proyectos y requerimientos.
- Estados y transiciones.
- Dashboard inicial.
- Pruebas del módulo.

### Fase 3 — Cotizaciones y pagos

- Cotizaciones y detalles.
- Respuesta del cliente y vigencia.
- Registro de comprobante.
- Validación de pago.
- Creación del expediente.

### Fase 4 — Ejecución técnica

- Especialidades.
- Asignaciones.
- Tareas.
- Estudios técnicos.
- Documentos en Cloudinary.

### Fase 5 — Propuestas y revisiones

- Versiones de propuestas.
- Conformidad u observaciones.
- Control de rondas.
- Cotización adicional.

### Fase 6 — Trámite público y cierre

- Expediente final.
- Trámites.
- Observaciones y subsanaciones.
- Aprobación, no viabilidad y cierre.

### Fase 7 — Reportes y endurecimiento

- Excel con Apache POI.
- Auditoría.
- Pruebas integrales.
- Accesibilidad y responsive.
- Documentación de despliegue.
- Configuración Netlify/Render.

## Forma de trabajo obligatoria para Codex

En cada fase:

1. Inspecciona primero los archivos relevantes.
2. Explica brevemente qué vas a modificar.
3. Implementa cambios completos y coherentes.
4. Ejecuta formato, compilación y pruebas.
5. Corrige fallos causados por tus cambios.
6. Muestra un resumen de archivos creados/modificados.
7. Indica comandos ejecutados y resultados relevantes.
8. Señala riesgos o decisiones pendientes.
9. Detente antes de iniciar la siguiente fase.

No borres ni reemplaces trabajo existente sin inspeccionarlo. No uses comandos destructivos. No inventes resultados de pruebas. Si una herramienta no está disponible, dilo con claridad y realiza la validación alternativa posible.

## Criterios de aceptación generales

El proyecto se considera aceptable cuando:

- Backend y frontend arrancan localmente.
- La API utiliza PostgreSQL/Supabase sin datos codificados.
- Los cuatro roles tienen permisos diferenciados.
- Se puede demostrar el flujo desde solicitud hasta cierre.
- Los documentos se cargan a Cloudinary.
- Los pagos y respuestas municipales se simulan de manera controlada.
- El historial registra los cambios de estado.
- Los reportes Excel se descargan correctamente.
- Swagger documenta la API.
- Las pruebas críticas pasan.
- El frontend es responsive y mantiene un estilo corporativo consistente.
- No existen secretos dentro del repositorio.
- Existe documentación para ejecutar y desplegar.

## Primera instrucción de ejecución

Comienza únicamente con la **Fase 0 — Inspección y planificación**.

Revisa la estructura disponible, el BPMN y los scripts de base de datos. Luego entrega:

1. diagnóstico del repositorio;
2. arquitectura propuesta final;
3. dependencias exactas recomendadas;
4. estructura de carpetas que crearás;
5. plan de implementación por fases;
6. riesgos técnicos;
7. preguntas realmente bloqueantes, si existen.

No programes todavía en esta primera respuesta. Espera aprobación para comenzar la Fase 1.

