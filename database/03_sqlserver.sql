/* ATELIER - Modelo fisico academico para Microsoft SQL Server 2019+ */

IF DB_ID('AtelierDB') IS NULL CREATE DATABASE AtelierDB;
GO
USE AtelierDB;
GO

CREATE TABLE roles (
    id_rol SMALLINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE usuarios (
    id_usuario UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_rol SMALLINT NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    clave_hash VARCHAR(255) NOT NULL,
    nombres NVARCHAR(100) NOT NULL,
    apellidos NVARCHAR(120) NOT NULL,
    telefono VARCHAR(20) NULL,
    activo BIT NOT NULL DEFAULT 1,
    ultimo_acceso DATETIME2 NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    actualizado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_usuarios_roles FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

CREATE TABLE clientes (
    id_cliente UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_usuario UNIQUEIDENTIFIER NULL UNIQUE,
    tipo_persona VARCHAR(15) NOT NULL DEFAULT 'NATURAL',
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    razon_social NVARCHAR(180) NULL,
    direccion NVARCHAR(250) NULL,
    distrito NVARCHAR(100) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_clientes_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT CK_clientes_tipo_persona CHECK (tipo_persona IN ('NATURAL','JURIDICA')),
    CONSTRAINT CK_clientes_documento CHECK (tipo_documento IN ('DNI','CE','RUC','PASAPORTE'))
);

CREATE TABLE empleados (
    id_empleado UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_usuario UNIQUEIDENTIFIER NOT NULL UNIQUE,
    codigo_colegiatura VARCHAR(40) NULL,
    cargo NVARCHAR(80) NOT NULL,
    tipo VARCHAR(15) NOT NULL DEFAULT 'INTERNO',
    disponible BIT NOT NULL DEFAULT 1,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_empleados_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT CK_empleados_tipo CHECK (tipo IN ('INTERNO','EXTERNO'))
);

CREATE TABLE proyectos (
    id_proyecto UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    id_cliente UNIQUEIDENTIFIER NOT NULL,
    id_encargado UNIQUEIDENTIFIER NULL,
    nombre NVARCHAR(180) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    tipo_proyecto NVARCHAR(60) NOT NULL,
    ubicacion NVARCHAR(250) NULL,
    area_terreno DECIMAL(12,2) NULL,
    estado VARCHAR(35) NOT NULL DEFAULT 'SOLICITUD_REGISTRADA',
    requiere_tramite_publico BIT NOT NULL DEFAULT 0,
    fecha_solicitud DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    fecha_inicio DATE NULL,
    fecha_fin_estimada DATE NULL,
    fecha_cierre DATE NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    actualizado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_proyectos_clientes FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT FK_proyectos_encargado FOREIGN KEY (id_encargado) REFERENCES empleados(id_empleado),
    CONSTRAINT CK_proyectos_area CHECK (area_terreno IS NULL OR area_terreno > 0),
    CONSTRAINT CK_proyectos_estado CHECK (estado IN
      ('SOLICITUD_REGISTRADA','DATOS_INCOMPLETOS','EN_COTIZACION','COTIZACION_ENVIADA',
       'ESPERANDO_PAGO','PAGO_EN_REVISION','PROYECTO_ACTIVO','EN_ESTUDIOS','EN_DISENO',
       'REVISION_CLIENTE','EXPEDIENTE_FINAL','EN_TRAMITE_PUBLICO','CON_OBSERVACIONES',
       'APROBADO','NO_VIABLE','CERRADO','CANCELADO'))
);

CREATE TABLE requerimientos (
    id_requerimiento UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    categoria NVARCHAR(60) NOT NULL,
    descripcion NVARCHAR(MAX) NOT NULL,
    prioridad VARCHAR(10) NOT NULL DEFAULT 'MEDIA',
    obligatorio BIT NOT NULL DEFAULT 1,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_requerimientos_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT CK_requerimientos_prioridad CHECK (prioridad IN ('BAJA','MEDIA','ALTA'))
);

CREATE TABLE cotizaciones (
    id_cotizacion UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    creada_por UNIQUEIDENTIFIER NOT NULL,
    numero VARCHAR(25) NOT NULL UNIQUE,
    version SMALLINT NOT NULL DEFAULT 1,
    tipo VARCHAR(15) NOT NULL DEFAULT 'INICIAL',
    subtotal DECIMAL(12,2) NOT NULL,
    impuesto DECIMAL(12,2) NOT NULL DEFAULT 0,
    total AS (subtotal + impuesto) PERSISTED,
    porcentaje_adelanto DECIMAL(5,2) NOT NULL DEFAULT 50,
    estado VARCHAR(15) NOT NULL DEFAULT 'BORRADOR',
    emitida_en DATETIME2 NULL,
    vence_en DATETIME2 NULL,
    respondida_en DATETIME2 NULL,
    observacion NVARCHAR(MAX) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_cotizaciones_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto),
    CONSTRAINT FK_cotizaciones_empleados FOREIGN KEY (creada_por) REFERENCES empleados(id_empleado),
    CONSTRAINT UQ_cotizaciones_version UNIQUE (id_proyecto, version, tipo),
    CONSTRAINT CK_cotizaciones_valores CHECK (version > 0 AND subtotal >= 0 AND impuesto >= 0 AND porcentaje_adelanto BETWEEN 0 AND 100),
    CONSTRAINT CK_cotizaciones_tipo CHECK (tipo IN ('INICIAL','ADICIONAL')),
    CONSTRAINT CK_cotizaciones_estado CHECK (estado IN ('BORRADOR','ENVIADA','ACEPTADA','RECHAZADA','VENCIDA','ANULADA'))
);

CREATE TABLE detalle_cotizacion (
    id_detalle UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_cotizacion UNIQUEIDENTIFIER NOT NULL,
    concepto NVARCHAR(180) NOT NULL,
    descripcion NVARCHAR(500) NULL,
    cantidad DECIMAL(10,2) NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(12,2) NOT NULL,
    importe AS (cantidad * precio_unitario) PERSISTED,
    CONSTRAINT FK_detalle_cotizacion FOREIGN KEY (id_cotizacion) REFERENCES cotizaciones(id_cotizacion) ON DELETE CASCADE,
    CONSTRAINT CK_detalle_valores CHECK (cantidad > 0 AND precio_unitario >= 0)
);

CREATE TABLE pagos (
    id_pago UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_cotizacion UNIQUEIDENTIFIER NOT NULL,
    validado_por UNIQUEIDENTIFIER NULL,
    tipo VARCHAR(15) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'PEN',
    numero_operacion VARCHAR(80) NULL,
    comprobante_url VARCHAR(500) NULL,
    comprobante_public_id VARCHAR(250) NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo NVARCHAR(500) NULL,
    pagado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    validado_en DATETIME2 NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_pagos_cotizaciones FOREIGN KEY (id_cotizacion) REFERENCES cotizaciones(id_cotizacion),
    CONSTRAINT FK_pagos_empleados FOREIGN KEY (validado_por) REFERENCES empleados(id_empleado),
    CONSTRAINT CK_pagos_tipo CHECK (tipo IN ('ADELANTO','SALDO','ADICIONAL')),
    CONSTRAINT CK_pagos_metodo CHECK (metodo IN ('TRANSFERENCIA','DEPOSITO','TARJETA','EFECTIVO','OTRO')),
    CONSTRAINT CK_pagos_estado CHECK (estado IN ('PENDIENTE','VALIDADO','RECHAZADO')),
    CONSTRAINT CK_pagos_monto CHECK (monto > 0)
);

CREATE TABLE expedientes (
    id_expediente UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL UNIQUE,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    cerrado_en DATETIME2 NULL,
    CONSTRAINT FK_expedientes_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT CK_expedientes_estado CHECK (estado IN ('ABIERTO','EN_REVISION','COMPLETO','ARCHIVADO'))
);

CREATE TABLE documentos (
    id_documento UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_expediente UNIQUEIDENTIFIER NOT NULL,
    subido_por UNIQUEIDENTIFIER NOT NULL,
    nombre NVARCHAR(200) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    version SMALLINT NOT NULL DEFAULT 1,
    url_cloudinary VARCHAR(600) NOT NULL,
    public_id_cloudinary VARCHAR(300) NOT NULL,
    formato VARCHAR(20) NULL,
    tamano_bytes BIGINT NULL,
    vigente BIT NOT NULL DEFAULT 1,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_documentos_expedientes FOREIGN KEY (id_expediente) REFERENCES expedientes(id_expediente) ON DELETE CASCADE,
    CONSTRAINT FK_documentos_usuarios FOREIGN KEY (subido_por) REFERENCES usuarios(id_usuario),
    CONSTRAINT UQ_documentos_cloudinary UNIQUE (id_expediente, public_id_cloudinary),
    CONSTRAINT CK_documentos_valores CHECK (version > 0 AND (tamano_bytes IS NULL OR tamano_bytes >= 0))
);

CREATE TABLE especialidades (
    id_especialidad SMALLINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(80) NOT NULL UNIQUE,
    descripcion NVARCHAR(250) NULL,
    activa BIT NOT NULL DEFAULT 1
);

CREATE TABLE asignaciones (
    id_asignacion UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    id_empleado UNIQUEIDENTIFIER NOT NULL,
    id_especialidad SMALLINT NOT NULL,
    rol_proyecto NVARCHAR(80) NOT NULL,
    fecha_asignacion DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    fecha_fin DATE NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT FK_asignaciones_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT FK_asignaciones_empleados FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    CONSTRAINT FK_asignaciones_especialidades FOREIGN KEY (id_especialidad) REFERENCES especialidades(id_especialidad),
    CONSTRAINT UQ_asignaciones UNIQUE (id_proyecto, id_empleado, id_especialidad),
    CONSTRAINT CK_asignaciones_estado CHECK (estado IN ('ACTIVA','FINALIZADA','CANCELADA'))
);

CREATE TABLE tareas (
    id_tarea UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_asignacion UNIQUEIDENTIFIER NOT NULL,
    titulo NVARCHAR(180) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    prioridad VARCHAR(10) NOT NULL DEFAULT 'MEDIA',
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_inicio DATE NULL,
    fecha_limite DATE NULL,
    completada_en DATETIME2 NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_tareas_asignaciones FOREIGN KEY (id_asignacion) REFERENCES asignaciones(id_asignacion) ON DELETE CASCADE,
    CONSTRAINT CK_tareas_prioridad CHECK (prioridad IN ('BAJA','MEDIA','ALTA','CRITICA')),
    CONSTRAINT CK_tareas_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','BLOQUEADA','COMPLETADA','CANCELADA')),
    CONSTRAINT CK_tareas_fechas CHECK (fecha_limite IS NULL OR fecha_inicio IS NULL OR fecha_limite >= fecha_inicio)
);

CREATE TABLE estudios_tecnicos (
    id_estudio UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    responsable UNIQUEIDENTIFIER NULL,
    tipo NVARCHAR(80) NOT NULL,
    proveedor NVARCHAR(180) NULL,
    requerido BIT NOT NULL DEFAULT 1,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud DATE NULL,
    fecha_resultado DATE NULL,
    resultado NVARCHAR(MAX) NULL,
    costo DECIMAL(12,2) NULL,
    CONSTRAINT FK_estudios_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT FK_estudios_empleados FOREIGN KEY (responsable) REFERENCES empleados(id_empleado),
    CONSTRAINT CK_estudios_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','COMPLETADO','NO_APLICA')),
    CONSTRAINT CK_estudios_costo CHECK (costo IS NULL OR costo >= 0)
);

CREATE TABLE propuestas (
    id_propuesta UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    creada_por UNIQUEIDENTIFIER NOT NULL,
    numero_version SMALLINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    url_vista_previa VARCHAR(600) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    enviada_en DATETIME2 NULL,
    respondida_en DATETIME2 NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_propuestas_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT FK_propuestas_empleados FOREIGN KEY (creada_por) REFERENCES empleados(id_empleado),
    CONSTRAINT UQ_propuestas_version UNIQUE (id_proyecto, tipo, numero_version),
    CONSTRAINT CK_propuestas_tipo CHECK (tipo IN ('ANTEPROYECTO','PROYECTO_FINAL')),
    CONSTRAINT CK_propuestas_estado CHECK (estado IN ('BORRADOR','ENVIADA','OBSERVADA','CONFORME','REEMPLAZADA')),
    CONSTRAINT CK_propuestas_version CHECK (numero_version > 0)
);

CREATE TABLE observaciones_cliente (
    id_observacion_cliente UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_propuesta UNIQUEIDENTIFIER NOT NULL,
    id_cliente UNIQUEIDENTIFIER NOT NULL,
    numero_ronda SMALLINT NOT NULL,
    descripcion NVARCHAR(MAX) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    es_adicional BIT NOT NULL DEFAULT 0,
    creada_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    atendida_en DATETIME2 NULL,
    CONSTRAINT FK_obs_cliente_propuestas FOREIGN KEY (id_propuesta) REFERENCES propuestas(id_propuesta) ON DELETE CASCADE,
    CONSTRAINT FK_obs_cliente_clientes FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    CONSTRAINT CK_obs_cliente_estado CHECK (estado IN ('PENDIENTE','ATENDIDA','RECHAZADA')),
    CONSTRAINT CK_obs_cliente_ronda CHECK (numero_ronda > 0)
);

CREATE TABLE tramites_publicos (
    id_tramite UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    responsable UNIQUEIDENTIFIER NULL,
    entidad NVARCHAR(180) NOT NULL,
    tipo_tramite NVARCHAR(150) NOT NULL,
    numero_expediente_externo VARCHAR(80) NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'PREPARACION',
    fecha_presentacion DATE NULL,
    fecha_respuesta DATE NULL,
    observacion_general NVARCHAR(MAX) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_tramites_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT FK_tramites_empleados FOREIGN KEY (responsable) REFERENCES empleados(id_empleado),
    CONSTRAINT CK_tramites_estado CHECK (estado IN ('PREPARACION','PRESENTADO','EN_REVISION','OBSERVADO','APROBADO','RECHAZADO','ARCHIVADO'))
);

CREATE TABLE observaciones_entidad (
    id_observacion_entidad UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_tramite UNIQUEIDENTIFIER NOT NULL,
    numero SMALLINT NOT NULL,
    descripcion NVARCHAR(MAX) NOT NULL,
    fecha_notificacion DATE NOT NULL,
    fecha_limite DATE NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    respuesta NVARCHAR(MAX) NULL,
    fecha_subsanacion DATE NULL,
    CONSTRAINT FK_obs_entidad_tramites FOREIGN KEY (id_tramite) REFERENCES tramites_publicos(id_tramite) ON DELETE CASCADE,
    CONSTRAINT UQ_obs_entidad_numero UNIQUE (id_tramite, numero),
    CONSTRAINT CK_obs_entidad_estado CHECK (estado IN ('PENDIENTE','SUBSANADA','ACEPTADA','NO_SUBSANABLE')),
    CONSTRAINT CK_obs_entidad_numero CHECK (numero > 0)
);

CREATE TABLE notificaciones (
    id_notificacion UNIQUEIDENTIFIER NOT NULL DEFAULT NEWSEQUENTIALID() PRIMARY KEY,
    id_usuario UNIQUEIDENTIFIER NOT NULL,
    id_proyecto UNIQUEIDENTIFIER NULL,
    titulo NVARCHAR(160) NOT NULL,
    mensaje NVARCHAR(600) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'INFORMATIVA',
    leida BIT NOT NULL DEFAULT 0,
    creada_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    leida_en DATETIME2 NULL,
    CONSTRAINT FK_notificaciones_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT FK_notificaciones_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto),
    CONSTRAINT CK_notificaciones_tipo CHECK (tipo IN ('INFORMATIVA','ACCION','ALERTA','VENCIMIENTO'))
);

