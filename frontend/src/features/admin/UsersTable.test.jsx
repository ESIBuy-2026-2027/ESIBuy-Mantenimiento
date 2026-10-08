import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { renderWithProviders } from '../../test/utils'
import UsersTable from './UsersTable'

describe('UsersTable', () => {
  it('HU19-CP01: muestra las cabeceras de las cinco columnas', () => {
    renderWithProviders(<UsersTable users={[]} />)

    const cabeceras = screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)

    expect(cabeceras).toEqual(['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta'])
  })

  it('HU19-CP01: muestra cada usuario con rol y estado legibles y la fecha en dd/mm/aaaa', () => {
    // TODO ciclo 8
    expect.fail('Funcionalidad no implementada')
  })
})
