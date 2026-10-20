-- ATELIER - Modelo fisico para Supabase/PostgreSQL
-- Ejecutar en SQL Editor con un usuario propietario del esquema public.

create extension if not exists pgcrypto;

create table if not exists roles (
    id_rol smallserial primary key,
    nombre varchar(40) not null unique,
    descripcion varchar(200),
    creado_en timestamptz not null default now()
);

create table if not exists usuarios (
    id_usuario uuid primary key default gen_random_uuid(),
    id_rol smallint not null references roles(id_rol),
    correo varchar(150) not null unique,
    clave_hash varchar(255) not null,
    nombres varchar(100) not null,
    apellidos varchar(120) not null,
    telefono varchar(20),
    activo boolean not null default true,
    ultimo_acceso timestamptz,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now()
);

create table if not exists clientes (
    id_cliente uuid primary key default gen_random_uuid(),
    id_usuario uuid unique references usuarios(id_usuario),
    tipo_persona varchar(15) not null default 'NATURAL'
        check (tipo_persona in ('NATURAL','JURIDICA')),
    tipo_documento varchar(10) not null check (tipo_documento in ('DNI','CE','RUC','PASAPORTE')),
    numero_documento varchar(20) not null unique,
    razon_social varchar(180),
    direccion varchar(250),
    distrito varchar(100),
    creado_en timestamptz not null default now()
);

create table if not exists empleados (
    id_empleado uuid primary key default gen_random_uuid(),
    id_usuario uuid not null unique references usuarios(id_usuario),
    codigo_colegiatura varchar(40),
    cargo varchar(80) not null,
    tipo varchar(15) not null default 'INTERNO' check (tipo in ('INTERNO','EXTERNO')),
    disponible boolean not null default true,
    creado_en timestamptz not null default now()
);

create table if not exists proyectos (
    id_proyecto uuid primary key default gen_random_uuid(),
    codigo varchar(20) not null unique,
    id_cliente uuid not null references clientes(id_cliente),
    id_encargado uuid references empleados(id_empleado),
    nombre varchar(180) not null,
    descripcion text,
    tipo_proyecto varchar(60) not null,
    ubicacion varchar(250),
    area_terreno numeric(12,2) check (area_terreno is null or area_terreno > 0),
    estado varchar(35) not null default 'SOLICITUD_REGISTRADA'
        check (estado in ('SOLICITUD_REGISTRADA','DATOS_INCOMPLETOS','EN_COTIZACION',
        'COTIZACION_ENVIADA','ESPERANDO_PAGO','PAGO_EN_REVISION','PROYECTO_ACTIVO',
        'EN_ESTUDIOS','EN_DISENO','REVISION_CLIENTE','EXPEDIENTE_FINAL','EN_TRAMITE_PUBLICO',
        'CON_OBSERVACIONES','APROBADO','NO_VIABLE','CERRADO','CANCELADO')),
    requiere_tramite_publico boolean not null default false,
    fecha_solicitud date not null default current_date,
    fecha_inicio date,
    fecha_fin_estimada date,
    fecha_cierre date,
    creado_en timestamptz not null default now(),
    actualizado_en timestamptz not null default now()
);

create table if not exists requerimientos (
    id_requerimiento uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    categoria varchar(60) not null,
    descripcion text not null,
    prioridad varchar(10) not null default 'MEDIA' check (prioridad in ('BAJA','MEDIA','ALTA')),
    obligatorio boolean not null default true,
    creado_en timestamptz not null default now()
);

create table if not exists cotizaciones (
    id_cotizacion uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto),
    creada_por uuid not null references empleados(id_empleado),
    numero varchar(25) not null unique,
    version smallint not null default 1 check (version > 0),
    tipo varchar(15) not null default 'INICIAL' check (tipo in ('INICIAL','ADICIONAL')),
    subtotal numeric(12,2) not null check (subtotal >= 0),
    impuesto numeric(12,2) not null default 0 check (impuesto >= 0),
    total numeric(12,2) generated always as (subtotal + impuesto) stored,
    porcentaje_adelanto numeric(5,2) not null default 50 check (porcentaje_adelanto between 0 and 100),
    estado varchar(15) not null default 'BORRADOR'
        check (estado in ('BORRADOR','ENVIADA','ACEPTADA','RECHAZADA','VENCIDA','ANULADA')),
    emitida_en timestamptz,
    vence_en timestamptz,
    respondida_en timestamptz,
    observacion text,
    creado_en timestamptz not null default now(),
    unique (id_proyecto, version, tipo)
);

create table if not exists detalle_cotizacion (
    id_detalle uuid primary key default gen_random_uuid(),
    id_cotizacion uuid not null references cotizaciones(id_cotizacion) on delete cascade,
    concepto varchar(180) not null,
    descripcion varchar(500),
    cantidad numeric(10,2) not null default 1 check (cantidad > 0),
    precio_unitario numeric(12,2) not null check (precio_unitario >= 0),
    importe numeric(12,2) generated always as (cantidad * precio_unitario) stored
);

