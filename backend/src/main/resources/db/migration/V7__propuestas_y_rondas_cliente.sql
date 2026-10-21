alter table propuestas
  add column vista_previa_nombre varchar(200),
  add column vista_previa_tipo_mime varchar(150),
  add column vista_previa_tamano_bytes bigint check (vista_previa_tamano_bytes is null or vista_previa_tamano_bytes >= 0),
  add column vista_previa_resource_type varchar(20);

alter table observaciones_cliente
  add column id_cotizacion_adicional uuid references cotizaciones(id_cotizacion) on delete restrict;

create unique index if not exists uq_observacion_cotizacion_adicional
  on observaciones_cliente(id_cotizacion_adicional)
  where id_cotizacion_adicional is not null;

create index if not exists idx_propuestas_proyecto_tipo_version
  on propuestas(id_proyecto,tipo,numero_version desc);
create index if not exists idx_observaciones_propuesta_estado
  on observaciones_cliente(id_propuesta,estado);

comment on column propuestas.vista_previa_public_id is 'public_id requerido para administrar la vista previa en Cloudinary';
comment on column observaciones_cliente.id_cotizacion_adicional is 'Cotización adicional opcional originada desde la tercera ronda';
