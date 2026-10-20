import type { SVGProps } from 'react';
import {
  DESCRIPTOR_ADVANCE,
  DESCRIPTOR_PATH,
  DESCRIPTOR_UPEM,
  WORDMARK_ADVANCE,
  WORDMARK_PATH,
  WORDMARK_UPEM,
} from './AtelierLogoPaths';

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
      <path
        d={WORDMARK_PATH}
        fill="#050505"
        transform={`translate(154 263) scale(${1290 / WORDMARK_ADVANCE} ${-290 / WORDMARK_UPEM})`}
      />
      <rect x="0" y="263" width="1536" height="18" fill="#050505" />
      <path
        d={DESCRIPTOR_PATH}
        fill="#050505"
        transform={`translate(160 340) scale(${1265 / DESCRIPTOR_ADVANCE} ${-47 / DESCRIPTOR_UPEM})`}
      />
      <circle cx="649" cy="416" r="37" fill="#d01b16" />
      <path d="M770 379 808 453H733Z" fill="#ffca29" />
      <rect x="856" y="379" width="74" height="74" fill="#80b3c9" />
    </svg>
  );
}
