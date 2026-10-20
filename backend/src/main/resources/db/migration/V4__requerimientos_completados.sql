alter table requerimientos
  add column completado boolean not null default false;

create index idx_requerimientos_proyecto_obligatorio
  on requerimientos(id_proyecto, obligatorio, completado);

comment on column requerimientos.completado is
  'Indica que el dato o entregable requerido fue recibido y revisado';
