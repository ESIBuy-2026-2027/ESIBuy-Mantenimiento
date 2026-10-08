import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const ADMIN = 'admin@esibuy.test'

async function abrirListado() {
  await abrirSesionPorApi(ADMIN)
  renderApp('/admin/users')
  await screen.findByRole('heading', { level: 1, name: 'Usuarios' })
}

function filasDeDatos() {
  return within(screen.getByRole('table', { name: 'Usuarios' })).getAllByRole('row').slice(1)
}

function columna(indice) {
  return filasDeDatos().map((fila) => within(fila).getAllByRole('cell')[indice].textContent)
}

async function esperarFilas(cantidad) {
  await waitFor(() => expect(filasDeDatos()).toHaveLength(cantidad))
}

describe('AdminUsersPage (HU19)', () => {
  it('HU19-CP01: con sesión de admin muestra el título y los 10 primeros usuarios', async () => {
    await abrirListado()

    await esperarFilas(10)
    expect(within(filasDeDatos()[0]).getByText('Mariana Ruiz Pascual')).toBeInTheDocument()
    expect(screen.getByText(/mostrando 1–10 de 25 usuarios/i)).toBeInTheDocument()
  })

  it('HU19-CP02: al buscar "ana" muestra solo los usuarios que coinciden', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'ana')

    await esperarFilas(2)
    expect(columna(0).sort()).toEqual(['Ana López García', 'Mariana Ruiz Pascual'])
  })

  it('HU19-CP03: al filtrar por Vendedor muestra solo vendedores', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Rol' }), 'Vendedor')

    await esperarFilas(7)
    expect(columna(2).every((rol) => rol === 'Vendedor')).toBe(true)
  })

  it('HU19-CP04: al filtrar por Pendiente muestra solo cuentas pendientes', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')

    await esperarFilas(5)
    expect(columna(3).every((estado) => estado === 'Pendiente')).toBe(true)
  })

  it('HU19-CP05: al pulsar "Siguiente" muestra la página 2 de 3', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    expect(await screen.findByText(/página 2 de 3/i)).toBeInTheDocument()
    expect(screen.getByText(/mostrando 11–20 de 25 usuarios/i)).toBeInTheDocument()
    await esperarFilas(10)
  })

  it('HU19-CP06: estando en la página 2, al buscar vuelve a la página 1', () => {
    // TODO ciclo 17
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP07: sin coincidencias muestra que no se han encontrado usuarios', () => {
    // TODO ciclo 18
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP08: si la API responde 500 muestra un mensaje de error', () => {
    // TODO ciclo 19
    expect.fail('Funcionalidad no implementada')
  })
})
