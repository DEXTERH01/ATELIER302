import type { SVGProps } from 'react';

type AtelierLogoProps = SVGProps<SVGSVGElement> & { title?: string };

/**
 * Recreación vectorial del identificador corporativo entregado por ATELIER 302.
 * Conserva el lienzo y las proporciones 1536 × 494 del arte original.
 */
export function AtelierLogo({ title = 'Atelier 302 Arquitectos y Asociados', ...props }: AtelierLogoProps) {
  return (
    <svg
      viewBox="0 0 1536 494"
      role="img"
      aria-label={title}
      preserveAspectRatio="xMidYMid meet"
      {...props}
    >
      <title>{title}</title>
      <text
        x="154"
        y="263"
        fill="#050505"
        fontFamily="Bodoni MT, Didot, Bodoni 72, Times New Roman, serif"
        fontSize="215"
        fontWeight="700"
        letterSpacing="-10"
        textLength="1290"
        lengthAdjust="spacingAndGlyphs"
      >
        Atelier302
      </text>
      <rect x="0" y="263" width="1536" height="18" fill="#050505" />
      <text
        x="160"
        y="340"
        fill="#050505"
        fontFamily="Agency FB, Bahnschrift Condensed, Arial Narrow, sans-serif"
        fontSize="47"
        fontWeight="500"
        letterSpacing="18"
        textLength="1265"
        lengthAdjust="spacingAndGlyphs"
      >
        ARQUITECTOS Y ASOCIADOS
      </text>
      <circle cx="649" cy="416" r="37" fill="#d01b16" />
      <path d="M770 379 808 453H733Z" fill="#ffca29" />
      <rect x="856" y="379" width="74" height="74" fill="#80b3c9" />
    </svg>
  );
}
