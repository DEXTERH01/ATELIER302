insert into especialidades (id_especialidad,nombre,descripcion) values
  (1,'Arquitectura','Diseño arquitectónico y planos'),
  (2,'Estructuras','Cálculo y planos estructurales'),
  (3,'Instalaciones eléctricas','Diseño de instalaciones eléctricas'),
  (4,'Instalaciones sanitarias','Diseño de agua y desagüe'),
  (5,'Topografía','Levantamiento topográfico'),
  (6,'Mecánica de suelos','Estudio geotécnico')
on conflict (id_especialidad) do update set nombre=excluded.nombre,descripcion=excluded.descripcion;
select setval(pg_get_serial_sequence('especialidades','id_especialidad'),greatest((select max(id_especialidad) from especialidades),1));

