import { describe, expect, it } from 'vitest';
import { siguientesEstados } from './projectState';
describe('siguientesEstados', () => {
  it('mantiene terminales sin salida', () => {
    expect(siguientesEstados('CERRADO', false)).toEqual([]);
    expect(siguientesEstados('CANCELADO', true)).toEqual([]);
  });
  it('bifurca el expediente según trámite', () => {
    expect(siguientesEstados('EXPEDIENTE_FINAL', true)).toContain('EN_TRAMITE_PUBLICO');
    expect(siguientesEstados('EXPEDIENTE_FINAL', false)).not.toContain('EN_TRAMITE_PUBLICO');
  });
});
