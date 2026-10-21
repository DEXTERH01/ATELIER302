-- ATELIER Gestión Digital - Datos integrales para demostración académica
-- Compatible con el esquema Flyway V1..V9.
--
-- IMPORTANTE
-- 1. Ejecutar únicamente después de que Render/Flyway haya aplicado V1..V9.
-- 2. Todos los nombres, documentos, teléfonos, operaciones y empresas son sintéticos.
-- 3. Los correos @atelier.demo no son buzones reales; sí sirven para iniciar sesión.
-- 4. Las URLs de Cloudinary usan recursos públicos ilustrativos. Sustituirlos desde la
--    plataforma cuando se quiera demostrar una carga real con la cuenta configurada.
-- 5. Contraseña común de todas las cuentas: AtelierDemo2026!
-- 6. No se insertan refresh tokens: se generan de forma segura durante el login.

begin;

select pg_advisory_xact_lock(hashtext('atelier-datos-demo-v9'));

do $$
begin
  if to_regclass('public.flyway_schema_history') is null
     or not exists (
       select 1 from flyway_schema_history where version = '9' and success
     ) then
    raise exception 'La base debe tener aplicadas las migraciones Flyway V1..V9 antes de cargar la demo';
  end if;
end $$;

-- ---------------------------------------------------------------------------
-- Seguridad y personas
-- ---------------------------------------------------------------------------

insert into roles (id_rol,nombre,descripcion) values
  (1,'ADMINISTRADOR','Administra usuarios, pagos y configuración'),
  (2,'ENCARGADO','Gestiona proyectos y relación con clientes'),
  (3,'ESPECIALISTA','Desarrolla tareas técnicas'),
  (4,'CLIENTE','Consulta y responde sobre sus proyectos')
on conflict (id_rol) do update
set nombre=excluded.nombre,descripcion=excluded.descripcion;

-- Hash BCrypt costo 12 de AtelierDemo2026!
insert into usuarios
  (id_usuario,id_rol,correo,clave_hash,nombres,apellidos,telefono,activo,ultimo_acceso)
values
  ('00000000-0000-0000-0000-000000000001',1,'admin@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Alexandra','Vega Campos','900000001',true,null),
  ('00000000-0000-0000-0000-000000000002',2,'diego.ramirez@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Diego','Ramírez Soto','900000002',true,null),
  ('00000000-0000-0000-0000-000000000003',2,'valeria.castro@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Valeria','Castro León','900000003',true,null),
  ('00000000-0000-0000-0000-000000000004',3,'lucia.torres@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Lucía','Torres Peña','900000004',true,null),
  ('00000000-0000-0000-0000-000000000005',3,'marco.salazar@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Marco','Salazar Núñez','900000005',true,null),
  ('00000000-0000-0000-0000-000000000006',3,'renato.paredes@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Renato','Paredes Díaz','900000006',true,null),
  ('00000000-0000-0000-0000-000000000007',3,'sofia.nunez@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Sofía','Núñez Rivas','900000007',true,null),
  ('00000000-0000-0000-0000-000000000008',3,'joel.cardenas@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Joel','Cárdenas Ruiz','900000008',true,null),
  ('00000000-0000-0000-0000-000000000101',4,'elena.rojas@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Elena','Rojas Flores','900000101',true,null),
  ('00000000-0000-0000-0000-000000000102',4,'familia.quinteros@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Raúl','Quinteros Silva','900000102',true,null),
  ('00000000-0000-0000-0000-000000000103',4,'inversiones.arce@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Patricia','Arce Medina','900000103',true,null),
  ('00000000-0000-0000-0000-000000000104',4,'maria.villanueva@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','María','Villanueva Paz','900000104',true,null),
  ('00000000-0000-0000-0000-000000000105',4,'grupo.nova@atelier.demo','$2a$12$UM4QVbdy/2PQjHLkw7/d0usjb/4dItbQQkyltECIW.QUeNg4Vge1q','Javier','Noriega Valdez','900000105',true,null)
on conflict (id_usuario) do update set
  id_rol=excluded.id_rol,correo=excluded.correo,clave_hash=excluded.clave_hash,
  nombres=excluded.nombres,apellidos=excluded.apellidos,telefono=excluded.telefono,activo=true;

insert into clientes
  (id_cliente,id_usuario,tipo_persona,tipo_documento,numero_documento,razon_social,direccion,distrito)
values
  ('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000101','NATURAL','DNI','70000001',null,'Av. Los Arquitectos 245','San Juan de Miraflores'),
  ('10000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000102','NATURAL','DNI','70000002',null,'Jr. Los Cedros 181','Santiago de Surco'),
  ('10000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000103','JURIDICA','RUC','20900000001','Inversiones Arce Demo S.A.C.','Av. República de Panamá 4210','Miraflores'),
  ('10000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000104','NATURAL','CE','CE-DEMO-04',null,'Calle Las Magnolias 330','La Molina'),
  ('10000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000105','JURIDICA','RUC','20900000002','Grupo Nova Demo E.I.R.L.','Av. El Sol 780','Barranco')
on conflict (id_cliente) do update set
  id_usuario=excluded.id_usuario,tipo_persona=excluded.tipo_persona,
  tipo_documento=excluded.tipo_documento,numero_documento=excluded.numero_documento,
  razon_social=excluded.razon_social,direccion=excluded.direccion,distrito=excluded.distrito;

insert into empleados
  (id_empleado,id_usuario,codigo_colegiatura,cargo,tipo,disponible)
values
  ('20000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001',null,'Administradora general','INTERNO',true),
  ('20000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000002','CAP-DEMO-2101','Jefe de proyectos','INTERNO',true),
  ('20000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000003','CAP-DEMO-2102','Coordinadora de proyectos','INTERNO',true),
  ('20000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000004','CAP-DEMO-3101','Arquitecta proyectista','INTERNO',true),
  ('20000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000005','CIP-DEMO-3102','Ingeniero estructural','EXTERNO',true),
  ('20000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000006','CIP-DEMO-3103','Ingeniero electricista','EXTERNO',true),
  ('20000000-0000-0000-0000-000000000007','00000000-0000-0000-0000-000000000007','CIP-DEMO-3104','Ingeniera sanitaria','INTERNO',true),
  ('20000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000008','CIP-DEMO-3105','Especialista en topografía','EXTERNO',true)
on conflict (id_empleado) do update set
  id_usuario=excluded.id_usuario,codigo_colegiatura=excluded.codigo_colegiatura,
  cargo=excluded.cargo,tipo=excluded.tipo,disponible=excluded.disponible;

insert into especialidades (id_especialidad,nombre,descripcion,activa) values
  (1,'Arquitectura','Diseño arquitectónico, modelado y planos',true),
  (2,'Estructuras','Cálculo y planos estructurales',true),
  (3,'Instalaciones eléctricas','Diseño de instalaciones eléctricas',true),
  (4,'Instalaciones sanitarias','Diseño de agua y desagüe',true),
  (5,'Topografía','Levantamiento topográfico',true),
  (6,'Mecánica de suelos','Estudio geotécnico',true)
