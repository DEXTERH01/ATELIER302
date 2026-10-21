"""Publica la evidencia ficticia en Cloudinary y enlaza los registros demo.

Lee las credenciales exclusivamente de ``backend/.env``. No imprime secretos y
solo actualiza los UUID fijos del conjunto de datos de demostración.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import mimetypes
import os
import re
import ssl
import sys
import time
import uuid
from pathlib import Path
from urllib.parse import urlparse
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

import psycopg


ROOT = Path(__file__).resolve().parents[1]
ENV_PATH = ROOT / "backend" / ".env"
LOCAL_MANIFEST = ROOT / "demo" / "evidence" / "local-manifest.json"
CLOUD_MANIFEST = ROOT / "demo" / "evidence" / "cloudinary-manifest.json"
SEED_PATH = ROOT / "database" / "02_datos_demo_supabase.sql"
UPDATE_SQL_PATH = ROOT / "database" / "03_enlazar_evidencia_cloudinary.sql"
SEED_START = "-- CLOUDINARY_EVIDENCE_START"
SEED_END = "-- CLOUDINARY_EVIDENCE_END"

PROPOSAL_IMAGE_MAP = {
    "70000000-0000-0000-0000-000000000001": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000002": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000003": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000004": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000005": "casa-mirador-rojas.png",
    "70000000-0000-0000-0000-000000000006": "casa-mirador-rojas.png",
    "70000000-0000-0000-0000-000000000007": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000008": "levantamiento-topografico.png",
    "70000000-0000-0000-0000-000000000009": "casa-mirador-rojas.png",
    "70000000-0000-0000-0000-000000000010": "galeria-nova-interior.png",
    "70000000-0000-0000-0000-000000000011": "levantamiento-topografico.png",
}


def load_env(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8-sig").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in {'"', "'"}:
            value = value[1:-1]
        values[key.strip()] = value
    return values


def require(values: dict[str, str], *keys: str) -> None:
    missing = [key for key in keys if not values.get(key)]
    if missing:
        raise RuntimeError(f"Faltan variables requeridas en backend/.env: {', '.join(missing)}")


def multipart_body(fields: dict[str, str], file_path: Path, mime_type: str) -> tuple[bytes, str]:
    boundary = f"----AtelierBoundary{uuid.uuid4().hex}"
    chunks: list[bytes] = []
    for name, value in fields.items():
        chunks.extend(
            [
                f"--{boundary}\r\n".encode(),
                f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode(),
                str(value).encode("utf-8"),
                b"\r\n",
            ]
        )
    chunks.extend(
        [
            f"--{boundary}\r\n".encode(),
            f'Content-Disposition: form-data; name="file"; filename="{file_path.name}"\r\n'.encode(),
            f"Content-Type: {mime_type}\r\n\r\n".encode(),
            file_path.read_bytes(),
            b"\r\n",
            f"--{boundary}--\r\n".encode(),
        ]
    )
    return b"".join(chunks), boundary


def upload_asset(asset: dict, env: dict[str, str]) -> dict:
    file_path = ROOT / asset["local_path"]
    timestamp = str(int(time.time()))
    signed = {
        "invalidate": "true",
        "overwrite": "true",
        "public_id": asset["public_id"],
        "timestamp": timestamp,
        "unique_filename": "false",
    }
    signature_base = "&".join(f"{key}={signed[key]}" for key in sorted(signed))
    signature = hashlib.sha1((signature_base + env["CLOUDINARY_API_SECRET"]).encode()).hexdigest()
    fields = {
        **signed,
        "api_key": env["CLOUDINARY_API_KEY"],
        "signature": signature,
    }
    body, boundary = multipart_body(fields, file_path, asset["mime_type"])
    endpoint = f"https://api.cloudinary.com/v1_1/{env['CLOUDINARY_CLOUD_NAME']}/{asset['resource_type']}/upload"
    request = Request(
        endpoint,
        data=body,
        method="POST",
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}", "User-Agent": "ATELIER-Demo-Evidence/1.0"},
    )
    try:
        with urlopen(request, timeout=90, context=ssl.create_default_context()) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")[:500]
        raise RuntimeError(f"Cloudinary rechazó {file_path.name}: HTTP {exc.code}: {detail}") from exc
    except URLError as exc:
        raise RuntimeError(f"No se pudo conectar con Cloudinary al subir {file_path.name}: {exc.reason}") from exc
    return {
        **asset,
        "secure_url": payload["secure_url"],
        "public_id": payload["public_id"],
        "resource_type": payload["resource_type"],
        "format": payload.get("format", file_path.suffix.lstrip(".")),
        "bytes": payload["bytes"],
        "version": payload.get("version"),
    }


def publish(env: dict[str, str]) -> dict:
    require(env, "CLOUDINARY_CLOUD_NAME", "CLOUDINARY_API_KEY", "CLOUDINARY_API_SECRET")
    manifest = json.loads(LOCAL_MANIFEST.read_text(encoding="utf-8"))
    published: list[dict] = []
    total = len(manifest["assets"])
    for index, asset in enumerate(manifest["assets"], start=1):
        result = upload_asset(asset, env)
        published.append(result)
        print(f"[{index:02d}/{total:02d}] Subido: {Path(asset['local_path']).name}")
    cloud_manifest = {
        "version": 1,
        "cloud_name": env["CLOUDINARY_CLOUD_NAME"],
        "published_at_epoch": int(time.time()),
        "assets": published,
    }
    CLOUD_MANIFEST.write_text(json.dumps(cloud_manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return cloud_manifest


def manifest_indexes(manifest: dict) -> tuple[dict[str, dict], dict[str, dict]]:
    by_record = {asset["record_id"]: asset for asset in manifest["assets"] if asset.get("record_id")}
    by_filename = {Path(asset["local_path"]).name: asset for asset in manifest["assets"]}
    return by_record, by_filename


def database_dsn(env: dict[str, str]) -> tuple[str, dict[str, str]]:
    require(env, "DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD")
    dsn = env["DATABASE_URL"]
    if dsn.startswith("jdbc:"):
        dsn = dsn[5:]
    kwargs = {
        "user": env["DATABASE_USERNAME"],
        "password": env["DATABASE_PASSWORD"],
        "connect_timeout": "15",
    }
    parsed = urlparse(dsn)
    if "sslmode=" not in dsn:
        kwargs["sslmode"] = "disable" if parsed.hostname in {"localhost", "127.0.0.1", "::1"} else "require"
    return dsn, kwargs


def update_database(env: dict[str, str], manifest: dict) -> None:
    by_record, by_filename = manifest_indexes(manifest)
    document_ids = [key for key, asset in by_record.items() if asset["kind"] == "documento"]
    payment_ids = [key for key, asset in by_record.items() if asset["kind"] == "pago"]
    proposal_ids = list(PROPOSAL_IMAGE_MAP)
    dsn, kwargs = database_dsn(env)
    with psycopg.connect(dsn, **kwargs) as connection:
        with connection.cursor() as cursor:
            cursor.execute("select count(*) from documentos where id_documento = any(%s::uuid[])", (document_ids,))
            document_count = cursor.fetchone()[0]
            cursor.execute("select count(*) from pagos where id_pago = any(%s::uuid[])", (payment_ids,))
            payment_count = cursor.fetchone()[0]
            cursor.execute("select count(*) from propuestas where id_propuesta = any(%s::uuid[])", (proposal_ids,))
            proposal_count = cursor.fetchone()[0]
            if (document_count, payment_count, proposal_count) != (9, 12, 11):
                raise RuntimeError(
                    "La base no contiene el conjunto demo esperado "
                    f"(documentos={document_count}/9, pagos={payment_count}/12, propuestas={proposal_count}/11)."
                )
            for record_id in document_ids:
                asset = by_record[record_id]
                cursor.execute(
                    """
                    update documentos
                       set url_cloudinary=%s, public_id_cloudinary=%s, formato='PDF',
                           tamano_bytes=%s, tipo_mime='application/pdf', resource_type='raw'
                     where id_documento=%s::uuid
                    """,
                    (asset["secure_url"], asset["public_id"], asset["bytes"], record_id),
                )
            for record_id in payment_ids:
                asset = by_record[record_id]
                cursor.execute(
                    """
                    update pagos
                       set comprobante_url=%s, comprobante_public_id=%s,
                           comprobante_tipo_mime='application/pdf', comprobante_tamano_bytes=%s,
                           comprobante_categoria='COMPROBANTE_PAGO', comprobante_resource_type='raw'
                     where id_pago=%s::uuid
                    """,
                    (asset["secure_url"], asset["public_id"], asset["bytes"], record_id),
                )
            for proposal_id, filename in PROPOSAL_IMAGE_MAP.items():
                asset = by_filename[filename]
                cursor.execute(
                    """
                    update propuestas
                       set url_vista_previa=%s, vista_previa_public_id=%s,
                           vista_previa_nombre=%s, vista_previa_tipo_mime='image/png',
                           vista_previa_tamano_bytes=%s, vista_previa_resource_type='image'
                     where id_propuesta=%s::uuid
                    """,
                    (asset["secure_url"], asset["public_id"], filename, asset["bytes"], proposal_id),
                )
        connection.commit()
    print("Supabase actualizado: 9 documentos, 12 pagos y 11 vistas previas de propuestas.")


def sql_literal(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def seed_sql(manifest: dict) -> str:
    by_record, by_filename = manifest_indexes(manifest)
    cloud_name = manifest["cloud_name"]
    statements = [SEED_START, "-- Generado por tools/publish_demo_evidence.py; no contiene credenciales."]
    for record_id, asset in by_record.items():
        if asset["kind"] == "documento":
            statements.append(
                "update documentos set "
                f"url_cloudinary={sql_literal(asset['secure_url'])}, "
                f"public_id_cloudinary={sql_literal(asset['public_id'])}, formato='PDF', "
                f"tamano_bytes={asset['bytes']}, tipo_mime='application/pdf', resource_type='raw' "
                f"where id_documento={sql_literal(record_id)}::uuid;"
            )
        elif asset["kind"] == "pago":
            statements.append(
                "update pagos set "
                f"comprobante_url={sql_literal(asset['secure_url'])}, "
                f"comprobante_public_id={sql_literal(asset['public_id'])}, "
                f"comprobante_tipo_mime='application/pdf', comprobante_tamano_bytes={asset['bytes']}, "
                "comprobante_categoria='COMPROBANTE_PAGO', comprobante_resource_type='raw' "
                f"where id_pago={sql_literal(record_id)}::uuid;"
            )
    for proposal_id, filename in PROPOSAL_IMAGE_MAP.items():
        asset = by_filename[filename]
        statements.append(
            "update propuestas set "
            f"url_vista_previa={sql_literal(asset['secure_url'])}, "
            f"vista_previa_public_id={sql_literal(asset['public_id'])}, "
            f"vista_previa_nombre={sql_literal(filename)}, vista_previa_tipo_mime='image/png', "
            f"vista_previa_tamano_bytes={asset['bytes']}, vista_previa_resource_type='image' "
            f"where id_propuesta={sql_literal(proposal_id)}::uuid;"
        )
    statements.extend(
        [
            "do $$ begin",
            "  if (",
            "    select count(*) from documentos",
            "    where id_documento::text like '51000000-0000-0000-0000-%'",
            f"      and url_cloudinary like 'https://res.cloudinary.com/{cloud_name}/raw/upload/%'",
            "      and public_id_cloudinary like 'atelier/demo/evidencias/documentos/%.pdf'",
            "      and tipo_mime = 'application/pdf'",
            "      and resource_type = 'raw'",
            "  ) <> 9 then",
            "    raise exception 'Evidencia Cloudinary incompleta o inválida en documentos demo';",
            "  end if;",
            "end $$;",
            SEED_END,
        ]
    )
    return "\n".join(statements)


def patch_seed(manifest: dict) -> None:
    source = SEED_PATH.read_text(encoding="utf-8")
    block = seed_sql(manifest)
    if SEED_START in source:
        source = re.sub(
            re.escape(SEED_START) + r".*?" + re.escape(SEED_END),
            block,
            source,
            flags=re.DOTALL,
        )
    else:
        marker = "\ncommit;"
        if marker not in source:
            raise RuntimeError("No se encontró el COMMIT final del script demo.")
        source = source.replace(marker, f"\n\n{block}\n{marker}", 1)
    SEED_PATH.write_text(source, encoding="utf-8", newline="\n")
    print("Script demo actualizado con las URLs publicadas en Cloudinary.")


def write_update_sql(manifest: dict) -> None:
    block = seed_sql(manifest)
    preflight = """do $$
