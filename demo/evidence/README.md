# Evidencia de demostración

Este directorio contiene evidencia **ficticia** y coherente con los registros de
`database/02_datos_demo_supabase.sql`. No contiene marcas de agua, sellos de una
entidad pública, firmas reales ni datos personales reales.

## Contenido

- `images/`: tres imágenes arquitectónicas generadas para los proyectos demo.
- `pdf/`: nueve documentos de expediente y doce comprobantes de pago.
- `local-manifest.json`: relación entre UUID, archivo local, MIME, tamaño y
  `public_id` previsto.
- `cloudinary-manifest.json`: respuesta de publicación con `secure_url`,
  `public_id`, tipo y tamaño. No contiene credenciales.

## Regenerar y publicar

Desde la raíz del repositorio:

```powershell
python tools/generate_demo_evidence.py
python tools/publish_demo_evidence.py --upload
python tools/publish_demo_evidence.py --verify
```

El publicador lee las credenciales desde `backend/.env`. El valor correcto de
`CLOUDINARY_CLOUD_NAME` es el **Product Environment / Cloud Name**; el nombre
asignado a una API key no es el cloud name.

Para que las URL de PDF sean accesibles en cuentas que bloquean su entrega por
defecto, activar en Cloudinary **Settings → Security → PDF and ZIP files
delivery**. La carga no necesita repetirse después de activar esa opción.

## Enlazar Supabase

Hay dos alternativas equivalentes:

1. Configurar temporalmente `DATABASE_URL`, `DATABASE_USERNAME` y
   `DATABASE_PASSWORD` con la conexión de Supabase y ejecutar:

   ```powershell
   python tools/publish_demo_evidence.py --update-database
   ```

2. Abrir Supabase SQL Editor y ejecutar
   `database/03_enlazar_evidencia_cloudinary.sql`.

Ambas alternativas validan que estén presentes exactamente los registros demo
esperados y solo actualizan sus UUID conocidos. El script principal de carga
demo también contiene estas URL, de modo que una recarga futura queda
reproducible.
