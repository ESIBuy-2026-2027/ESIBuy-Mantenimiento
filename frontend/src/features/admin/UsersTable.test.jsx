import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import { renderWithProviders } from '../../test/utils'
import UsersTable from './UsersTable'

describe('UsersTable', () => {
  it('HU19-CP01: muestra las cabeceras de las cinco columnas', () => {
    renderWithProviders(<UsersTable users={[]} />)

    const cabeceras = screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)

    expect(cabeceras).toEqual(['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta'])
  })

  it('HU19-CP01: muestra cada usuario con rol y estado legibles y la fecha en dd/mm/aaaa', () => {
    const users = [
      {
        id: 'u1',
        nombre: 'Ana',
        apellidos: 'López García',
        email: 'ana.lopez@esibuy.test',
        rol: 'ADMINISTRADOR',
        estado: 'ACTIVO',
        fechaAlta: '2026-03-05T10:00:00',
      },
      {
        id: 'u2',
        nombre: 'Mariana',
        apellidos: 'Ruiz',
        email: 'mariana.ruiz@esibuy.test',
        rol: 'VENDEDOR',
        estado: 'PENDIENTE',
        fechaAlta: '2026-10-01T10:00:00',
      },
    ]

    renderWithProviders(<UsersTable users={users} />)

    const filas = screen.getAllByRole('row').slice(1)
    const celdas = filas.map((fila) =>
      within(fila).getAllByRole('cell').map((celda) => celda.textContent),
    )

    expect(celdas).toEqual([
      ['Ana López García', 'ana.lopez@esibuy.test', 'Administrador', 'Activo', '05/03/2026'],
      ['Mariana Ruiz', 'mariana.ruiz@esibuy.test', 'Vendedor', 'Pendiente', '01/10/2026'],
    ])
  })
})
