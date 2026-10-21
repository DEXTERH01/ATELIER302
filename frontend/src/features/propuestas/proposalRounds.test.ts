import { describe,expect,it } from 'vitest';
import { etiquetaRonda,rondaIncluida } from './proposalRounds';

describe('control de rondas de propuestas',()=>{
  it('incluye exactamente las dos primeras rondas',()=>{
    expect(rondaIncluida(1)).toBe(true);expect(rondaIncluida(2)).toBe(true);
    expect(rondaIncluida(3)).toBe(false);
  });
  it('distingue evaluación y cambio adicional desde la tercera',()=>{
    expect(etiquetaRonda(3,false)).toBe('Evaluar adicional');
    expect(etiquetaRonda(3,true)).toBe('Adicional');
  });
});