CREATE TABLE historial_estados (
    id_historial BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_proyecto UNIQUEIDENTIFIER NOT NULL,
    cambiado_por UNIQUEIDENTIFIER NULL,
    estado_anterior VARCHAR(35) NULL,
    estado_nuevo VARCHAR(35) NOT NULL,
    comentario NVARCHAR(500) NULL,
    cambiado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_historial_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
    CONSTRAINT FK_historial_usuarios FOREIGN KEY (cambiado_por) REFERENCES usuarios(id_usuario)
);

CREATE TABLE auditoria (
    id_auditoria BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_usuario UNIQUEIDENTIFIER NULL,
    tabla_afectada VARCHAR(80) NOT NULL,
    id_registro VARCHAR(80) NULL,
    accion VARCHAR(15) NOT NULL,
    detalle NVARCHAR(MAX) NULL,
    ip_origen VARCHAR(45) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_auditoria_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT CK_auditoria_accion CHECK (accion IN ('INSERT','UPDATE','DELETE','LOGIN','LOGOUT','EXPORT')),
    CONSTRAINT CK_auditoria_json CHECK (detalle IS NULL OR ISJSON(detalle) = 1)
);

CREATE INDEX IX_proyectos_cliente ON proyectos(id_cliente);
CREATE INDEX IX_proyectos_estado ON proyectos(estado);
CREATE INDEX IX_cotizaciones_proyecto ON cotizaciones(id_proyecto);
CREATE INDEX IX_pagos_cotizacion ON pagos(id_cotizacion);
CREATE INDEX IX_documentos_expediente ON documentos(id_expediente);
CREATE INDEX IX_asignaciones_proyecto ON asignaciones(id_proyecto);
CREATE INDEX IX_tareas_asignacion_estado ON tareas(id_asignacion, estado);
CREATE INDEX IX_propuestas_proyecto ON propuestas(id_proyecto);
CREATE INDEX IX_tramites_proyecto ON tramites_publicos(id_proyecto);
CREATE INDEX IX_notificaciones_usuario ON notificaciones(id_usuario, leida);
CREATE INDEX IX_historial_proyecto ON historial_estados(id_proyecto, cambiado_en DESC);
GO

INSERT INTO roles (nombre, descripcion) VALUES
('ADMINISTRADOR','Administra usuarios, pagos y configuracion'),
('ENCARGADO','Gestiona proyectos y relacion con el cliente'),
('ESPECIALISTA','Desarrolla tareas tecnicas'),
('CLIENTE','Consulta y responde sobre su proyecto');

INSERT INTO especialidades (nombre, descripcion) VALUES
('Arquitectura','Diseno arquitectonico y planos'),
('Estructuras','Calculo y planos estructurales'),
('Instalaciones electricas','Diseno electrico'),
('Instalaciones sanitarias','Diseno sanitario'),
('Topografia','Levantamiento topografico'),
('Mecanica de suelos','Estudio geotecnico');
GO
