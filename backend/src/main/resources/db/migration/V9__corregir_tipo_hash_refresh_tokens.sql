-- RefreshToken.tokenHash se mapea en JPA como VARCHAR(64). El tipo CHAR(64)
-- de la migración inicial se expone por JDBC como bpchar y falla la validación.
alter table refresh_tokens
  alter column token_hash type varchar(64) using btrim(token_hash);

comment on column refresh_tokens.token_hash is
  'Hash SHA-256 hexadecimal (64 caracteres); nunca contiene el refresh token original';
