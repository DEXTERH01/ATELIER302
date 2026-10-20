alter table documentos add column categoria varchar(30) not null default 'EXPEDIENTE'
  check (categoria in ('EXPEDIENTE','ESTUDIO','PROPUESTA','TRAMITE','RESOLUCION'));
alter table documentos add column tipo_mime varchar(150);
alter table documentos add column id_estudio uuid references estudios_tecnicos(id_estudio) on delete restrict;
alter table documentos add column id_propuesta uuid references propuestas(id_propuesta) on delete restrict;
alter table documentos add column id_tramite uuid references tramites_publicos(id_tramite) on delete restrict;
alter table documentos add constraint chk_documento_origen_unico check
  (num_nonnulls(id_estudio,id_propuesta,id_tramite) <= 1);
alter table propuestas add column vista_previa_public_id varchar(300);

alter table requerimientos drop constraint requerimientos_id_proyecto_fkey;
alter table requerimientos add constraint fk_requerimientos_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table expedientes drop constraint expedientes_id_proyecto_fkey;
alter table expedientes add constraint fk_expedientes_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table asignaciones drop constraint asignaciones_id_proyecto_fkey;
alter table asignaciones add constraint fk_asignaciones_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table estudios_tecnicos drop constraint estudios_tecnicos_id_proyecto_fkey;
alter table estudios_tecnicos add constraint fk_estudios_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table propuestas drop constraint propuestas_id_proyecto_fkey;
alter table propuestas add constraint fk_propuestas_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table tramites_publicos drop constraint tramites_publicos_id_proyecto_fkey;
alter table tramites_publicos add constraint fk_tramites_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table notificaciones drop constraint notificaciones_id_proyecto_fkey;
alter table notificaciones add constraint fk_notificaciones_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;
alter table historial_estados drop constraint historial_estados_id_proyecto_fkey;
alter table historial_estados add constraint fk_historial_proyecto foreign key(id_proyecto) references proyectos(id_proyecto) on delete restrict;

comment on column documentos.url_cloudinary is 'secure_url devuelta por Cloudinary';
comment on column documentos.public_id_cloudinary is 'public_id requerido para administrar o eliminar el recurso';

