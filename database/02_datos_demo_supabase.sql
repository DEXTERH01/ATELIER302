-- ATELIER - Datos ficticios para demostracion del flujo TO-BE
-- Ejecutar despues de 01_supabase_postgresql.sql.
-- Todos los correos, documentos, URLs y numeros de operacion son ficticios.

insert into roles (id_rol, nombre, descripcion) values
    (1, 'ADMINISTRADOR', 'Administra usuarios, pagos y configuracion'),
    (2, 'ENCARGADO', 'Gestiona proyectos y relacion con el cliente'),
    (3, 'ESPECIALISTA', 'Desarrolla tareas tecnicas'),
    (4, 'CLIENTE', 'Consulta y responde sobre su proyecto')
on conflict (id_rol) do nothing;

select setval(pg_get_serial_sequence('roles','id_rol'), greatest((select max(id_rol) from roles), 1));

insert into usuarios
    (id_usuario, id_rol, correo, clave_hash, nombres, apellidos, telefono)
values
    ('00000000-0000-0000-0000-000000000001', 1, 'admin@atelier.demo', '$2a$10$DEMO_NO_USAR_EN_PRODUCCION', 'Andrea', 'Administradora', '999000001'),
    ('00000000-0000-0000-0000-000000000002', 2, 'encargado@atelier.demo', '$2a$10$DEMO_NO_USAR_EN_PRODUCCION', 'Carlos', 'Mendoza', '999000002'),
    ('00000000-0000-0000-0000-000000000003', 3, 'arquitecta@atelier.demo', '$2a$10$DEMO_NO_USAR_EN_PRODUCCION', 'Lucia', 'Torres', '999000003'),
    ('00000000-0000-0000-0000-000000000004', 3, 'estructuras@atelier.demo', '$2a$10$DEMO_NO_USAR_EN_PRODUCCION', 'Marco', 'Salazar', '999000004'),
    ('00000000-0000-0000-0000-000000000005', 4, 'cliente@atelier.demo', '$2a$10$DEMO_NO_USAR_EN_PRODUCCION', 'Elena', 'Rojas', '999000005')
on conflict (id_usuario) do nothing;

insert into clientes
    (id_cliente, id_usuario, tipo_persona, tipo_documento, numero_documento, direccion, distrito)
values
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000005',
     'NATURAL', 'DNI', '70000001', 'Av. Los Arquitectos 245', 'San Juan de Miraflores')
on conflict (id_cliente) do nothing;

insert into empleados (id_empleado, id_usuario, codigo_colegiatura, cargo, tipo) values
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', null, 'Administradora', 'INTERNO'),
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'CAP-DEMO-1001', 'Jefe de proyecto', 'INTERNO'),
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', 'CAP-DEMO-1002', 'Arquitecta', 'INTERNO'),
    ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000004', 'CIP-DEMO-1003', 'Ingeniero estructural', 'EXTERNO')
on conflict (id_empleado) do nothing;

insert into especialidades (id_especialidad, nombre, descripcion) values
    (1, 'Arquitectura', 'Diseno arquitectonico y planos'),
    (2, 'Estructuras', 'Calculo y planos estructurales'),
    (3, 'Instalaciones electricas', 'Diseno de instalaciones electricas'),
    (4, 'Instalaciones sanitarias', 'Diseno de agua y desague'),
    (5, 'Topografia', 'Levantamiento topografico'),
    (6, 'Mecanica de suelos', 'Estudio geotecnico')
on conflict (id_especialidad) do nothing;

select setval(pg_get_serial_sequence('especialidades','id_especialidad'), greatest((select max(id_especialidad) from especialidades), 1));

insert into proyectos
    (id_proyecto, codigo, id_cliente, id_encargado, nombre, descripcion, tipo_proyecto,
     ubicacion, area_terreno, estado, requiere_tramite_publico, fecha_solicitud, fecha_inicio, fecha_fin_estimada)
values
    ('30000000-0000-0000-0000-000000000001', 'PROY-2026-0001',
     '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
     'Vivienda unifamiliar Rojas', 'Diseno integral de vivienda de dos pisos', 'VIVIENDA',
     'San Juan de Miraflores, Lima', 120.00, 'EN_TRAMITE_PUBLICO', true,
     '2026-09-15', '2026-09-22', '2026-12-15')
