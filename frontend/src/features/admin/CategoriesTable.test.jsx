import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import { renderWithProviders } from '../../test/utils'
import CategoriesTable from './CategoriesTable'

const CATEGORIAS = [
  { id: 'c-1', nombre: 'Hogar', descripcion: 'Muebles y decoración', estado: 'ACTIVA', numeroProductos: 14 },
  { id: 'c-2', nombre: 'Moda', descripcion: '', estado: 'INACTIVA', numeroProductos: 0 },
]

const celdasDeCadaFila = () =>
  screen
    .getAllByRole('row')
    .slice(1)
    .map((fila) => within(fila).getAllByRole('cell').map((celda) => celda.textContent))

describe('CategoriesTable', () => {
  it('HU39-CP09: muestra las cabeceras de las cuatro columnas', () => {
    renderWithProviders(<CategoriesTable categories={CATEGORIAS} />)

    expect(screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)).toEqual([
      'Nombre',
      'Descripción',
      'Estado',
      'Productos',
    ])
  })

  it('HU39-CP09: muestra de cada categoría su nombre, descripción, estado y número de productos', () => {
    renderWithProviders(<CategoriesTable categories={CATEGORIAS} />)

    expect(celdasDeCadaFila()).toEqual([
      ['Hogar', 'Muebles y decoración', 'Activa', '14'],
      ['Moda', '—', 'Inactiva', '0'],
    ])
  })

  it('HU39-CP07: el estado de cada categoría se muestra en una insignia', () => {
    renderWithProviders(<CategoriesTable categories={CATEGORIAS} />)

    const [hogar, moda] = screen.getAllByRole('row').slice(1)
    expect(within(hogar).getByText('Activa')).toBeInTheDocument()
    expect(within(moda).getByText('Inactiva')).toBeInTheDocument()
  })

  it('HU39-CP10: una descripción vacía se muestra como un guion', () => {
    renderWithProviders(<CategoriesTable categories={[CATEGORIAS[1]]} />)

    expect(within(screen.getAllByRole('row')[1]).getByText('—')).toBeInTheDocument()
  })

  it('HU39-CP09: con renderActions añade la columna Acciones con lo que devuelva para cada categoría', () => {
    renderWithProviders(
      <CategoriesTable categories={CATEGORIAS} renderActions={(categoria) => <span>acción de {categoria.nombre}</span>} />,
    )

    expect(screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)).toEqual([
      'Nombre',
      'Descripción',
      'Estado',
      'Productos',
      'Acciones',
    ])
    expect(within(screen.getAllByRole('row')[1]).getByText('acción de Hogar')).toBeInTheDocument()
  })

  it('HU39-CP09: mientras se refresca, la tabla avisa de que está ocupada', () => {
    renderWithProviders(<CategoriesTable categories={CATEGORIAS} refreshing />)

    expect(screen.getByRole('table').parentElement).toHaveAttribute('aria-busy', 'true')
  })
})
