export const rondaIncluida=(numero:number)=>numero<=2;
export function etiquetaRonda(numero:number,adicional:boolean){
  if(rondaIncluida(numero))return 'Incluida';
  return adicional?'Adicional':'Evaluar adicional';
}