on conflict (id_especialidad) do update
set nombre=excluded.nombre,descripcion=excluded.descripcion,activa=excluded.activa;

select setval(pg_get_serial_sequence('roles','id_rol'),greatest((select max(id_rol) from roles),1));
select setval(pg_get_serial_sequence('especialidades','id_especialidad'),greatest((select max(id_especialidad) from especialidades),1));

-- ---------------------------------------------------------------------------
-- Proyectos: una fotografía representativa de cada estado del flujo
-- ---------------------------------------------------------------------------

insert into proyectos
  (id_proyecto,codigo,id_cliente,id_encargado,nombre,descripcion,tipo_proyecto,
   ubicacion,area_terreno,estado,requiere_tramite_publico,fecha_solicitud,
   fecha_inicio,fecha_fin_estimada,fecha_cierre)
values
  ('30000000-0000-0000-0000-000000000001','PROY-2026-0001','10000000-0000-0000-0000-000000000001',null,'Casa Patio Rojas','Vivienda compacta con patio central y futura ampliación','VIVIENDA','San Juan de Miraflores, Lima',120.00,'SOLICITUD_REGISTRADA',false,'2026-10-20',null,null,null),
  ('30000000-0000-0000-0000-000000000002','PROY-2026-0002','10000000-0000-0000-0000-000000000002',null,'Remodelación Quinteros','Remodelación integral de primer piso y ampliación posterior','REMODELACION','Santiago de Surco, Lima',185.00,'DATOS_INCOMPLETOS',false,'2026-10-18',null,null,null),
  ('30000000-0000-0000-0000-000000000003','PROY-2026-0003','10000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','Oficinas Arce','Acondicionamiento corporativo de oficinas administrativas','OFICINAS','Miraflores, Lima',310.00,'EN_COTIZACION',false,'2026-10-12',null,'2027-01-30',null),
  ('30000000-0000-0000-0000-000000000004','PROY-2026-0004','10000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000003','Casa Jardín Villanueva','Vivienda unifamiliar de dos niveles con jardín posterior','VIVIENDA','La Molina, Lima',240.00,'COTIZACION_ENVIADA',false,'2026-10-05',null,'2027-03-15',null),
  ('30000000-0000-0000-0000-000000000005','PROY-2026-0005','10000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000002','Café Cultural Nova','Cafetería con sala de exposiciones y terraza','COMERCIAL','Barranco, Lima',165.00,'ESPERANDO_PAGO',false,'2026-09-28',null,'2027-02-28',null),
  ('30000000-0000-0000-0000-000000000006','PROY-2026-0006','10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000003','Minidepartamentos Rojas','Edificio residencial de cuatro niveles','MULTIFAMILIAR','Chorrillos, Lima',200.00,'PAGO_EN_REVISION',true,'2026-09-20',null,'2027-05-30',null),
  ('30000000-0000-0000-0000-000000000007','PROY-2026-0007','10000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000002','Ampliación Casa Quinteros','Segundo nivel y redistribución de escalera','AMPLIACION','Santiago de Surco, Lima',145.00,'PROYECTO_ACTIVO',false,'2026-09-12','2026-09-24','2027-01-31',null),
  ('30000000-0000-0000-0000-000000000008','PROY-2026-0008','10000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000003','Centro Empresarial Arce','Edificio corporativo con estacionamientos','OFICINAS','San Isidro, Lima',680.00,'EN_ESTUDIOS',true,'2026-08-25','2026-09-10','2027-08-30',null),
  ('30000000-0000-0000-0000-000000000009','PROY-2026-0009','10000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000002','Vivienda Bioclimática Villanueva','Vivienda con control solar y ventilación cruzada','VIVIENDA','Pachacámac, Lima',320.00,'EN_DISENO',false,'2026-08-10','2026-08-28','2027-02-15',null),
  ('30000000-0000-0000-0000-000000000010','PROY-2026-0010','10000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000003','Galería Nova','Galería comercial y patio gastronómico','COMERCIAL','Barranco, Lima',420.00,'REVISION_CLIENTE',false,'2026-07-22','2026-08-08','2027-03-30',null),
  ('30000000-0000-0000-0000-000000000011','PROY-2026-0011','10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000002','Casa Mirador Rojas','Vivienda de campo con terraza panorámica','VIVIENDA','Cieneguilla, Lima',510.00,'EXPEDIENTE_FINAL',false,'2026-06-18','2026-07-01','2026-12-20',null),
  ('30000000-0000-0000-0000-000000000012','PROY-2026-0012','10000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000003','Edificio Quinteros','Edificio multifamiliar de cinco niveles','MULTIFAMILIAR','Pueblo Libre, Lima',360.00,'EN_TRAMITE_PUBLICO',true,'2026-05-15','2026-06-01','2027-04-30',null),
  ('30000000-0000-0000-0000-000000000013','PROY-2026-0013','10000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','Almacén Arce','Almacén logístico y oficinas de control','INDUSTRIAL','Lurín, Lima',1250.00,'CON_OBSERVACIONES',true,'2026-04-12','2026-05-05','2027-01-31',null),
  ('30000000-0000-0000-0000-000000000014','PROY-2026-0014','10000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000003','Casa Taller Villanueva','Vivienda y taller artístico con ingreso independiente','USO_MIXTO','La Molina, Lima',285.00,'APROBADO',true,'2026-03-08','2026-03-25','2026-11-30',null),
  ('30000000-0000-0000-0000-000000000015','PROY-2026-0015','10000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000002','Tienda Nova','Diseño interior de tienda especializada','COMERCIAL','San Borja, Lima',95.00,'CERRADO',false,'2026-01-15','2026-02-01','2026-05-15','2026-05-12'),
  ('30000000-0000-0000-0000-000000000016','PROY-2026-0016','10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000003','Condominio Los Olivos','Evaluación preliminar para conjunto residencial','MULTIFAMILIAR','Los Olivos, Lima',980.00,'NO_VIABLE',true,'2026-02-02','2026-02-20','2027-06-30','2026-07-14'),
  ('30000000-0000-0000-0000-000000000017','PROY-2026-0017','10000000-0000-0000-0000-000000000002',null,'Ampliación Temporal Quinteros','Solicitud cancelada por cambio de alcance familiar','AMPLIACION','Santiago de Surco, Lima',75.00,'CANCELADO',false,'2026-09-01',null,null,'2026-09-03')
on conflict (id_proyecto) do nothing;

insert into correlativos_proyecto (anio,ultimo_numero,actualizado_en) values (2026,17,now())
on conflict (anio) do update
set ultimo_numero=greatest(correlativos_proyecto.ultimo_numero,excluded.ultimo_numero),actualizado_en=now();

insert into requerimientos
  (id_requerimiento,id_proyecto,categoria,descripcion,prioridad,obligatorio,completado)
