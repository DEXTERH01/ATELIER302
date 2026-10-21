-- Hibernate mapea Pago.moneda como VARCHAR(3). La estructura inicial la
-- declaró como CHAR(3), que PostgreSQL informa a JDBC como bpchar.
-- Conservamos los datos y normalizamos cualquier espacio de relleno previo.
alter table pagos
  alter column moneda drop default,
  alter column moneda type varchar(3) using btrim(moneda),
  alter column moneda set default 'PEN';

comment on column pagos.moneda is 'Código ISO 4217 de tres caracteres';
