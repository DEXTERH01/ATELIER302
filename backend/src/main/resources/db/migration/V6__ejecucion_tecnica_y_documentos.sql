alter table documentos
  add column resource_type varchar(20) not null default 'auto',
  add column visible_cliente boolean not null default false;

update documentos
set tipo_mime = case lower(formato)
  when 'pdf' then 'application/pdf'
  when 'jpg' then 'image/jpeg'
  when 'jpeg' then 'image/jpeg'
  when 'png' then 'image/png'
  else 'application/octet-stream'
end
where tipo_mime is null;

update documentos set tamano_bytes = 0 where tamano_bytes is null;

alter table documentos
  alter column tipo_mime set not null,
  alter column tamano_bytes set not null;

create index if not exists idx_asignaciones_proyecto_estado on asignaciones(id_proyecto,estado);
create index if not exists idx_tareas_asignacion_estado on tareas(id_asignacion,estado);
create index if not exists idx_estudios_proyecto_estado on estudios_tecnicos(id_proyecto,estado);
create index if not exists idx_documentos_estudio_vigente on documentos(id_estudio,vigente);

comment on column documentos.resource_type is 'resource_type real devuelto por Cloudinary';
comment on column documentos.visible_cliente is 'Autorización explícita para mostrar el documento al cliente propietario';