values
  ('31000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','AMBIENTES','Sala, comedor, cocina, tres dormitorios y patio central','ALTA',true,false),
  ('31000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000002','DOCUMENTOS','Adjuntar partida registral y parámetros urbanísticos','ALTA',true,false),
  ('31000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000003','ALCANCE','Oficinas para 35 personas, directorio y comedor','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000004','ESTILO','Lenguaje contemporáneo, tonos cálidos y vegetación','MEDIA',true,true),
  ('31000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000005','OPERACION','Aforo de 60 personas y cocina de producción ligera','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000006','UNIDADES','Ocho minidepartamentos y azotea común','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000007','AMPLIACION','Dos dormitorios, baño y sala familiar en segundo nivel','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000008','PROGRAMA','Plantas flexibles y núcleo central de circulación','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000009','SOSTENIBILIDAD','Control solar, reúso de aguas grises y ventilación cruzada','MEDIA',true,true),
  ('31000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000010','COMERCIAL','Doce locales, patio gastronómico y servicios comunes','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000011','30000000-0000-0000-0000-000000000011','VISTAS','Priorizar visuales al valle desde áreas sociales','MEDIA',true,true),
  ('31000000-0000-0000-0000-000000000012','30000000-0000-0000-0000-000000000012','UNIDADES','Quince departamentos y diez estacionamientos','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000013','30000000-0000-0000-0000-000000000013','LOGISTICA','Patio de maniobras, almacén y oficinas de control','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000014','30000000-0000-0000-0000-000000000014','TALLER','Taller iluminado naturalmente con acceso independiente','MEDIA',true,true),
  ('31000000-0000-0000-0000-000000000015','30000000-0000-0000-0000-000000000015','IMAGEN','Interior sobrio con exhibición modular','MEDIA',true,true),
  ('31000000-0000-0000-0000-000000000016','30000000-0000-0000-0000-000000000016','FACTIBILIDAD','Evaluar accesibilidad vial y compatibilidad de uso','ALTA',true,true),
  ('31000000-0000-0000-0000-000000000017','30000000-0000-0000-0000-000000000017','ALCANCE','Ampliación de un dormitorio y depósito','MEDIA',true,false)
on conflict (id_requerimiento) do nothing;

-- ---------------------------------------------------------------------------
-- Cotizaciones, partidas y pagos
-- ---------------------------------------------------------------------------

insert into cotizaciones
  (id_cotizacion,id_proyecto,creada_por,numero,version,tipo,subtotal,impuesto,
   porcentaje_adelanto,estado,emitida_en,vence_en,respondida_en,observacion)
values
  ('40000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','COT-2026-0001',1,'INICIAL',9600.00,1728.00,50,'BORRADOR',null,null,null,'Borrador pendiente de revisión interna'),
  ('40000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000003','COT-2026-0002',1,'INICIAL',8200.00,1476.00,50,'ENVIADA','2026-10-08 10:00:00-05','2026-10-18 23:59:59-05',null,'Incluye arquitectura y estructuras'),
  ('40000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000002','COT-2026-0003',1,'INICIAL',6800.00,1224.00,50,'ACEPTADA','2026-10-01 09:30:00-05','2026-10-11 23:59:59-05','2026-10-03 15:20:00-05','Diseño comercial y expediente técnico'),
  ('40000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000003','COT-2026-0004',1,'INICIAL',15000.00,2700.00,50,'ACEPTADA','2026-09-22 11:00:00-05','2026-10-02 23:59:59-05','2026-09-23 18:00:00-05','Proyecto integral multifamiliar'),
  ('40000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000002','COT-2026-0005',1,'INICIAL',9200.00,1656.00,50,'ACEPTADA','2026-09-14 09:00:00-05','2026-09-24 23:59:59-05','2026-09-15 14:10:00-05','Ampliación y especialidades'),
  ('40000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000003','COT-2026-0006',1,'INICIAL',12000.00,2160.00,50,'ACEPTADA','2026-08-28 10:00:00-05','2026-09-07 23:59:59-05','2026-08-30 12:00:00-05','Anteproyecto y coordinación de especialidades'),
  ('40000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000002','COT-2026-0007',1,'INICIAL',10500.00,1890.00,50,'ACEPTADA','2026-08-12 08:30:00-05','2026-08-22 23:59:59-05','2026-08-13 13:40:00-05','Diseño bioclimático integral'),
  ('40000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000003','COT-2026-0008',1,'INICIAL',16500.00,2970.00,50,'ACEPTADA','2026-07-25 10:00:00-05','2026-08-04 23:59:59-05','2026-07-26 16:00:00-05','Proyecto comercial completo'),
  ('40000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000002','COT-2026-0009',1,'INICIAL',14000.00,2520.00,50,'ACEPTADA','2026-06-20 09:00:00-05','2026-06-30 23:59:59-05','2026-06-21 18:00:00-05','Arquitectura y expediente final'),
  ('40000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000003','COT-2026-0010',1,'INICIAL',18500.00,3330.00,50,'ACEPTADA','2026-05-18 10:00:00-05','2026-05-28 23:59:59-05','2026-05-19 17:30:00-05','Multifamiliar con gestión municipal'),
  ('40000000-0000-0000-0000-000000000011','30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000002','COT-2026-0011',1,'INICIAL',22000.00,3960.00,50,'ACEPTADA','2026-04-15 09:00:00-05','2026-04-25 23:59:59-05','2026-04-16 11:00:00-05','Proyecto industrial y licencia'),
  ('40000000-0000-0000-0000-000000000012','30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000003','COT-2026-0012',1,'INICIAL',11000.00,1980.00,50,'ACEPTADA','2026-03-10 10:00:00-05','2026-03-20 23:59:59-05','2026-03-11 15:00:00-05','Casa taller y trámite municipal'),
  ('40000000-0000-0000-0000-000000000013','30000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000002','COT-2026-0013',1,'INICIAL',7500.00,1350.00,50,'ACEPTADA','2026-01-18 09:00:00-05','2026-01-28 23:59:59-05','2026-01-19 12:00:00-05','Diseño y supervisión de implementación'),
  ('40000000-0000-0000-0000-000000000014','30000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000003','COT-2026-0014',1,'INICIAL',9800.00,1764.00,50,'ACEPTADA','2026-02-05 10:00:00-05','2026-02-15 23:59:59-05','2026-02-06 10:30:00-05','Evaluación y anteproyecto de factibilidad'),
  ('40000000-0000-0000-0000-000000000015','30000000-0000-0000-0000-000000000017','20000000-0000-0000-0000-000000000002','COT-2026-0015',1,'INICIAL',3200.00,576.00,50,'ANULADA',null,null,null,'Anulada por cancelación de la solicitud'),
  ('40000000-0000-0000-0000-000000000016','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000003','COT-2026-0008-A',1,'ADICIONAL',1800.00,324.00,100,'ACEPTADA','2026-09-18 10:00:00-05','2026-09-28 23:59:59-05','2026-09-18 17:00:00-05','Tercera ronda de cambios solicitada por el cliente')
on conflict (id_cotizacion) do nothing;

insert into detalle_cotizacion
  (id_detalle,id_cotizacion,concepto,descripcion,cantidad,precio_unitario)