declare
  documentos_demo integer;
  pagos_demo integer;
  propuestas_demo integer;
begin
  select count(*) into documentos_demo from documentos where id_documento::text like '51000000-0000-0000-0000-%';
  select count(*) into pagos_demo from pagos where id_pago::text like '42000000-0000-0000-0000-%';
  select count(*) into propuestas_demo from propuestas where id_propuesta::text like '70000000-0000-0000-0000-%';
  if documentos_demo <> 9 or pagos_demo <> 12 or propuestas_demo <> 11 then
    raise exception 'Conjunto demo inesperado: documentos %, pagos %, propuestas %', documentos_demo, pagos_demo, propuestas_demo;
  end if;
end $$;"""
    contents = (
        "-- Enlaza exclusivamente la evidencia ficticia del conjunto demo.\n"
        "-- No contiene credenciales y puede ejecutarse en Supabase SQL Editor.\n\n"
        "begin;\n\n"
        f"{preflight}\n\n"
        f"{block}\n\n"
        "commit;\n"
    )
    UPDATE_SQL_PATH.write_text(contents, encoding="utf-8", newline="\n")
    print(f"SQL de enlace creado: {UPDATE_SQL_PATH}")


def verify_cloud_assets(manifest: dict) -> None:
    failures: list[str] = []
    for index, asset in enumerate(manifest["assets"], start=1):
        request = Request(
            asset["secure_url"],
            method="GET",
            headers={"Range": "bytes=0-63", "User-Agent": "ATELIER-Demo-Evidence/1.0"},
        )
        try:
            with urlopen(request, timeout=30, context=ssl.create_default_context()) as response:
                received = response.read(64)
                content_type = response.headers.get_content_type()
                if not received:
                    failures.append(f"{Path(asset['local_path']).name}: respuesta vacía")
                elif asset["mime_type"] == "application/pdf" and not received.startswith(b"%PDF"):
                    failures.append(f"{Path(asset['local_path']).name}: firma PDF inválida")
                elif asset["mime_type"] == "image/png" and not received.startswith(b"\x89PNG"):
                    failures.append(f"{Path(asset['local_path']).name}: firma PNG inválida")
                elif content_type not in {asset["mime_type"], "application/octet-stream"}:
                    failures.append(f"{Path(asset['local_path']).name}: MIME inesperado {content_type}")
        except Exception as exc:
            failures.append(f"{Path(asset['local_path']).name}: {exc}")
        print(f"[{index:02d}/{len(manifest['assets']):02d}] Verificado: {Path(asset['local_path']).name}")
    if failures:
        raise RuntimeError("Falló la verificación de recursos:\n- " + "\n- ".join(failures))
    print(f"Cloudinary verificado: {len(manifest['assets'])} recursos accesibles y con firma válida.")


def load_cloud_manifest() -> dict:
    if not CLOUD_MANIFEST.exists():
        raise RuntimeError("No existe cloudinary-manifest.json; ejecuta primero con --upload.")
    return json.loads(CLOUD_MANIFEST.read_text(encoding="utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--upload", action="store_true", help="Sube los 24 recursos a Cloudinary")
    parser.add_argument("--update-database", action="store_true", help="Actualiza los UUID demo en Supabase")
    parser.add_argument("--patch-seed", action="store_true", help="Actualiza el SQL demo con las URLs publicadas")
    parser.add_argument("--verify", action="store_true", help="Verifica URL, MIME y firma binaria de cada recurso")
    parser.add_argument("--write-update-sql", action="store_true", help="Genera un SQL transaccional para Supabase SQL Editor")
    args = parser.parse_args()
    if not (args.upload or args.update_database or args.patch_seed or args.verify or args.write_update_sql):
        parser.error("selecciona al menos una operación")
    env = load_env(ENV_PATH)
    manifest = publish(env) if args.upload else load_cloud_manifest()
    if args.update_database:
        update_database(env, manifest)
    if args.patch_seed:
        patch_seed(manifest)
    if args.verify:
        verify_cloud_assets(manifest)
    if args.write_update_sql:
        write_update_sql(manifest)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
