import { describe, expect, it } from 'vitest';
import { siguientesEstados } from './projectState';
describe('siguientesEstados', () => {
  it('mantiene terminales sin salida', () => {
    expect(siguientesEstados('CERRADO', false)).toEqual([]);
    expect(siguientesEstados('CANCELADO', true)).toEqual([]);
  });
  it('bifurca el expediente según trámite', () => {
    expect(siguientesEstados('EXPEDIENTE_FINAL', true)).toEqual(['CANCELADO']);
    expect(siguientesEstados('EXPEDIENTE_FINAL', false)).not.toContain('EN_TRAMITE_PUBLICO');
  });
  it('reserva revisión y expediente final al flujo de propuestas', () => {
    expect(siguientesEstados('EN_DISENO', false)).not.toContain('REVISION_CLIENTE');
    expect(siguientesEstados('REVISION_CLIENTE', false)).not.toContain('EXPEDIENTE_FINAL');
  });
});
