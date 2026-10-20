insert into roles (id_rol,nombre,descripcion) values
  (1,'ADMINISTRADOR','Administra usuarios, pagos y configuración'),
  (2,'ENCARGADO','Gestiona proyectos y relación con clientes'),
  (3,'ESPECIALISTA','Desarrolla tareas técnicas'),
  (4,'CLIENTE','Consulta y responde sobre sus proyectos')
on conflict (id_rol) do update set nombre=excluded.nombre,descripcion=excluded.descripcion;

select setval(pg_get_serial_sequence('roles','id_rol'),greatest((select max(id_rol) from roles),1));

create table refresh_tokens (
  id_refresh_token uuid primary key,
  id_usuario uuid not null references usuarios(id_usuario) on delete restrict,
  familia_id uuid not null,
  token_hash char(64) not null unique,
  emitido_en timestamptz not null,
  expira_en timestamptz not null,
  revocado_en timestamptz,
  reemplazado_por uuid references refresh_tokens(id_refresh_token) on delete restrict,
  ip_origen varchar(45),
  user_agent varchar(300),
  check (expira_en > emitido_en)
);
create index idx_refresh_usuario on refresh_tokens(id_usuario);
create index idx_refresh_familia on refresh_tokens(familia_id);
create index idx_refresh_expiracion on refresh_tokens(expira_en) where revocado_en is null;

create table correlativos_proyecto (
  anio smallint primary key check (anio between 2000 and 9999),
  ultimo_numero integer not null check (ultimo_numero >= 0),
  actualizado_en timestamptz not null default now()
);

comment on column refresh_tokens.token_hash is 'Hash SHA-256; nunca contiene el refresh token original';

