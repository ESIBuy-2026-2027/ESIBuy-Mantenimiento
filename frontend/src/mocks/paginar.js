const TAMANO_PAGINA_POR_DEFECTO = 10

export function leerPagina(params, tamanoPorDefecto = TAMANO_PAGINA_POR_DEFECTO) {
  return {
    pagina: Math.max(1, Number(params.get('page')) || 1),
    tamano: Math.max(1, Number(params.get('size')) || tamanoPorDefecto),
  }
}

export function paginar(elementos, { pagina, tamano }, transformar = (elemento) => elemento) {
  const inicio = (pagina - 1) * tamano

  return {
    items: elementos.slice(inicio, inicio + tamano).map(transformar),
    page: pagina,
    size: tamano,
    totalItems: elementos.length,
    totalPages: Math.ceil(elementos.length / tamano),
  }
}

export function normalizar(texto) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}
