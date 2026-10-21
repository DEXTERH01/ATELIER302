"""Genera evidencia documental ficticia para la demostración de ATELIER 302.

Los archivos no incluyen marcas de agua, sellos oficiales ni firmas reales. Los
identificadores y contenidos corresponden exclusivamente al conjunto de datos
simulados de ``database/02_datos_demo_supabase.sql``.
"""

from __future__ import annotations

import json
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    HRFlowable,
    Image,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "demo" / "evidence" / "pdf"
IMAGE_DIR = ROOT / "demo" / "evidence" / "images"
MANIFEST_PATH = ROOT / "demo" / "evidence" / "local-manifest.json"

IVORY = colors.HexColor("#F6F1E8")
GRAPHITE = colors.HexColor("#20211F")
TERRACOTTA = colors.HexColor("#994B37")
WARM_GRAY = colors.HexColor("#74716B")
LIGHT_GRAY = colors.HexColor("#E7E2D9")
PALE_BLUE = colors.HexColor("#DDEBF0")
WHITE = colors.white


STYLES = getSampleStyleSheet()
STYLES.add(
    ParagraphStyle(
        name="AtelierTitle",
        parent=STYLES["Title"],
        fontName="Helvetica-Bold",
        fontSize=22,
        leading=26,
        textColor=GRAPHITE,
        spaceAfter=6 * mm,
    )
)
STYLES.add(
    ParagraphStyle(
        name="AtelierSubtitle",
        parent=STYLES["Normal"],
        fontName="Helvetica",
        fontSize=10,
        leading=14,
        textColor=WARM_GRAY,
        spaceAfter=5 * mm,
    )
)
STYLES.add(
    ParagraphStyle(
        name="AtelierHeading",
        parent=STYLES["Heading2"],
        fontName="Helvetica-Bold",
        fontSize=12,
        leading=15,
        textColor=TERRACOTTA,
        spaceBefore=3 * mm,
        spaceAfter=2.5 * mm,
    )
)
STYLES.add(
    ParagraphStyle(
        name="AtelierBody",
        parent=STYLES["BodyText"],
        fontName="Helvetica",
        fontSize=9.3,
        leading=14,
        textColor=GRAPHITE,
        alignment=TA_LEFT,
        spaceAfter=2.5 * mm,
    )
)
STYLES.add(
    ParagraphStyle(
        name="AtelierSmall",
        parent=STYLES["BodyText"],
        fontName="Helvetica",
        fontSize=7.6,
        leading=10,
        textColor=WARM_GRAY,
    )
)
STYLES.add(
    ParagraphStyle(
        name="AtelierAmount",
        parent=STYLES["Title"],
        fontName="Helvetica-Bold",
        fontSize=25,
        leading=28,
        alignment=TA_RIGHT,
        textColor=TERRACOTTA,
    )
)


def _header_footer(canvas, doc) -> None:
    width, height = A4
    canvas.saveState()
    canvas.setFillColor(IVORY)
    canvas.rect(0, height - 24 * mm, width, 24 * mm, fill=1, stroke=0)
    canvas.setFillColor(GRAPHITE)
    canvas.setFont("Helvetica-Bold", 15)
    canvas.drawString(18 * mm, height - 14 * mm, "ATELIER 302")
    canvas.setFont("Helvetica", 7.2)
    canvas.setFillColor(WARM_GRAY)
    canvas.drawString(18 * mm, height - 19 * mm, "ARQUITECTOS Y ASOCIADOS")
    canvas.setFillColor(TERRACOTTA)
    canvas.rect(width - 30 * mm, height - 17 * mm, 4 * mm, 4 * mm, fill=1, stroke=0)
    canvas.setFillColor(colors.HexColor("#E0B32D"))
    canvas.circle(width - 21 * mm, height - 15 * mm, 2 * mm, fill=1, stroke=0)
    canvas.setFillColor(colors.HexColor("#7CAFC4"))
    p = canvas.beginPath()
    p.moveTo(width - 13 * mm, height - 17 * mm)
    p.lineTo(width - 9 * mm, height - 13 * mm)
    p.lineTo(width - 5 * mm, height - 17 * mm)
    p.close()
    canvas.drawPath(p, fill=1, stroke=0)

    canvas.setStrokeColor(LIGHT_GRAY)
    canvas.line(18 * mm, 16 * mm, width - 18 * mm, 16 * mm)
    canvas.setFont("Helvetica", 7)
    canvas.setFillColor(WARM_GRAY)
    canvas.drawString(18 * mm, 10 * mm, "ATELIER 302 · Gestión Digital de Proyectos")
    canvas.drawRightString(width - 18 * mm, 10 * mm, f"Página {doc.page}")
    canvas.restoreState()