on conflict (id_proyecto) do nothing;

insert into requerimientos (id_requerimiento, id_proyecto, categoria, descripcion, prioridad) values
    ('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'AMBIENTES', 'Sala, comedor, cocina, tres dormitorios y azotea', 'ALTA'),
    ('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'ESTILO', 'Fachada contemporanea con iluminacion natural', 'MEDIA')
on conflict (id_requerimiento) do nothing;

insert into cotizaciones
    (id_cotizacion, id_proyecto, creada_por, numero, version, tipo, subtotal, impuesto,
     porcentaje_adelanto, estado, emitida_en, vence_en, respondida_en)
values
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000002', 'COT-2026-0001', 1, 'INICIAL',
     5000.00, 900.00, 50.00, 'ACEPTADA', '2026-09-16 10:00:00-05',
     '2026-09-26 23:59:59-05', '2026-09-17 15:30:00-05')
on conflict (id_cotizacion) do nothing;

insert into detalle_cotizacion (id_detalle, id_cotizacion, concepto, cantidad, precio_unitario) values
    ('41000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'Diseno arquitectonico', 1, 3000.00),
    ('41000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'Especialidad de estructuras', 1, 1200.00),
    ('41000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000001', 'Gestion de expediente municipal', 1, 800.00)
on conflict (id_detalle) do nothing;

insert into pagos
    (id_pago, id_cotizacion, validado_por, tipo, metodo, monto, numero_operacion,
     comprobante_url, comprobante_public_id, estado, pagado_en, validado_en)
values
    ('42000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000001', 'ADELANTO', 'TRANSFERENCIA', 2950.00,
     'DEMO-OP-2026-001', 'https://res.cloudinary.com/demo/image/upload/atelier/comprobantes/demo_pago.jpg',
     'atelier/comprobantes/demo_pago', 'VALIDADO', '2026-09-18 09:15:00-05', '2026-09-18 11:00:00-05')
on conflict (id_pago) do nothing;

insert into expedientes (id_expediente, id_proyecto, codigo, estado) values
    ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'EXP-2026-0001', 'EN_REVISION')
on conflict (id_expediente) do nothing;

insert into documentos
    (id_documento, id_expediente, subido_por, nombre, tipo_documento, version,
     url_cloudinary, public_id_cloudinary, formato, tamano_bytes)
values
    ('51000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000003', 'Plano arquitectonico v1', 'PLANO', 1,
     'https://res.cloudinary.com/demo/raw/upload/atelier/expedientes/plano_v1.pdf',
     'atelier/expedientes/plano_v1', 'PDF', 2450000),
    ('51000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000004', 'Memoria de estructuras', 'MEMORIA', 1,
     'https://res.cloudinary.com/demo/raw/upload/atelier/expedientes/memoria_estructuras.pdf',
     'atelier/expedientes/memoria_estructuras', 'PDF', 1320000)
on conflict (id_documento) do nothing;

insert into asignaciones
    (id_asignacion, id_proyecto, id_empleado, id_especialidad, rol_proyecto, fecha_asignacion)
values
    ('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000003', 1, 'Responsable de arquitectura', '2026-09-22'),
    ('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000004', 2, 'Responsable de estructuras', '2026-09-22')
on conflict (id_asignacion) do nothing;

insert into tareas
    (id_tarea, id_asignacion, titulo, descripcion, prioridad, estado, fecha_inicio, fecha_limite, completada_en)
values
    ('61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001',
     'Elaborar anteproyecto', 'Preparar plantas, cortes y elevaciones', 'ALTA', 'COMPLETADA',
     '2026-09-23', '2026-10-05', '2026-10-04 17:30:00-05'),
    ('61000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002',
     'Desarrollar calculo estructural', 'Calcular elementos estructurales principales', 'ALTA', 'COMPLETADA',
     '2026-10-05', '2026-10-15', '2026-10-14 18:00:00-05')
on conflict (id_tarea) do nothing;

insert into estudios_tecnicos
    (id_estudio, id_proyecto, responsable, tipo, proveedor, estado, fecha_solicitud,
     fecha_resultado, resultado, costo)
values
    ('62000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000004', 'Mecanica de suelos', 'Geotecnia Demo SAC',
     'COMPLETADO', '2026-09-23', '2026-09-30', 'Capacidad portante apta para vivienda de dos pisos', 750.00)
on conflict (id_estudio) do nothing;

insert into propuestas
    (id_propuesta, id_proyecto, creada_por, numero_version, tipo, descripcion,
     url_vista_previa, estado, enviada_en, respondida_en)
values
    ('70000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000003', 1, 'ANTEPROYECTO', 'Primera propuesta',
     'https://res.cloudinary.com/demo/image/upload/atelier/propuestas/v1.jpg', 'OBSERVADA',
     '2026-10-05 10:00:00-05', '2026-10-06 16:00:00-05'),
    ('70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000003', 2, 'ANTEPROYECTO', 'Propuesta corregida',
     'https://res.cloudinary.com/demo/image/upload/atelier/propuestas/v2.jpg', 'CONFORME',
     '2026-10-08 10:00:00-05', '2026-10-09 12:00:00-05')
on conflict (id_propuesta) do nothing;

insert into observaciones_cliente
    (id_observacion_cliente, id_propuesta, id_cliente, numero_ronda, descripcion, estado, atendida_en)
values
    ('71000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001', 1, 'Ampliar la ventana de la sala y mover el acceso a la cocina',
     'ATENDIDA', '2026-10-08 09:00:00-05')
on conflict (id_observacion_cliente) do nothing;

insert into tramites_publicos
    (id_tramite, id_proyecto, responsable, entidad, tipo_tramite,
     numero_expediente_externo, estado, fecha_presentacion)
values
    ('80000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
     '20000000-0000-0000-0000-000000000002', 'Municipalidad Distrital - DEMO',
     'Licencia de edificacion', 'MUNI-DEMO-2026-001', 'OBSERVADO', '2026-10-16')
on conflict (id_tramite) do nothing;

insert into observaciones_entidad
    (id_observacion_entidad, id_tramite, numero, descripcion, fecha_notificacion,
     fecha_limite, estado)
values
    ('81000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001',
     1, 'Adjuntar plano de ubicacion con cuadro normativo actualizado', '2026-10-19', '2026-10-30', 'PENDIENTE')
on conflict (id_observacion_entidad) do nothing;

insert into notificaciones
    (id_notificacion, id_usuario, id_proyecto, titulo, mensaje, tipo)
values
    ('90000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
     '30000000-0000-0000-0000-000000000001', 'Nueva observacion municipal',
     'El expediente MUNI-DEMO-2026-001 tiene una observacion pendiente de subsanacion.', 'ACCION'),
    ('90000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005',
     '30000000-0000-0000-0000-000000000001', 'Proyecto en tramite',
     'Tu proyecto fue presentado ante la entidad publica y se encuentra en revision.', 'INFORMATIVA')
on conflict (id_notificacion) do nothing;

insert into historial_estados
    (id_proyecto, cambiado_por, estado_anterior, estado_nuevo, comentario, cambiado_en)
values
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000005', null, 'SOLICITUD_REGISTRADA', 'Solicitud creada desde el portal', '2026-09-15 09:00:00-05'),
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'SOLICITUD_REGISTRADA', 'COTIZACION_ENVIADA', 'Cotizacion enviada al cliente', '2026-09-16 10:00:00-05'),
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'PAGO_EN_REVISION', 'PROYECTO_ACTIVO', 'Adelanto validado', '2026-09-18 11:00:00-05'),
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'EXPEDIENTE_FINAL', 'EN_TRAMITE_PUBLICO', 'Expediente presentado ante la municipalidad', '2026-10-16 11:30:00-05'),
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'EN_TRAMITE_PUBLICO', 'CON_OBSERVACIONES', 'Entidad emitio una observacion', '2026-10-19 14:00:00-05');

insert into auditoria
    (id_usuario, tabla_afectada, id_registro, accion, detalle)
values
    ('00000000-0000-0000-0000-000000000001', 'pagos', '42000000-0000-0000-0000-000000000001',
     'UPDATE', '{"evento":"VALIDACION_PAGO","resultado":"VALIDADO","entorno":"DEMO"}'::jsonb);