values
  ('41000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001','Servicio profesional integral','Anteproyecto, desarrollo y coordinación',1,9600.00),
  ('41000000-0000-0000-0000-000000000002','40000000-0000-0000-0000-000000000002','Diseño de vivienda','Arquitectura y estructuras',1,8200.00),
  ('41000000-0000-0000-0000-000000000003','40000000-0000-0000-0000-000000000003','Diseño comercial','Cafetería y expediente técnico',1,6800.00),
  ('41000000-0000-0000-0000-000000000004','40000000-0000-0000-0000-000000000004','Proyecto multifamiliar','Arquitectura y cuatro especialidades',1,15000.00),
  ('41000000-0000-0000-0000-000000000005','40000000-0000-0000-0000-000000000005','Ampliación de vivienda','Diseño y compatibilización',1,9200.00),
  ('41000000-0000-0000-0000-000000000006','40000000-0000-0000-0000-000000000006','Centro empresarial','Anteproyecto y estudios',1,12000.00),
  ('41000000-0000-0000-0000-000000000007','40000000-0000-0000-0000-000000000007','Diseño bioclimático','Proyecto integral',1,10500.00),
  ('41000000-0000-0000-0000-000000000008','40000000-0000-0000-0000-000000000008','Galería comercial','Proyecto completo',1,16500.00),
  ('41000000-0000-0000-0000-000000000009','40000000-0000-0000-0000-000000000009','Casa de campo','Proyecto y expediente',1,14000.00),
  ('41000000-0000-0000-0000-000000000010','40000000-0000-0000-0000-000000000010','Edificio multifamiliar','Diseño y trámite',1,18500.00),
  ('41000000-0000-0000-0000-000000000011','40000000-0000-0000-0000-000000000011','Almacén logístico','Proyecto industrial integral',1,22000.00),
  ('41000000-0000-0000-0000-000000000012','40000000-0000-0000-0000-000000000012','Casa taller','Proyecto y licencia',1,11000.00),
  ('41000000-0000-0000-0000-000000000013','40000000-0000-0000-0000-000000000013','Diseño interior','Tienda y supervisión',1,7500.00),
  ('41000000-0000-0000-0000-000000000014','40000000-0000-0000-0000-000000000014','Evaluación de factibilidad','Estudios y anteproyecto',1,9800.00),
  ('41000000-0000-0000-0000-000000000015','40000000-0000-0000-0000-000000000015','Ampliación menor','Propuesta cancelada',1,3200.00),
  ('41000000-0000-0000-0000-000000000016','40000000-0000-0000-0000-000000000016','Ronda adicional de diseño','Cambios posteriores a dos rondas incluidas',1,1800.00)
on conflict (id_detalle) do nothing;

insert into pagos
  (id_pago,id_cotizacion,validado_por,tipo,metodo,monto,moneda,numero_operacion,
   comprobante_url,comprobante_public_id,comprobante_tipo_mime,
   comprobante_tamano_bytes,comprobante_categoria,comprobante_resource_type,
   estado,motivo_rechazo,pagado_en,validado_en)