def _document(path: Path, title: str, subtitle: str, story: list, metadata: dict) -> None:
    doc = SimpleDocTemplate(
        str(path),
        pagesize=A4,
        rightMargin=18 * mm,
        leftMargin=18 * mm,
        topMargin=32 * mm,
        bottomMargin=22 * mm,
        title=title,
        author="ATELIER 302 Arquitectos y Asociados",
        subject=subtitle,
        creator="ATELIER Gestión Digital",
    )
    lead = [
        Paragraph(title, STYLES["AtelierTitle"]),
        Paragraph(subtitle, STYLES["AtelierSubtitle"]),
        _key_value_table(metadata),
        Spacer(1, 4 * mm),
        HRFlowable(width="100%", thickness=1.2, color=TERRACOTTA),
        Spacer(1, 3 * mm),
    ]
    doc.build(lead + story, onFirstPage=_header_footer, onLaterPages=_header_footer)


def _key_value_table(values: dict[str, str]) -> Table:
    rows = []
    items = list(values.items())
    for index in range(0, len(items), 2):
        left = items[index]
        right = items[index + 1] if index + 1 < len(items) else ("", "")
        rows.append(
            [
                Paragraph(f"<b>{left[0]}</b>", STYLES["AtelierSmall"]),
                Paragraph(str(left[1]), STYLES["AtelierBody"]),
                Paragraph(f"<b>{right[0]}</b>" if right[0] else "", STYLES["AtelierSmall"]),
                Paragraph(str(right[1]), STYLES["AtelierBody"]),
            ]
        )
    table = Table(rows, colWidths=[29 * mm, 55 * mm, 29 * mm, 55 * mm])
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), IVORY),
                ("BOX", (0, 0), (-1, -1), 0.4, LIGHT_GRAY),
                ("INNERGRID", (0, 0), (-1, -1), 0.3, LIGHT_GRAY),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
            ]
        )
    )
    return table


def _section(title: str, paragraphs: list[str]) -> list:
    items = [Paragraph(title, STYLES["AtelierHeading"])]
    items.extend(Paragraph(text, STYLES["AtelierBody"]) for text in paragraphs)
    return items


def _data_table(headers: list[str], rows: list[list[str]], widths: list[float] | None = None) -> Table:
    data = [[Paragraph(f"<b>{h}</b>", STYLES["AtelierSmall"]) for h in headers]]
    data += [[Paragraph(str(cell), STYLES["AtelierSmall"]) for cell in row] for row in rows]
    table = Table(data, colWidths=widths, repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), GRAPHITE),
                ("TEXTCOLOR", (0, 0), (-1, 0), WHITE),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, IVORY]),
                ("GRID", (0, 0), (-1, -1), 0.35, LIGHT_GRAY),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def _photo(path: Path, caption: str, width: float = 168 * mm, height: float = 87 * mm) -> list:
    image = Image(str(path), width=width, height=height, kind="proportional")
    caption_p = Paragraph(caption, STYLES["AtelierSmall"])
    return [KeepTogether([image, Spacer(1, 1.5 * mm), caption_p]), Spacer(1, 3 * mm)]


