import { describe, it, expect, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import UsersFilters from './UsersFilters'

function renderFilters(props = {}) {
  const handlers = {
    onSearchChange: vi.fn(),
    onRoleChange: vi.fn(),
    onStatusChange: vi.fn(),
  }
  renderWithProviders(<UsersFilters search="" role="" status="" {...handlers} {...props} />)
  return handlers
}

describe('UsersFilters', () => {
  it('HU19-CP02: avisa del texto escrito en el buscador por nombre o email', async () => {
    const user = userEvent.setup()
    const { onSearchChange } = renderFilters({ search: 'an' })

    const buscador = screen.getByRole('searchbox', { name: 'Buscar por nombre o email' })
    await user.type(buscador, 'a')

    expect(buscador).toHaveValue('an')
    expect(onSearchChange).toHaveBeenLastCalledWith('ana')
  })

  it('HU19-CP03: avisa del rol elegido en el filtro de rol', async () => {
    const user = userEvent.setup()
    const { onRoleChange } = renderFilters()

    const filtroRol = screen.getByRole('combobox', { name: 'Rol' })
    const opciones = within(filtroRol).getAllByRole('option').map((opcion) => opcion.textContent)
    await user.selectOptions(filtroRol, 'Vendedor')

    expect(opciones).toEqual(['Todos los roles', 'Administrador', 'Vendedor', 'Cliente'])
    expect(onRoleChange).toHaveBeenLastCalledWith('VENDEDOR')
  })

  it('HU19-CP04: avisa del estado elegido en el filtro de estado', () => {
    // TODO ciclo 11
    expect.fail('Funcionalidad no implementada')
  })
})