create table if not exists pagos (
    id_pago uuid primary key default gen_random_uuid(),
    id_cotizacion uuid not null references cotizaciones(id_cotizacion),
    validado_por uuid references empleados(id_empleado),
    tipo varchar(15) not null check (tipo in ('ADELANTO','SALDO','ADICIONAL')),
    metodo varchar(20) not null check (metodo in ('TRANSFERENCIA','DEPOSITO','TARJETA','EFECTIVO','OTRO')),
    monto numeric(12,2) not null check (monto > 0),
    moneda char(3) not null default 'PEN',
    numero_operacion varchar(80),
    comprobante_url varchar(500),
    comprobante_public_id varchar(250),
    estado varchar(15) not null default 'PENDIENTE'
        check (estado in ('PENDIENTE','VALIDADO','RECHAZADO')),
    motivo_rechazo varchar(500),
    pagado_en timestamptz not null default now(),
    validado_en timestamptz,
    creado_en timestamptz not null default now()
);

create table if not exists expedientes (
    id_expediente uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null unique references proyectos(id_proyecto) on delete cascade,
    codigo varchar(30) not null unique,
    estado varchar(20) not null default 'ABIERTO'
        check (estado in ('ABIERTO','EN_REVISION','COMPLETO','ARCHIVADO')),
    creado_en timestamptz not null default now(),
    cerrado_en timestamptz
);

create table if not exists documentos (
    id_documento uuid primary key default gen_random_uuid(),
    id_expediente uuid not null references expedientes(id_expediente) on delete cascade,
    subido_por uuid not null references usuarios(id_usuario),
    nombre varchar(200) not null,
    tipo_documento varchar(50) not null,
    version smallint not null default 1 check (version > 0),
    url_cloudinary varchar(600) not null,
    public_id_cloudinary varchar(300) not null,
    formato varchar(20),
    tamano_bytes bigint check (tamano_bytes is null or tamano_bytes >= 0),
    vigente boolean not null default true,
    creado_en timestamptz not null default now(),
    unique (id_expediente, public_id_cloudinary)
);

create table if not exists especialidades (
    id_especialidad smallserial primary key,
    nombre varchar(80) not null unique,
    descripcion varchar(250),
    activa boolean not null default true
);

create table if not exists asignaciones (
    id_asignacion uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    id_empleado uuid not null references empleados(id_empleado),
    id_especialidad smallint not null references especialidades(id_especialidad),
    rol_proyecto varchar(80) not null,
    fecha_asignacion date not null default current_date,
    fecha_fin date,
    estado varchar(15) not null default 'ACTIVA' check (estado in ('ACTIVA','FINALIZADA','CANCELADA')),
    unique (id_proyecto, id_empleado, id_especialidad)
);

create table if not exists tareas (
    id_tarea uuid primary key default gen_random_uuid(),
    id_asignacion uuid not null references asignaciones(id_asignacion) on delete cascade,
    titulo varchar(180) not null,
    descripcion text,
    prioridad varchar(10) not null default 'MEDIA' check (prioridad in ('BAJA','MEDIA','ALTA','CRITICA')),
    estado varchar(15) not null default 'PENDIENTE'
        check (estado in ('PENDIENTE','EN_PROCESO','BLOQUEADA','COMPLETADA','CANCELADA')),
    fecha_inicio date,
    fecha_limite date,
    completada_en timestamptz,
    creado_en timestamptz not null default now(),
    check (fecha_limite is null or fecha_inicio is null or fecha_limite >= fecha_inicio)
);

create table if not exists estudios_tecnicos (
    id_estudio uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    responsable uuid references empleados(id_empleado),
    tipo varchar(80) not null,
    proveedor varchar(180),
    requerido boolean not null default true,
    estado varchar(15) not null default 'PENDIENTE'
        check (estado in ('PENDIENTE','EN_PROCESO','COMPLETADO','NO_APLICA')),
    fecha_solicitud date,
    fecha_resultado date,
    resultado text,
    costo numeric(12,2) check (costo is null or costo >= 0)
);

create table if not exists propuestas (
    id_propuesta uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    creada_por uuid not null references empleados(id_empleado),
    numero_version smallint not null check (numero_version > 0),
    tipo varchar(20) not null check (tipo in ('ANTEPROYECTO','PROYECTO_FINAL')),
    descripcion text,
    url_vista_previa varchar(600),
    estado varchar(20) not null default 'BORRADOR'
        check (estado in ('BORRADOR','ENVIADA','OBSERVADA','CONFORME','REEMPLAZADA')),
    enviada_en timestamptz,
    respondida_en timestamptz,
    creado_en timestamptz not null default now(),
    unique (id_proyecto, tipo, numero_version)
);