DOCUMENTS = [
    {
        "id": "51000000-0000-0000-0000-000000000001",
        "filename": "doc-001-levantamiento-topografico.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-001-levantamiento-topografico.pdf",
        "title": "Informe de levantamiento topográfico",
        "subtitle": "Avance técnico para el Centro Empresarial Arce",
        "metadata": {"Proyecto": "PROY-2026-0008", "Expediente": "EXP-2026-0008", "Ubicación": "San Isidro, Lima", "Fecha": "20 de octubre de 2026"},
        "photo": "levantamiento-topografico.png",
        "caption": "Registro de campo: estación total y puntos de control del lote.",
        "sections": [
            ("Objeto del estudio", ["Registrar la geometría del predio, sus niveles, linderos visibles y elementos urbanos relevantes para el desarrollo del anteproyecto del Centro Empresarial Arce."]),
            ("Metodología", ["Se estableció una poligonal de apoyo y se levantaron puntos mediante estación total. Las cotas se refirieron a un banco de nivel temporal protegido dentro del lote."]),
            ("Resultados preliminares", ["El terreno presenta una pendiente media aproximada de 1.8 % hacia el frente. Se identificaron vereda, sardineles, cajas de servicio y dos interferencias menores que deberán coordinarse durante el expediente técnico."]),
            ("Conclusión", ["La información obtenida es suficiente para continuar con la compatibilización arquitectónica. El plano topográfico definitivo incorporará cuadro de coordenadas, curvas de nivel y secciones longitudinales."]),
        ],
    },
    {
        "id": "51000000-0000-0000-0000-000000000002",
        "filename": "doc-002-mecanica-suelos.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-002-mecanica-suelos.pdf",
        "title": "Informe de mecánica de suelos",
        "subtitle": "Vivienda Bioclimática Villanueva · evaluación geotécnica",
        "metadata": {"Proyecto": "PROY-2026-0009", "Expediente": "EXP-2026-0009", "Ubicación": "Pachacámac, Lima", "Fecha": "7 de septiembre de 2026"},
        "sections": [
            ("Alcance", ["Evaluación referencial del terreno para definir criterios preliminares de cimentación de una vivienda unifamiliar de dos niveles."]),
            ("Exploración", ["Se consideraron tres calicatas distribuidas en el área de ocupación. El perfil predominante corresponde a arena limosa medianamente densa, sin evidencia de rellenos orgánicos."]),
            ("Parámetros de diseño", ["Para el desarrollo académico del proyecto se adopta una capacidad portante admisible de 1.50 kg/cm² y profundidad mínima de cimentación de 1.20 m respecto del nivel de terreno terminado."]),
            ("Recomendaciones", ["Emplear cimientos corridos conectados, verificar niveles antes del vaciado y mantener el fondo de excavación libre de material suelto. El diseño estructural definitivo deberá confirmar estos criterios."]),
        ],
        "table": (["Calicata", "Profundidad", "Estrato predominante", "Observación"], [["C-01", "2.50 m", "Arena limosa", "Densidad media"], ["C-02", "2.80 m", "Arena limosa", "Sin nivel freático"], ["C-03", "2.50 m", "Arena fina", "Compacta desde 1.40 m"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000003",
        "filename": "doc-003-anteproyecto-galeria-nova-v4.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-003-anteproyecto-galeria-nova-v4.pdf",
        "title": "Anteproyecto Galería Nova · versión 4",
        "subtitle": "Síntesis de propuesta arquitectónica para revisión del cliente",
        "metadata": {"Proyecto": "PROY-2026-0010", "Expediente": "EXP-2026-0010", "Área": "420.00 m²", "Fecha de envío": "2 de octubre de 2026"},
        "photo": "galeria-nova-interior.png",
        "caption": "Vista conceptual del vestíbulo y conexión con el patio gastronómico.",
        "photo_height_mm": 52,
        "sections": [
            ("Partido arquitectónico", ["La propuesta organiza doce locales alrededor de un recorrido continuo que culmina en un patio gastronómico iluminado naturalmente. El acceso principal funciona como espacio de transición y orientación."]),
            ("Cambios incorporados", ["Se amplió el patio central, se reubicaron dos locales para mejorar su frente comercial y se desarrolló una nueva alternativa de fachada correspondiente a la ronda adicional aprobada."]),
            ("Materialidad", ["Se propone una base de concreto visto y superficies marfil, carpinterías grafito, madera cálida y acentos terracota coherentes con la identidad de ATELIER 302."]),
        ],
        "table": (["Zona", "Cantidad", "Área estimada", "Criterio"], [["Locales comerciales", "12", "222 m²", "Flexibilidad modular"], ["Patio gastronómico", "1", "96 m²", "Ventilación natural"], ["Servicios comunes", "2 núcleos", "42 m²", "Accesibilidad"], ["Circulación", "—", "60 m²", "Recorrido legible"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000004",
        "filename": "doc-004-proyecto-final-casa-mirador.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-004-proyecto-final-casa-mirador.pdf",
        "title": "Proyecto final Casa Mirador Rojas",
        "subtitle": "Memoria descriptiva y cuadro general de entregables",
        "metadata": {"Proyecto": "PROY-2026-0011", "Expediente": "EXP-2026-0011", "Ubicación": "Cieneguilla, Lima", "Conformidad": "5 de octubre de 2026"},
        "photo": "casa-mirador-rojas.png",
        "caption": "Vista exterior referencial de la vivienda y su terraza panorámica.",
        "photo_height_mm": 52,
        "sections": [
            ("Concepto", ["La vivienda se implanta en dos niveles y orienta sus áreas sociales hacia el valle. Los volúmenes protegen las terrazas del asoleamiento directo y articulan espacios interiores con patios ajardinados."]),
            ("Solución funcional", ["El primer nivel reúne sala, comedor, cocina, terraza y ambientes de servicio. El segundo nivel contiene dormitorios y una sala familiar conectada al mirador."]),
            ("Coordinación", ["La propuesta final integra arquitectura, estructuras, instalaciones sanitarias y eléctricas. Los planos han sido compatibilizados para evitar interferencias en los principales pases verticales."]),
        ],
        "table": (["Entregable", "Código", "Estado"], [["Planos de arquitectura", "A-01 a A-12", "Conforme"], ["Planos estructurales", "E-01 a E-08", "Coordinado"], ["Instalaciones sanitarias", "IS-01 a IS-06", "Coordinado"], ["Instalaciones eléctricas", "IE-01 a IE-07", "Coordinado"], ["Memoria y especificaciones", "MD-01", "Conforme"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000005",
        "filename": "doc-005-cargo-ingreso-municipal.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-005-cargo-ingreso-municipal.pdf",
        "title": "Cargo de ingreso documental",
        "subtitle": "Licencia de edificación · Edificio Quinteros",
        "metadata": {"Proyecto": "PROY-2026-0012", "Expediente interno": "EXP-2026-0012", "Expediente externo": "MPL-DEMO-2026-0142", "Fecha": "1 de octubre de 2026"},
        "sections": [
            ("Constancia de presentación", ["Se deja constancia del ingreso del expediente para evaluación de licencia de edificación de un edificio multifamiliar de cinco niveles, ubicado en Pueblo Libre, Lima."]),
            ("Documentación presentada", ["Formulario de solicitud, memoria descriptiva, planos de arquitectura, estructuras e instalaciones, cuadro de áreas, documentación de propiedad y anexos técnicos."]),
            ("Seguimiento", ["Estado inicial: EN REVISIÓN. Responsable de seguimiento en ATELIER 302: equipo de coordinación de proyectos. La recepción del expediente no implica aprobación técnica."]),
        ],
        "table": (["Ítem", "Documento", "Folios", "Resultado"], [["01", "Solicitud y anexos", "12", "Recibido"], ["02", "Memoria descriptiva", "18", "Recibido"], ["03", "Planos de especialidades", "24", "Recibido"], ["04", "Sustentos complementarios", "9", "Recibido"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000006",
        "filename": "doc-006-notificacion-observaciones.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-006-notificacion-observaciones.pdf",
        "title": "Notificación de observaciones técnicas",
        "subtitle": "Licencia de edificación industrial · Almacén Arce",
        "metadata": {"Proyecto": "PROY-2026-0013", "Expediente": "MLU-DEMO-2026-0088", "Fecha de notificación": "10 de octubre de 2026", "Fecha límite": "30 de octubre de 2026"},
        "sections": [
            ("Resultado de revisión", ["Durante la evaluación del expediente se identificaron aspectos subsanables en la documentación de seguridad y en la consistencia del cuadro de áreas."]),
            ("Observación principal", ["Actualizar el plano de seguridad incorporando rutas de evacuación, señalización, punto de reunión y correspondencia con el aforo. Rectificar el cuadro de áreas para que coincida con las láminas de arquitectura."]),
            ("Acción requerida", ["Presentar lámina de seguridad revisada, cuadro de áreas consolidado y carta de respuesta que identifique cada modificación. El proyecto permanece en estado CON OBSERVACIONES hasta la revisión de la subsanación."]),
        ],
        "table": (["N.º", "Especialidad", "Observación", "Prioridad"], [["01", "Seguridad", "Completar rutas y señalización", "Alta"], ["02", "Arquitectura", "Conciliar cuadro de áreas", "Alta"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000007",
        "filename": "doc-007-resolucion-licencia.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-007-resolucion-licencia.pdf",
        "title": "Resolución de aprobación de licencia",
        "subtitle": "Remodelación Casa Taller Villanueva",
        "metadata": {"Proyecto": "PROY-2026-0014", "Expediente": "MLM-DEMO-2026-0194", "Tipo": "Licencia de remodelación", "Fecha": "22 de septiembre de 2026"},
        "sections": [
            ("Antecedentes", ["Se evaluó el expediente técnico presentado para la remodelación de una vivienda con taller artístico e ingreso independiente, incluyendo la subsanación de la conexión sanitaria del primer nivel."]),
            ("Evaluación", ["Los documentos revisados guardan correspondencia con el alcance declarado y con el cuadro de áreas del proyecto. La observación sanitaria fue atendida mediante el detalle IS-04 y la memoria corregida."]),
            ("Decisión", ["Se registra la aprobación del expediente para fines de la simulación funcional del sistema ATELIER Gestión Digital. El proyecto pasa al estado APROBADO y queda disponible para seguimiento y cierre."]),
            ("Condiciones", ["La ejecución deberá respetar los documentos aprobados. Toda modificación sustancial debe registrarse y evaluarse antes de su implementación."]),
        ],
    },
    {
        "id": "51000000-0000-0000-0000-000000000008",
        "filename": "doc-008-acta-cierre-proyecto.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-008-acta-cierre-proyecto.pdf",
        "title": "Acta de cierre de proyecto",
        "subtitle": "Tienda Nova · cierre administrativo y documental",
        "metadata": {"Proyecto": "PROY-2026-0015", "Expediente": "EXP-2026-0015", "Ubicación": "San Borja, Lima", "Fecha de cierre": "12 de mayo de 2026"},
        "sections": [
            ("Resumen", ["El servicio de diseño interior y supervisión de implementación concluyó con la entrega de planos finales, especificaciones, registro de cambios y archivo fotográfico organizado."]),
            ("Verificación de cierre", ["Los entregables contractuales fueron registrados en el expediente, las observaciones quedaron atendidas y no existen tareas pendientes asignadas al equipo del proyecto."]),
            ("Resultado", ["Se dispone el cierre administrativo del proyecto Tienda Nova. El expediente permanece conservado en el sistema para consulta, trazabilidad y auditoría."]),
        ],
        "table": (["Control", "Resultado"], [["Entregables finales", "Completos"], ["Observaciones del cliente", "Atendidas"], ["Tareas del equipo", "Cerradas"], ["Archivo del expediente", "Conservado"]]),
    },
    {
        "id": "51000000-0000-0000-0000-000000000009",
        "filename": "doc-009-informe-no-viabilidad.pdf",
        "public_id": "atelier/demo/evidencias/documentos/doc-009-informe-no-viabilidad.pdf",
        "title": "Informe de no viabilidad",
        "subtitle": "Condominio Los Olivos · compatibilidad preliminar de uso",
        "metadata": {"Proyecto": "PROY-2026-0016", "Expediente": "MLO-DEMO-2026-0031", "Ubicación": "Los Olivos, Lima", "Fecha": "14 de julio de 2026"},
        "sections": [
            ("Objeto", ["Evaluar de manera preliminar la compatibilidad del conjunto residencial propuesto con las condiciones de acceso, zonificación y configuración del lote."]),
            ("Hallazgos", ["El uso solicitado no resulta compatible con la condición urbanística considerada en la simulación. Asimismo, el frente operativo y el acceso existente no permiten resolver adecuadamente el ingreso vehicular previsto."]),
            ("Conclusión", ["No se recomienda continuar con el alcance planteado. El sistema registra el proyecto como NO VIABLE y conserva los antecedentes para futuras alternativas de menor intensidad."]),
            ("Alternativas", ["Reformular el programa, reducir el número de unidades o evaluar un predio con acceso y parámetros compatibles antes de iniciar un nuevo expediente."]),
        ],
    },
]


PAYMENTS = [
    ("001", "42000000-0000-0000-0000-000000000001", "PROY-2026-0006", "Minidepartamentos Rojas", "COT-2026-0004", "8,850.00", "TRANSFERENCIA", "OP-DEMO-2026-0001", "24/09/2026 09:20", "PENDIENTE"),
    ("002", "42000000-0000-0000-0000-000000000002", "PROY-2026-0007", "Ampliación Casa Quinteros", "COT-2026-0005", "5,428.00", "TRANSFERENCIA", "OP-DEMO-2026-0002", "16/09/2026 10:00", "VALIDADO"),
    ("003", "42000000-0000-0000-0000-000000000003", "PROY-2026-0008", "Centro Empresarial Arce", "COT-2026-0006", "7,080.00", "DEPÓSITO", "OP-DEMO-2026-0003", "31/08/2026 09:00", "VALIDADO"),
    ("004", "42000000-0000-0000-0000-000000000004", "PROY-2026-0009", "Vivienda Bioclimática Villanueva", "COT-2026-0007", "6,195.00", "TRANSFERENCIA", "OP-DEMO-2026-0004", "14/08/2026 08:40", "VALIDADO"),
    ("005", "42000000-0000-0000-0000-000000000005", "PROY-2026-0010", "Galería Nova", "COT-2026-0008", "9,735.00", "TRANSFERENCIA", "OP-DEMO-2026-0005", "27/07/2026 10:00", "VALIDADO"),
    ("006", "42000000-0000-0000-0000-000000000006", "PROY-2026-0011", "Casa Mirador Rojas", "COT-2026-0009", "8,260.00", "DEPÓSITO", "OP-DEMO-2026-0006", "22/06/2026 09:10", "VALIDADO"),
    ("007", "42000000-0000-0000-0000-000000000007", "PROY-2026-0012", "Edificio Quinteros", "COT-2026-0010", "10,915.00", "TRANSFERENCIA", "OP-DEMO-2026-0007", "20/05/2026 08:30", "VALIDADO"),
    ("008", "42000000-0000-0000-0000-000000000008", "PROY-2026-0013", "Almacén Arce", "COT-2026-0011", "12,980.00", "TRANSFERENCIA", "OP-DEMO-2026-0008", "17/04/2026 09:00", "VALIDADO"),
    ("009", "42000000-0000-0000-0000-000000000009", "PROY-2026-0014", "Casa Taller Villanueva", "COT-2026-0012", "6,490.00", "TRANSFERENCIA", "OP-DEMO-2026-0009", "12/03/2026 09:30", "VALIDADO"),
    ("010", "42000000-0000-0000-0000-000000000010", "PROY-2026-0015", "Tienda Nova", "COT-2026-0013", "4,425.00", "TARJETA", "OP-DEMO-2026-0010", "20/01/2026 11:00", "VALIDADO"),
    ("011", "42000000-0000-0000-0000-000000000011", "PROY-2026-0016", "Condominio Los Olivos", "COT-2026-0014", "5,782.00", "TRANSFERENCIA", "OP-DEMO-2026-0011", "07/02/2026 10:00", "VALIDADO"),
    ("012", "42000000-0000-0000-0000-000000000012", "PROY-2026-0010", "Galería Nova", "COT-2026-0008-A", "2,124.00", "TRANSFERENCIA", "OP-DEMO-2026-0012", "19/09/2026 09:00", "VALIDADO"),
]


def generate_document(item: dict) -> dict:
    story: list = []
    if item.get("photo"):
        story.extend(
            _photo(
                IMAGE_DIR / item["photo"],
                item["caption"],
                height=item.get("photo_height_mm", 87) * mm,
            )
        )
    for heading, paragraphs in item["sections"]:
        story.extend(_section(heading, paragraphs))
    if item.get("table"):
        headers, rows = item["table"]
        story += [Spacer(1, 2 * mm), _data_table(headers, rows)]
    path = OUTPUT_DIR / item["filename"]
    _document(path, item["title"], item["subtitle"], story, item["metadata"])
    return {
        "kind": "documento",
        "record_id": item["id"],
        "local_path": str(path.relative_to(ROOT)).replace("\\", "/"),
        "public_id": item["public_id"],
        "mime_type": "application/pdf",
        "resource_type": "raw",
        "bytes": path.stat().st_size,
    }


def generate_payment(row: tuple[str, ...]) -> dict:
    number, record_id, project_code, project_name, quote, amount, method, operation, paid_at, status = row
    title = f"Comprobante de pago {number}"
    subtitle = f"{project_code} · {project_name}"
    status_color = colors.HexColor("#2F6B4F") if status == "VALIDADO" else colors.HexColor("#9B6B18")
    status_card = Table(
        [
            [Paragraph("IMPORTE REGISTRADO", STYLES["AtelierSmall"]), Paragraph("ESTADO", STYLES["AtelierSmall"])],
            [Paragraph(f"S/ {amount}", STYLES["AtelierAmount"]), Paragraph(f"<b>{status}</b>", ParagraphStyle("status", parent=STYLES["AtelierBody"], fontSize=13, textColor=status_color, alignment=TA_RIGHT))],
        ],
        colWidths=[92 * mm, 76 * mm],
    )
    status_card.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), IVORY), ("BOX", (0, 0), (-1, -1), 0.6, LIGHT_GRAY), ("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("LEFTPADDING", (0, 0), (-1, -1), 10), ("RIGHTPADDING", (0, 0), (-1, -1), 10), ("TOPPADDING", (0, 0), (-1, -1), 9), ("BOTTOMPADDING", (0, 0), (-1, -1), 9)]))
    story = [
        status_card,
        Spacer(1, 8 * mm),
        Paragraph("Detalle de la operación", STYLES["AtelierHeading"]),
        _data_table(["Concepto", "Dato registrado"], [["Tipo de pago", "Adelanto" if number != "012" else "Ronda adicional de diseño"], ["Método", method], ["Número de operación", operation], ["Fecha y hora", paid_at], ["Moneda", "PEN · Sol peruano"], ["Cotización relacionada", quote]], [57 * mm, 111 * mm]),
        Spacer(1, 8 * mm),
        Paragraph("Trazabilidad", STYLES["AtelierHeading"]),
        Paragraph("Este comprobante se conserva asociado a la cotización y al proyecto indicados. Su estado refleja el registro de la plataforma; la validación administrativa se realiza por un usuario autorizado.", STYLES["AtelierBody"]),
        Spacer(1, 6 * mm),
        Table([[Paragraph("Documento generado para el expediente digital del proyecto.", STYLES["AtelierSmall"])]], colWidths=[168 * mm], style=TableStyle([("BACKGROUND", (0, 0), (-1, -1), PALE_BLUE), ("BOX", (0, 0), (-1, -1), 0.4, colors.HexColor("#A9C7D2")), ("LEFTPADDING", (0, 0), (-1, -1), 8), ("RIGHTPADDING", (0, 0), (-1, -1), 8), ("TOPPADDING", (0, 0), (-1, -1), 8), ("BOTTOMPADDING", (0, 0), (-1, -1), 8)])),
    ]
    filename = f"pago-{number}-comprobante.pdf"
    path = OUTPUT_DIR / filename
    _document(path, title, subtitle, story, {"Proyecto": project_code, "Cotización": quote, "Operación": operation, "Registro": paid_at})
    return {
        "kind": "pago",
        "record_id": record_id,
        "local_path": str(path.relative_to(ROOT)).replace("\\", "/"),
        "public_id": f"atelier/demo/evidencias/comprobantes/{filename}",
        "mime_type": "application/pdf",
        "resource_type": "raw",
        "bytes": path.stat().st_size,
    }


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    assets = [generate_document(item) for item in DOCUMENTS]
    assets.extend(generate_payment(row) for row in PAYMENTS)
    image_assets = [
        ("casa-mirador-rojas.png", "atelier/demo/evidencias/imagenes/casa-mirador-rojas"),
        ("levantamiento-topografico.png", "atelier/demo/evidencias/imagenes/levantamiento-topografico"),
        ("galeria-nova-interior.png", "atelier/demo/evidencias/imagenes/galeria-nova-interior"),
    ]
    for filename, public_id in image_assets:
        path = IMAGE_DIR / filename
        assets.append({"kind": "imagen", "record_id": None, "local_path": str(path.relative_to(ROOT)).replace("\\", "/"), "public_id": public_id, "mime_type": "image/png", "resource_type": "image", "bytes": path.stat().st_size})
    MANIFEST_PATH.write_text(json.dumps({"version": 1, "assets": assets}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(DOCUMENTS) + len(PAYMENTS)} PDF files and manifest {MANIFEST_PATH}")


if __name__ == "__main__":
    main()