values
  ('42000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000004',null,'ADELANTO','TRANSFERENCIA',8850.00,'PEN','OP-DEMO-2026-0001','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0001','image/jpeg',184250,'COMPROBANTE_PAGO','image','PENDIENTE',null,'2026-09-24 09:20:00-05',null),
  ('42000000-0000-0000-0000-000000000002','40000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',5428.00,'PEN','OP-DEMO-2026-0002','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0002','image/jpeg',181120,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-09-16 10:00:00-05','2026-09-16 14:00:00-05'),
  ('42000000-0000-0000-0000-000000000003','40000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','ADELANTO','DEPOSITO',7080.00,'PEN','OP-DEMO-2026-0003','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0003','image/jpeg',192300,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-08-31 09:00:00-05','2026-08-31 11:30:00-05'),
  ('42000000-0000-0000-0000-000000000004','40000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',6195.00,'PEN','OP-DEMO-2026-0004','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0004','image/jpeg',177800,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-08-14 08:40:00-05','2026-08-14 12:10:00-05'),
  ('42000000-0000-0000-0000-000000000005','40000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',9735.00,'PEN','OP-DEMO-2026-0005','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0005','image/jpeg',201500,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-07-27 10:00:00-05','2026-07-27 12:00:00-05'),
  ('42000000-0000-0000-0000-000000000006','40000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000001','ADELANTO','DEPOSITO',8260.00,'PEN','OP-DEMO-2026-0006','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0006','image/jpeg',187650,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-06-22 09:10:00-05','2026-06-22 10:40:00-05'),
  ('42000000-0000-0000-0000-000000000007','40000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',10915.00,'PEN','OP-DEMO-2026-0007','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0007','image/jpeg',209100,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-05-20 08:30:00-05','2026-05-20 11:00:00-05'),
  ('42000000-0000-0000-0000-000000000008','40000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',12980.00,'PEN','OP-DEMO-2026-0008','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0008','image/jpeg',215400,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-04-17 09:00:00-05','2026-04-17 12:00:00-05'),
  ('42000000-0000-0000-0000-000000000009','40000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',6490.00,'PEN','OP-DEMO-2026-0009','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0009','image/jpeg',193720,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-03-12 09:30:00-05','2026-03-12 10:45:00-05'),
  ('42000000-0000-0000-0000-000000000010','40000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000001','ADELANTO','TARJETA',4425.00,'PEN','OP-DEMO-2026-0010','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0010','image/jpeg',166200,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-01-20 11:00:00-05','2026-01-20 11:30:00-05'),
  ('42000000-0000-0000-0000-000000000011','40000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000001','ADELANTO','TRANSFERENCIA',5782.00,'PEN','OP-DEMO-2026-0011','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0011','image/jpeg',172900,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-02-07 10:00:00-05','2026-02-07 12:00:00-05'),
  ('42000000-0000-0000-0000-000000000012','40000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000001','ADICIONAL','TRANSFERENCIA',2124.00,'PEN','OP-DEMO-2026-0012','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/comprobantes/pago-0012','image/jpeg',168440,'COMPROBANTE_PAGO','image','VALIDADO',null,'2026-09-19 09:00:00-05','2026-09-19 10:20:00-05')
on conflict (id_pago) do nothing;

-- ---------------------------------------------------------------------------
-- Expedientes, equipo, tareas y estudios
-- ---------------------------------------------------------------------------

insert into expedientes (id_expediente,id_proyecto,codigo,estado,creado_en,cerrado_en) values
  ('50000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000007','EXP-2026-0007','ABIERTO','2026-09-16 14:00:00-05',null),
  ('50000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000008','EXP-2026-0008','EN_REVISION','2026-08-31 11:30:00-05',null),
  ('50000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000009','EXP-2026-0009','EN_REVISION','2026-08-14 12:10:00-05',null),
  ('50000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000010','EXP-2026-0010','EN_REVISION','2026-07-27 12:00:00-05',null),
  ('50000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000011','EXP-2026-0011','COMPLETO','2026-06-22 10:40:00-05',null),
  ('50000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000012','EXP-2026-0012','COMPLETO','2026-05-20 11:00:00-05',null),
  ('50000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000013','EXP-2026-0013','COMPLETO','2026-04-17 12:00:00-05',null),
  ('50000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000014','EXP-2026-0014','COMPLETO','2026-03-12 10:45:00-05',null),
  ('50000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000015','EXP-2026-0015','ARCHIVADO','2026-01-20 11:30:00-05','2026-05-12 17:00:00-05'),
  ('50000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000016','EXP-2026-0016','ARCHIVADO','2026-02-07 12:00:00-05','2026-07-14 15:00:00-05')
on conflict (id_expediente) do nothing;

insert into asignaciones
  (id_asignacion,id_proyecto,id_empleado,id_especialidad,rol_proyecto,fecha_asignacion,fecha_fin,estado)
values
  ('60000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000004',1,'Responsable de arquitectura','2026-09-24',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000008',5,'Levantamiento topográfico','2026-09-10',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000005',2,'Responsable estructural','2026-09-10',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000004',1,'Arquitecta proyectista','2026-08-28',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000007',4,'Especialista sanitaria','2026-08-28',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004',1,'Arquitecta líder','2026-08-08',null,'ACTIVA'),
  ('60000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000004',1,'Arquitecta líder','2026-07-01','2026-10-05','FINALIZADA'),
  ('60000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000006',3,'Especialista eléctrico','2026-06-01','2026-09-15','FINALIZADA'),
  ('60000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000005',2,'Responsable estructural','2026-05-05','2026-08-20','FINALIZADA'),
  ('60000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000007',4,'Especialista sanitaria','2026-03-25','2026-07-10','FINALIZADA'),
  ('60000000-0000-0000-0000-000000000011','30000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000004',1,'Diseño interior','2026-02-01','2026-05-10','FINALIZADA'),
  ('60000000-0000-0000-0000-000000000012','30000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000008',5,'Evaluación del terreno','2026-02-20','2026-06-15','FINALIZADA')
on conflict (id_asignacion) do nothing;

insert into tareas
  (id_tarea,id_asignacion,titulo,descripcion,prioridad,estado,fecha_inicio,fecha_limite,completada_en)
values
  ('61000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001','Preparar levantamiento arquitectónico','Medición y registro del inmueble existente','ALTA','EN_PROCESO','2026-10-18','2026-10-28',null),
  ('61000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000002','Procesar levantamiento topográfico','Generar curvas y plano perimétrico','ALTA','EN_PROCESO','2026-10-10','2026-10-25',null),
  ('61000000-0000-0000-0000-000000000003','60000000-0000-0000-0000-000000000003','Definir sistema estructural','Evaluar luces, juntas y modulación','CRITICA','BLOQUEADA','2026-10-01','2026-10-30',null),
  ('61000000-0000-0000-0000-000000000004','60000000-0000-0000-0000-000000000004','Desarrollar anteproyecto bioclimático','Plantas, cortes, fachadas y asoleamiento','ALTA','COMPLETADA','2026-09-01','2026-09-30','2026-09-29 18:00:00-05'),
  ('61000000-0000-0000-0000-000000000005','60000000-0000-0000-0000-000000000005','Trazar redes sanitarias','Agua, desagüe y reúso de aguas grises','MEDIA','EN_PROCESO','2026-10-05','2026-10-29',null),
  ('61000000-0000-0000-0000-000000000006','60000000-0000-0000-0000-000000000006','Atender observaciones de diseño','Incorporar tercera ronda validada','ALTA','COMPLETADA','2026-09-20','2026-10-02','2026-10-01 17:30:00-05'),
  ('61000000-0000-0000-0000-000000000007','60000000-0000-0000-0000-000000000007','Consolidar expediente final','Verificar láminas y memoria','ALTA','COMPLETADA','2026-09-15','2026-10-05','2026-10-04 16:00:00-05'),
  ('61000000-0000-0000-0000-000000000008','60000000-0000-0000-0000-000000000008','Compatibilizar instalaciones','Revisar arquitectura y estructuras','ALTA','COMPLETADA','2026-08-20','2026-09-15','2026-09-14 17:00:00-05'),
  ('61000000-0000-0000-0000-000000000009','60000000-0000-0000-0000-000000000009','Emitir memoria estructural','Cálculo y planos firmados','CRITICA','COMPLETADA','2026-07-15','2026-08-20','2026-08-19 18:20:00-05'),
  ('61000000-0000-0000-0000-000000000010','60000000-0000-0000-0000-000000000010','Completar memoria sanitaria','Memoria y detalles de conexión','MEDIA','COMPLETADA','2026-06-12','2026-07-10','2026-07-09 15:00:00-05'),
  ('61000000-0000-0000-0000-000000000011','60000000-0000-0000-0000-000000000011','Supervisar implementación','Visitas de obra y cierre','MEDIA','COMPLETADA','2026-04-01','2026-05-10','2026-05-10 13:00:00-05'),
  ('61000000-0000-0000-0000-000000000012','60000000-0000-0000-0000-000000000012','Evaluar accesibilidad vial','Informe técnico de factibilidad','ALTA','COMPLETADA','2026-05-20','2026-06-15','2026-06-14 12:00:00-05')
on conflict (id_tarea) do nothing;

insert into estudios_tecnicos
  (id_estudio,id_proyecto,responsable,tipo,proveedor,requerido,estado,
   fecha_solicitud,fecha_resultado,resultado,costo)
values
  ('62000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000008','Levantamiento topográfico','Topografía Andina Demo',true,'EN_PROCESO','2026-10-01',null,null,1450.00),
  ('62000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000005','Mecánica de suelos','Geotecnia Lima Demo',true,'COMPLETADO','2026-08-30','2026-09-08','Suelo granular denso; capacidad admisible 2.10 kg/cm²',1850.00),
  ('62000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000008','Levantamiento topográfico','Topografía Andina Demo',false,'NO_APLICA','2026-08-10',null,'Se utilizará levantamiento municipal vigente',0.00),
  ('62000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000005','Mecánica de suelos','GeoEnsayos Demo S.A.C.',true,'COMPLETADO','2026-06-05','2026-06-18','Cimentación superficial recomendada a 1.40 m',2200.00),
  ('62000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000008','Levantamiento topográfico','Servicios Técnicos Demo',true,'COMPLETADO','2026-05-08','2026-05-16','Linderos y cotas compatibles con partida registral',1650.00),
  ('62000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000005','Evaluación estructural','Estructuras Seguras Demo',true,'COMPLETADO','2026-04-01','2026-04-14','Estructura existente apta para intervención propuesta',1350.00),
  ('62000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000008','Factibilidad urbana','Consultoría Urbana Demo',true,'COMPLETADO','2026-03-01','2026-03-22','Acceso vial insuficiente para la densidad solicitada',1750.00)
on conflict (id_estudio) do nothing;

-- ---------------------------------------------------------------------------
-- Propuestas, rondas de revisión y adicional pagado
-- ---------------------------------------------------------------------------

insert into propuestas
  (id_propuesta,id_proyecto,creada_por,numero_version,tipo,descripcion,
   url_vista_previa,vista_previa_public_id,vista_previa_nombre,
   vista_previa_tipo_mime,vista_previa_tamano_bytes,vista_previa_resource_type,
   estado,enviada_en,respondida_en)
values
  ('70000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004',1,'ANTEPROYECTO','Primera distribución de locales','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/galeria-v1','galeria-v1.jpg','image/jpeg',214500,'image','OBSERVADA','2026-08-20 10:00:00-05','2026-08-22 16:00:00-05'),
  ('70000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004',2,'ANTEPROYECTO','Segunda distribución con patio ampliado','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/galeria-v2','galeria-v2.jpg','image/jpeg',219800,'image','OBSERVADA','2026-08-30 10:00:00-05','2026-09-01 13:00:00-05'),
  ('70000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004',3,'ANTEPROYECTO','Tercera alternativa solicitada como adicional','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/galeria-v3','galeria-v3.jpg','image/jpeg',224600,'image','OBSERVADA','2026-09-10 09:30:00-05','2026-09-12 15:00:00-05'),
  ('70000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004',4,'ANTEPROYECTO','Versión que incorpora la ronda adicional pagada','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/galeria-v4','galeria-v4.jpg','image/jpeg',230100,'image','ENVIADA','2026-10-02 10:00:00-05',null),
  ('70000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000004',1,'ANTEPROYECTO','Anteproyecto aprobado por el cliente','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/mirador-ante-v1','mirador-ante-v1.jpg','image/jpeg',225000,'image','CONFORME','2026-08-10 10:00:00-05','2026-08-12 11:00:00-05'),
  ('70000000-0000-0000-0000-000000000006','30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000004',1,'PROYECTO_FINAL','Proyecto final conforme','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/mirador-final-v1','mirador-final-v1.jpg','image/jpeg',248000,'image','CONFORME','2026-10-03 09:00:00-05','2026-10-05 12:00:00-05'),
  ('70000000-0000-0000-0000-000000000007','30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000004',1,'PROYECTO_FINAL','Proyecto multifamiliar aprobado por cliente','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/quinteros-final','quinteros-final.jpg','image/jpeg',260000,'image','CONFORME','2026-09-15 09:00:00-05','2026-09-17 17:00:00-05'),
  ('70000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000004',1,'PROYECTO_FINAL','Proyecto industrial conforme','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/arce-final','arce-final.jpg','image/jpeg',275000,'image','CONFORME','2026-08-21 10:00:00-05','2026-08-22 15:00:00-05'),
  ('70000000-0000-0000-0000-000000000009','30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000004',1,'PROYECTO_FINAL','Casa taller aprobada','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/taller-final','taller-final.jpg','image/jpeg',239000,'image','CONFORME','2026-07-11 09:00:00-05','2026-07-12 10:00:00-05'),
  ('70000000-0000-0000-0000-000000000010','30000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000004',1,'PROYECTO_FINAL','Diseño interior ejecutado','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/tienda-final','tienda-final.jpg','image/jpeg',206000,'image','CONFORME','2026-03-10 10:00:00-05','2026-03-11 18:00:00-05'),
  ('70000000-0000-0000-0000-000000000011','30000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000004',1,'ANTEPROYECTO','Alternativa preliminar no viable','https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/propuestas/olivos-v1','olivos-v1.jpg','image/jpeg',218000,'image','CONFORME','2026-05-10 09:00:00-05','2026-05-11 14:00:00-05')
on conflict (id_propuesta) do nothing;

insert into observaciones_cliente
  (id_observacion_cliente,id_propuesta,id_cliente,numero_ronda,descripcion,
   estado,es_adicional,id_cotizacion_adicional,creada_en,atendida_en)
values
  ('71000000-0000-0000-0000-000000000001','70000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000005',1,'Ampliar el patio central y mejorar la circulación hacia los servicios','ATENDIDA',false,null,'2026-08-22 16:00:00-05','2026-08-29 18:00:00-05'),
  ('71000000-0000-0000-0000-000000000002','70000000-0000-0000-0000-000000000002','10000000-0000-0000-0000-000000000005',2,'Reubicar dos locales para dar mayor frente al corredor principal','ATENDIDA',false,null,'2026-09-01 13:00:00-05','2026-09-09 17:00:00-05'),
  ('71000000-0000-0000-0000-000000000003','70000000-0000-0000-0000-000000000003','10000000-0000-0000-0000-000000000005',3,'Agregar una tercera alternativa de fachada y redistribuir el patio gastronómico','ATENDIDA',true,'40000000-0000-0000-0000-000000000016','2026-09-12 15:00:00-05','2026-10-01 17:30:00-05')
on conflict (id_observacion_cliente) do nothing;

-- ---------------------------------------------------------------------------
-- Trámites públicos, observaciones y documentos del expediente
-- ---------------------------------------------------------------------------

insert into tramites_publicos
  (id_tramite,id_proyecto,responsable,entidad,tipo_tramite,numero_expediente_externo,
   estado,fecha_presentacion,fecha_respuesta,observacion_general)
values
  ('80000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000003','Municipalidad de Pueblo Libre - Demo','Licencia de edificación','MPL-DEMO-2026-0142','EN_REVISION','2026-10-01',null,'Expediente admitido y en evaluación técnica'),
  ('80000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000002','Municipalidad de Lurín - Demo','Licencia de edificación industrial','MLU-DEMO-2026-0088','OBSERVADO','2026-09-05','2026-10-10','Pendiente subsanar plano de seguridad y cuadro de áreas'),
  ('80000000-0000-0000-0000-000000000003','30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000003','Municipalidad de La Molina - Demo','Licencia de remodelación','MLM-DEMO-2026-0194','APROBADO','2026-07-20','2026-09-22','Licencia aprobada para ejecución'),
  ('80000000-0000-0000-0000-000000000004','30000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000002','Municipalidad de San Borja - Demo','Conformidad de obra menor','MSB-DEMO-2026-0047','ARCHIVADO','2026-04-20','2026-05-12','Trámite cerrado con conformidad'),
  ('80000000-0000-0000-0000-000000000005','30000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000003','Municipalidad de Los Olivos - Demo','Consulta de compatibilidad de uso','MLO-DEMO-2026-0031','RECHAZADO','2026-06-20','2026-07-14','Uso solicitado incompatible con zonificación y acceso existente')
on conflict (id_tramite) do nothing;

insert into observaciones_entidad
  (id_observacion_entidad,id_tramite,numero,descripcion,fecha_notificacion,
   fecha_limite,estado,respuesta,fecha_subsanacion)
values
  ('81000000-0000-0000-0000-000000000001','80000000-0000-0000-0000-000000000002',1,'Actualizar plano de seguridad, rutas de evacuación y cuadro de áreas','2026-10-10','2026-10-30','PENDIENTE',null,null),
  ('81000000-0000-0000-0000-000000000002','80000000-0000-0000-0000-000000000003',1,'Precisar detalle de conexión sanitaria en primer nivel','2026-08-12','2026-08-26','SUBSANADA','Se adjuntó detalle IS-04 y memoria sanitaria corregida','2026-08-22')
on conflict (id_observacion_entidad) do nothing;

insert into documentos
  (id_documento,id_expediente,subido_por,nombre,tipo_documento,version,
   url_cloudinary,public_id_cloudinary,formato,tamano_bytes,vigente,categoria,
   tipo_mime,id_estudio,id_propuesta,id_tramite,resource_type,visible_cliente)
values
  ('51000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000008','Avance del levantamiento topográfico','INFORME',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/topografia-avance','JPG',184200,true,'ESTUDIO','image/jpeg','62000000-0000-0000-0000-000000000001',null,null,'image',false),
  ('51000000-0000-0000-0000-000000000002','50000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000005','Informe de mecánica de suelos','INFORME_TECNICO',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/suelos-v1','JPG',196400,true,'ESTUDIO','image/jpeg','62000000-0000-0000-0000-000000000002',null,null,'image',true),
  ('51000000-0000-0000-0000-000000000003','50000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000004','Anteproyecto Galería Nova v4','PLANO',4,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/galeria-propuesta-v4','JPG',230100,true,'PROPUESTA','image/jpeg',null,'70000000-0000-0000-0000-000000000004',null,'image',true),
  ('51000000-0000-0000-0000-000000000004','50000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000004','Proyecto final Casa Mirador','PLANO',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/mirador-final','JPG',248000,true,'PROPUESTA','image/jpeg',null,'70000000-0000-0000-0000-000000000006',null,'image',true),
  ('51000000-0000-0000-0000-000000000005','50000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000003','Cargo de ingreso municipal','CARGO',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/cargo-quinteros','JPG',188500,true,'TRAMITE','image/jpeg',null,null,'80000000-0000-0000-0000-000000000001','image',true),
  ('51000000-0000-0000-0000-000000000006','50000000-0000-0000-0000-000000000007','00000000-0000-0000-0000-000000000002','Notificación de observaciones','NOTIFICACION',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/observacion-arce','JPG',174200,true,'TRAMITE','image/jpeg',null,null,'80000000-0000-0000-0000-000000000002','image',true),
  ('51000000-0000-0000-0000-000000000007','50000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000003','Resolución de licencia aprobada','RESOLUCION',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/resolucion-taller','JPG',205800,true,'RESOLUCION','image/jpeg',null,null,'80000000-0000-0000-0000-000000000003','image',true),
  ('51000000-0000-0000-0000-000000000008','50000000-0000-0000-0000-000000000009','00000000-0000-0000-0000-000000000002','Acta de cierre del proyecto','ACTA',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/acta-tienda','JPG',163700,true,'EXPEDIENTE','image/jpeg',null,null,null,'image',true),
  ('51000000-0000-0000-0000-000000000009','50000000-0000-0000-0000-000000000010','00000000-0000-0000-0000-000000000003','Informe de no viabilidad','INFORME',1,'https://res.cloudinary.com/demo/image/upload/sample.jpg','atelier/demo/documentos/no-viable-olivos','JPG',179900,true,'RESOLUCION','image/jpeg',null,null,'80000000-0000-0000-0000-000000000005','image',true)
on conflict (id_documento) do nothing;

-- ---------------------------------------------------------------------------
-- Notificaciones, historia y auditoría
-- ---------------------------------------------------------------------------

insert into notificaciones
  (id_notificacion,id_usuario,id_proyecto,titulo,mensaje,tipo,leida,creada_en,leida_en)
values
  ('90000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000104','30000000-0000-0000-0000-000000000010','Propuesta lista para revisión','La cuarta versión del anteproyecto está disponible para tu conformidad.','ACCION',false,'2026-10-02 10:05:00-05',null),
  ('90000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000002','30000000-0000-0000-0000-000000000013','Observación municipal pendiente','El expediente industrial tiene una observación con vencimiento el 30 de octubre.','VENCIMIENTO',false,'2026-10-10 14:10:00-05',null),
  ('90000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000104','30000000-0000-0000-0000-000000000014','Licencia aprobada','La municipalidad aprobó la licencia de tu Casa Taller.','INFORMATIVA',true,'2026-09-22 16:00:00-05','2026-09-22 18:30:00-05'),
  ('90000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000101','30000000-0000-0000-0000-000000000006','Pago en revisión','Recibimos el comprobante del adelanto y será revisado por administración.','INFORMATIVA',true,'2026-09-24 09:22:00-05','2026-09-24 09:30:00-05'),
  ('90000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000008','30000000-0000-0000-0000-000000000008','Tarea próxima a vencer','El levantamiento topográfico vence el 25 de octubre.','ALERTA',false,'2026-10-20 08:00:00-05',null),
  ('90000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000105','30000000-0000-0000-0000-000000000015','Proyecto cerrado','El proyecto Tienda Nova fue cerrado satisfactoriamente.','INFORMATIVA',true,'2026-05-12 17:10:00-05','2026-05-12 18:00:00-05')
on conflict (id_notificacion) do nothing;

insert into historial_estados
  (id_historial,id_proyecto,cambiado_por,estado_anterior,estado_nuevo,comentario,cambiado_en)
values
  (1001,'30000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000101',null,'SOLICITUD_REGISTRADA','Solicitud registrada desde el portal','2026-10-20 09:00:00-05'),
  (1002,'30000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000102','SOLICITUD_REGISTRADA','DATOS_INCOMPLETOS','Faltan partida y parámetros urbanísticos','2026-10-19 10:00:00-05'),
  (1003,'30000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000002','SOLICITUD_REGISTRADA','EN_COTIZACION','Requerimientos completos','2026-10-14 11:00:00-05'),
  (1004,'30000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000003','EN_COTIZACION','COTIZACION_ENVIADA','Cotización enviada con vigencia de diez días','2026-10-08 10:00:00-05'),
  (1005,'30000000-0000-0000-0000-000000000005','00000000-0000-0000-0000-000000000105','COTIZACION_ENVIADA','ESPERANDO_PAGO','Cotización aceptada por el cliente','2026-10-03 15:20:00-05'),
  (1006,'30000000-0000-0000-0000-000000000006','00000000-0000-0000-0000-000000000101','ESPERANDO_PAGO','PAGO_EN_REVISION','Comprobante de adelanto registrado','2026-09-24 09:20:00-05'),
  (1007,'30000000-0000-0000-0000-000000000007','00000000-0000-0000-0000-000000000001','PAGO_EN_REVISION','PROYECTO_ACTIVO','Adelanto validado y expediente creado','2026-09-16 14:00:00-05'),
  (1008,'30000000-0000-0000-0000-000000000008','00000000-0000-0000-0000-000000000003','PROYECTO_ACTIVO','EN_ESTUDIOS','Estudios obligatorios programados','2026-09-12 10:00:00-05'),
  (1009,'30000000-0000-0000-0000-000000000009','00000000-0000-0000-0000-000000000003','EN_ESTUDIOS','EN_DISENO','Estudios obligatorios concluidos','2026-09-09 10:00:00-05'),
  (1010,'30000000-0000-0000-0000-000000000010','00000000-0000-0000-0000-000000000003','EN_DISENO','REVISION_CLIENTE','Nueva propuesta enviada al cliente','2026-10-02 10:00:00-05'),
  (1011,'30000000-0000-0000-0000-000000000011','00000000-0000-0000-0000-000000000101','REVISION_CLIENTE','EXPEDIENTE_FINAL','Proyecto final conforme','2026-10-05 12:00:00-05'),
  (1012,'30000000-0000-0000-0000-000000000012','00000000-0000-0000-0000-000000000003','EXPEDIENTE_FINAL','EN_TRAMITE_PUBLICO','Expediente presentado a la municipalidad','2026-10-01 09:00:00-05'),
  (1013,'30000000-0000-0000-0000-000000000013','00000000-0000-0000-0000-000000000002','EN_TRAMITE_PUBLICO','CON_OBSERVACIONES','Entidad emitió observaciones subsanables','2026-10-10 14:00:00-05'),
  (1014,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','EN_TRAMITE_PUBLICO','APROBADO','Licencia municipal aprobada','2026-09-22 16:00:00-05'),
  (1015,'30000000-0000-0000-0000-000000000015','00000000-0000-0000-0000-000000000002','APROBADO','CERRADO','Entregables aceptados y cierre administrativo','2026-05-12 17:00:00-05'),
  (1016,'30000000-0000-0000-0000-000000000016','00000000-0000-0000-0000-000000000003','EN_TRAMITE_PUBLICO','NO_VIABLE','La entidad declaró incompatible el uso solicitado','2026-07-14 15:00:00-05'),
  (1017,'30000000-0000-0000-0000-000000000017','00000000-0000-0000-0000-000000000102','SOLICITUD_REGISTRADA','CANCELADO','El cliente canceló la solicitud por cambio de alcance','2026-09-03 10:00:00-05'),
  (1018,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000104',null,'SOLICITUD_REGISTRADA','Solicitud inicial de Casa Taller','2026-03-08 09:00:00-05'),
  (1019,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','SOLICITUD_REGISTRADA','EN_COTIZACION','Información validada','2026-03-09 11:00:00-05'),
  (1020,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','EN_COTIZACION','COTIZACION_ENVIADA','Cotización emitida','2026-03-10 10:00:00-05'),
  (1021,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000104','COTIZACION_ENVIADA','ESPERANDO_PAGO','Cotización aceptada','2026-03-11 15:00:00-05'),
  (1022,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000104','ESPERANDO_PAGO','PAGO_EN_REVISION','Adelanto registrado','2026-03-12 09:30:00-05'),
  (1023,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000001','PAGO_EN_REVISION','PROYECTO_ACTIVO','Pago validado','2026-03-12 10:45:00-05'),
  (1024,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','PROYECTO_ACTIVO','EN_ESTUDIOS','Evaluación estructural requerida','2026-04-01 09:00:00-05'),
  (1025,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','EN_ESTUDIOS','EN_DISENO','Evaluación completada','2026-04-14 16:00:00-05'),
  (1026,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','EN_DISENO','REVISION_CLIENTE','Proyecto final enviado','2026-07-11 09:00:00-05'),
  (1027,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000104','REVISION_CLIENTE','EXPEDIENTE_FINAL','Proyecto conforme','2026-07-12 10:00:00-05'),
  (1028,'30000000-0000-0000-0000-000000000014','00000000-0000-0000-0000-000000000003','EXPEDIENTE_FINAL','EN_TRAMITE_PUBLICO','Expediente presentado','2026-07-20 09:00:00-05')
on conflict (id_historial) do nothing;

insert into auditoria
  (id_auditoria,id_usuario,tabla_afectada,id_registro,accion,detalle,ip_origen,creado_en)
values
  (2001,'00000000-0000-0000-0000-000000000001','pagos','42000000-0000-0000-0000-000000000009','UPDATE','{"evento":"VALIDACION_PAGO","resultado":"VALIDADO","entorno":"DEMO"}'::jsonb,'127.0.0.1','2026-03-12 10:45:00-05'),
  (2002,'00000000-0000-0000-0000-000000000003','proyectos','30000000-0000-0000-0000-000000000014','UPDATE','{"evento":"CAMBIO_ESTADO","estado":"APROBADO","entorno":"DEMO"}'::jsonb,'127.0.0.1','2026-09-22 16:00:00-05'),
  (2003,'00000000-0000-0000-0000-000000000105','propuestas','70000000-0000-0000-0000-000000000003','UPDATE','{"evento":"OBSERVACION_ADICIONAL","ronda":3,"entorno":"DEMO"}'::jsonb,'127.0.0.1','2026-09-12 15:00:00-05'),
  (2004,'00000000-0000-0000-0000-000000000002','tramites_publicos','80000000-0000-0000-0000-000000000002','UPDATE','{"evento":"OBSERVACION_ENTIDAD","numero":1,"entorno":"DEMO"}'::jsonb,'127.0.0.1','2026-10-10 14:00:00-05'),
  (2005,'00000000-0000-0000-0000-000000000001','reportes',null,'EXPORT','{"reporte":"PROYECTOS_POR_ESTADO","formato":"XLSX","entorno":"DEMO"}'::jsonb,'127.0.0.1','2026-10-20 16:00:00-05')
on conflict (id_auditoria) do nothing;

do $$
begin
  if (select count(*) from usuarios where correo like '%@atelier.demo') < 13 then
    raise exception 'Carga demo incompleta: se esperaban 13 usuarios @atelier.demo';
  end if;
  if exists (
    select 1 from usuarios
    where correo like '%@atelier.demo'
      and (length(clave_hash) <> 60 or clave_hash not like '$2a$12$%')
  ) then
    raise exception 'Carga demo inválida: existe una contraseña sin BCrypt costo 12';
  end if;
  if (select count(*) from proyectos where codigo between 'PROY-2026-0001' and 'PROY-2026-0017') <> 17
     or (select count(distinct estado) from proyectos where codigo between 'PROY-2026-0001' and 'PROY-2026-0017') <> 17 then
    raise exception 'Carga demo incompleta: deben existir 17 proyectos en 17 estados diferentes';
  end if;
  if not exists (
    select 1
    from observaciones_cliente o
    join cotizaciones c on c.id_cotizacion=o.id_cotizacion_adicional
    join pagos p on p.id_cotizacion=c.id_cotizacion
    where o.numero_ronda=3 and o.es_adicional and p.tipo='ADICIONAL' and p.estado='VALIDADO'
  ) then
    raise exception 'Carga demo incompleta: falta la tercera ronda adicional pagada';
  end if;
end $$;

select setval(pg_get_serial_sequence('historial_estados','id_historial'),greatest((select max(id_historial) from historial_estados),1));
select setval(pg_get_serial_sequence('auditoria','id_auditoria'),greatest((select max(id_auditoria) from auditoria),1));

commit;

-- Resumen esperado en una base recién migrada:
-- 13 usuarios, 5 clientes, 8 empleados, 17 proyectos, 16 cotizaciones,
-- 12 pagos, 10 expedientes, 12 asignaciones, 12 tareas, 7 estudios,
-- 11 propuestas, 3 rondas de observación, 5 trámites y 9 documentos.
