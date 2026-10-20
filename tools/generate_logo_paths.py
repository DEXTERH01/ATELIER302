"""Genera contornos SVG inmutables para el wordmark corporativo."""

from pathlib import Path
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.misc.transform import Transform
from fontTools.ttLib import TTFont


def outline(font_path: str, text: str, tracking: int = 0) -> tuple[str, int, int]:
    font = TTFont(font_path)
    glyphs = font.getGlyphSet()
    cmap = font.getBestCmap()
    metrics = font["hmtx"].metrics
    cursor = 0
    commands: list[str] = []
    for character in text:
        glyph_name = cmap[ord(character)]
        pen = SVGPathPen(glyphs)
        transformed = TransformPen(pen, Transform(1, 0, 0, 1, cursor, 0))
        glyphs[glyph_name].draw(transformed)
        commands.append(pen.getCommands())
        cursor += metrics[glyph_name][0] + tracking
    return "".join(commands), cursor - tracking, font["head"].unitsPerEm


wordmark, wordmark_advance, wordmark_upem = outline(
    r"C:\Windows\Fonts\BOD_B.TTF", "Atelier302"
)
descriptor, descriptor_advance, descriptor_upem = outline(
    r"C:\Windows\Fonts\AGENCYR.TTF", "ARQUITECTOS Y ASOCIADOS", tracking=300
)

output = Path(__file__).parents[1] / "frontend/src/components/brand/AtelierLogoPaths.ts"
output.write_text(
    "// Generado por tools/generate_logo_paths.py. No editar manualmente.\n"
    f"export const WORDMARK_PATH={wordmark!r};\n"
    f"export const WORDMARK_ADVANCE={wordmark_advance};\n"
    f"export const WORDMARK_UPEM={wordmark_upem};\n"
    f"export const DESCRIPTOR_PATH={descriptor!r};\n"
    f"export const DESCRIPTOR_ADVANCE={descriptor_advance};\n"
    f"export const DESCRIPTOR_UPEM={descriptor_upem};\n",
    encoding="utf-8",
)