create table if not exists observaciones_cliente (
    id_observacion_cliente uuid primary key default gen_random_uuid(),
    id_propuesta uuid not null references propuestas(id_propuesta) on delete cascade,
    id_cliente uuid not null references clientes(id_cliente),
    numero_ronda smallint not null check (numero_ronda > 0),
    descripcion text not null,
    estado varchar(15) not null default 'PENDIENTE'
        check (estado in ('PENDIENTE','ATENDIDA','RECHAZADA')),
    es_adicional boolean not null default false,
    creada_en timestamptz not null default now(),
    atendida_en timestamptz
);

create table if not exists tramites_publicos (
    id_tramite uuid primary key default gen_random_uuid(),
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    responsable uuid references empleados(id_empleado),
    entidad varchar(180) not null,
    tipo_tramite varchar(150) not null,
    numero_expediente_externo varchar(80),
    estado varchar(25) not null default 'PREPARACION'
        check (estado in ('PREPARACION','PRESENTADO','EN_REVISION','OBSERVADO','APROBADO','RECHAZADO','ARCHIVADO')),
    fecha_presentacion date,
    fecha_respuesta date,
    observacion_general text,
    creado_en timestamptz not null default now()
);

create table if not exists observaciones_entidad (
    id_observacion_entidad uuid primary key default gen_random_uuid(),
    id_tramite uuid not null references tramites_publicos(id_tramite) on delete cascade,
    numero smallint not null check (numero > 0),
    descripcion text not null,
    fecha_notificacion date not null,
    fecha_limite date,
    estado varchar(15) not null default 'PENDIENTE'
        check (estado in ('PENDIENTE','SUBSANADA','ACEPTADA','NO_SUBSANABLE')),
    respuesta text,
    fecha_subsanacion date,
    unique (id_tramite, numero)
);

create table if not exists notificaciones (
    id_notificacion uuid primary key default gen_random_uuid(),
    id_usuario uuid not null references usuarios(id_usuario) on delete cascade,
    id_proyecto uuid references proyectos(id_proyecto) on delete cascade,
    titulo varchar(160) not null,
    mensaje varchar(600) not null,
    tipo varchar(20) not null default 'INFORMATIVA'
        check (tipo in ('INFORMATIVA','ACCION','ALERTA','VENCIMIENTO')),
    leida boolean not null default false,
    creada_en timestamptz not null default now(),
    leida_en timestamptz
);

create table if not exists historial_estados (
    id_historial bigserial primary key,
    id_proyecto uuid not null references proyectos(id_proyecto) on delete cascade,
    cambiado_por uuid references usuarios(id_usuario),
    estado_anterior varchar(35),
    estado_nuevo varchar(35) not null,
    comentario varchar(500),
    cambiado_en timestamptz not null default now()
);

create table if not exists auditoria (
    id_auditoria bigserial primary key,
    id_usuario uuid references usuarios(id_usuario),
    tabla_afectada varchar(80) not null,
    id_registro varchar(80),
    accion varchar(15) not null check (accion in ('INSERT','UPDATE','DELETE','LOGIN','LOGOUT','EXPORT')),
    detalle jsonb,
    ip_origen inet,
    creado_en timestamptz not null default now()
);

create index if not exists idx_proyectos_cliente on proyectos(id_cliente);
create index if not exists idx_proyectos_encargado on proyectos(id_encargado);
create index if not exists idx_proyectos_estado on proyectos(estado);
create index if not exists idx_cotizaciones_proyecto on cotizaciones(id_proyecto);
create index if not exists idx_pagos_cotizacion on pagos(id_cotizacion);
create index if not exists idx_documentos_expediente on documentos(id_expediente);
create index if not exists idx_asignaciones_proyecto on asignaciones(id_proyecto);
create index if not exists idx_tareas_asignacion_estado on tareas(id_asignacion, estado);
create index if not exists idx_propuestas_proyecto on propuestas(id_proyecto);
create index if not exists idx_tramites_proyecto on tramites_publicos(id_proyecto);
create index if not exists idx_notificaciones_usuario on notificaciones(id_usuario, leida);
create index if not exists idx_historial_proyecto on historial_estados(id_proyecto, cambiado_en desc);

create or replace function actualizar_fecha_modificacion()
returns trigger language plpgsql as $$
begin
    new.actualizado_en = now();
    return new;
end;
$$;

drop trigger if exists trg_usuarios_actualizado on usuarios;
create trigger trg_usuarios_actualizado before update on usuarios
for each row execute function actualizar_fecha_modificacion();

drop trigger if exists trg_proyectos_actualizado on proyectos;
create trigger trg_proyectos_actualizado before update on proyectos
for each row execute function actualizar_fecha_modificacion();

-- Seguridad base. El backend Java debe conectarse con un usuario de servicio.
alter table usuarios enable row level security;
alter table clientes enable row level security;
alter table proyectos enable row level security;
alter table documentos enable row level security;

comment on table proyectos is 'Entidad central del flujo TO-BE de ATELIER';
comment on column documentos.url_cloudinary is 'URL segura o publica devuelta por Cloudinary';
comment on column documentos.public_id_cloudinary is 'Identificador usado para administrar el recurso en Cloudinary';

