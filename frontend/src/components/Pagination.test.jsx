import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { renderWithProviders } from '../test/utils'
import Pagination from './Pagination'

describe('Pagination', () => {
  it('HU19-CP05: muestra la página actual y el total de páginas', () => {
    renderWithProviders(<Pagination page={1} totalPages={3} onPageChange={() => {}} />)

    expect(screen.getByText('Página 2 de 3')).toBeInTheDocument()
  })

  it('HU19-CP05: en la primera página "Anterior" está deshabilitado', () => {
    // TODO ciclo 7
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP05: en la última página "Siguiente" está deshabilitado', () => {
    // TODO ciclo 8
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP05: al pulsar "Siguiente" pide la página siguiente', () => {
    // TODO ciclo 9
    expect.fail('Funcionalidad no implementada')
  })
})
