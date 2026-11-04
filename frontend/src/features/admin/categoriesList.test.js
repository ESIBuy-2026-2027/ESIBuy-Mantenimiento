import { describe, it, expect } from 'vitest'
import { CATEGORY_SORT_OPTIONS, filterAndSortCategories } from './categoriesList'

const HOGAR = { id: 'c-1', nombre: 'Hogar', descripcion: 'Muebles y decoración', estado: 'ACTIVA', numeroProductos: 14 }
const TECNOLOGIA = { id: 'c-2', nombre: 'Tecnología', descripcion: '', estado: 'ACTIVA', numeroProductos: 18 }
const JUGUETES = { id: 'c-3', nombre: 'Juguetes', descripcion: 'Juegos', estado: 'INACTIVA', numeroProductos: 3 }
const MODA = { id: 'c-4', nombre: 'Moda', descripcion: '', estado: 'INACTIVA', numeroProductos: 0 }
const ALIMENTACION = { id: 'c-5', nombre: 'Alimentación', descripcion: '', estado: 'ACTIVA', numeroProductos: 6 }

// Desordenadas a propósito: el resultado no debe depender del orden de entrada.
const CATEGORIAS = [TECNOLOGIA, MODA, HOGAR, JUGUETES, ALIMENTACION]

const SIN_FILTROS = { search: '', status: '', sort: 'name', order: 'asc' }

const nombres = (categorias) => categorias.map((categoria) => categoria.nombre)

describe('filterAndSortCategories', () => {
  it('HU39-CP08: sin filtros devuelve todas las categorías, también las inactivas, ordenadas por nombre', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, SIN_FILTROS))).toEqual([
      'Alimentación',
      'Hogar',
      'Juguetes',
      'Moda',
      'Tecnología',
    ])
  })

  it('HU39-CP08: la búsqueda por nombre no distingue mayúsculas ni tildes', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, search: 'TECNOLOGIA' }))).toEqual([
      'Tecnología',
    ])
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, search: 'alimentacion' }))).toEqual([
      'Alimentación',
    ])
  })

  it('HU39-CP08: la búsqueda encuentra el texto en cualquier parte del nombre y solo mira el nombre', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, search: 'og' }))).toEqual(['Hogar', 'Tecnología'])
    expect(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, search: 'muebles' })).toEqual([])
  })

  it('HU39-CP08: el filtro por estado deja solo las activas o solo las inactivas', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, status: 'ACTIVA' }))).toEqual([
      'Alimentación',
      'Hogar',
      'Tecnología',
    ])
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, status: 'INACTIVA' }))).toEqual([
      'Juguetes',
      'Moda',
    ])
  })

  it('HU39-CP08: la búsqueda y el estado se combinan', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, search: 'o', status: 'INACTIVA' }))).toEqual([
      'Moda',
    ])
  })

  it('HU39-CP08: ordena por nombre en sentido descendente', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, order: 'desc' }))).toEqual([
      'Tecnología',
      'Moda',
      'Juguetes',
      'Hogar',
      'Alimentación',
    ])
  })

  it('HU39-CP08: ordena por número de productos, de menos a más y al revés', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, sort: 'products' }))).toEqual([
      'Moda',
      'Juguetes',
      'Alimentación',
      'Hogar',
      'Tecnología',
    ])
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, sort: 'products', order: 'desc' }))).toEqual([
      'Tecnología',
      'Hogar',
      'Alimentación',
      'Juguetes',
      'Moda',
    ])
  })

  it('HU39-CP08: ordena por estado, las activas primero, y a igualdad desempata por nombre', () => {
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, sort: 'status' }))).toEqual([
      'Alimentación',
      'Hogar',
      'Tecnología',
      'Juguetes',
      'Moda',
    ])
    expect(nombres(filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, sort: 'status', order: 'desc' }))).toEqual([
      'Moda',
      'Juguetes',
      'Tecnología',
      'Hogar',
      'Alimentación',
    ])
  })

  it('HU39-CP08: no modifica la lista que recibe', () => {
    const copia = [...CATEGORIAS]

    filterAndSortCategories(CATEGORIAS, { ...SIN_FILTROS, order: 'desc' })

    expect(CATEGORIAS).toEqual(copia)
  })

  it('HU39-CP08: ofrece ordenar por nombre, productos y estado', () => {
    expect(CATEGORY_SORT_OPTIONS).toEqual([
      { value: 'name', label: 'Nombre' },
      { value: 'products', label: 'Productos' },
      { value: 'status', label: 'Estado' },
    ])
  })
})
