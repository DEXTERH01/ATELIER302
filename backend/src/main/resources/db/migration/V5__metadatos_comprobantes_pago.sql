alter table pagos
  add column comprobante_tipo_mime varchar(150),
  add column comprobante_tamano_bytes bigint
    check (comprobante_tamano_bytes is null or comprobante_tamano_bytes >= 0),
  add column comprobante_categoria varchar(40) not null default 'COMPROBANTE_PAGO'
    check (comprobante_categoria = 'COMPROBANTE_PAGO'),
  add column comprobante_resource_type varchar(20) not null default 'auto';

create index idx_pagos_estado on pagos(estado);
create index idx_cotizaciones_estado_vencimiento on cotizaciones(estado, vence_en);

comment on column pagos.comprobante_url is 'secure_url devuelta por Cloudinary';
comment on column pagos.comprobante_public_id is 'public_id usado para compensación y administración del recurso';
comment on column pagos.comprobante_tipo_mime is 'MIME validado por el backend antes de cargar el archivo';
comment on column pagos.comprobante_tamano_bytes is 'Tamaño exacto del comprobante cargado';

alter table pagos
  alter column comprobante_url set not null,
  alter column comprobante_public_id set not null,
  alter column comprobante_tipo_mime set not null,
  alter column comprobante_tamano_bytes set not null;
