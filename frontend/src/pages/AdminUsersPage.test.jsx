import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const ADMIN = 'admin@esibuy.test'
const MENSAJE_ERROR = 'No se ha podido cargar el listado de usuarios. Inténtalo de nuevo.'

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

async function abrirEnLaSegundaPagina(user) {
  await abrirListado()
  await esperarFilas(10)
  await user.click(screen.getByRole('button', { name: /siguiente/i }))
  await screen.findByText(/página 2 de 3/i)
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
    expect(columna(0).sort((a, b) => a.localeCompare(b))).toEqual(['Ana López García', 'Mariana Ruiz Pascual'])
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

  it('HU19-CP05: mientras llega la página siguiente la tabla se marca como ocupada', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    let liberarPeticion
    const peticionRetenida = new Promise((resolve) => {
      liberarPeticion = resolve
    })
    server.use(http.get('/api/admin/usuarios', () => peticionRetenida, { once: true }))
    const contenedorTabla = () => screen.getByRole('table', { name: 'Usuarios' }).closest('[aria-busy]')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => expect(contenedorTabla()).toHaveAttribute('aria-busy', 'true'))
    liberarPeticion()
    expect(await screen.findByText(/página 2 de 3/i)).toBeInTheDocument()
    expect(contenedorTabla()).toHaveAttribute('aria-busy', 'false')
  })

  it('HU19-CP06: estando en la página 2, al buscar vuelve a la página 1', async () => {
    const user = userEvent.setup()
    await abrirEnLaSegundaPagina(user)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'ana')

    await esperarFilas(2)
  })

  it('HU19-CP06: estando en la página 2, al filtrar vuelve a la página 1', async () => {
    const user = userEvent.setup()
    await abrirEnLaSegundaPagina(user)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')

    await esperarFilas(5)
  })

  it('HU19-CP07: sin coincidencias muestra que no se han encontrado usuarios', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'zzz')

    expect(await screen.findByRole('heading', { name: 'No se han encontrado usuarios' })).toBeInTheDocument()
    expect(screen.queryByRole('table', { name: 'Usuarios' })).not.toBeInTheDocument()
  })

  it('HU19-CP07: sin coincidencias, "Limpiar filtros" vuelve a mostrar todos los usuarios', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'zzz')
    await screen.findByRole('heading', { name: 'No se han encontrado usuarios' })

    await user.click(screen.getByRole('button', { name: 'Limpiar filtros' }))

    await waitFor(() => expect(screen.getByRole('table', { name: 'Usuarios' })).toBeInTheDocument())
    await esperarFilas(10)
    expect(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' })).toHaveValue('')
  })

  it('HU19-CP08: si la API responde 500 muestra un mensaje de error', async () => {
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 })))

    await abrirListado()

    expect(await screen.findByText(MENSAJE_ERROR)).toBeInTheDocument()
    expect(screen.queryByRole('table', { name: 'Usuarios' })).not.toBeInTheDocument()
    expect(screen.queryByText(/cargando usuarios/i)).not.toBeInTheDocument()
  })

  it('HU19-CP08: tras un error, "Reintentar" vuelve a cargar el listado', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirListado()
    await screen.findByText(MENSAJE_ERROR)

    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    await waitFor(() => expect(screen.getByRole('table', { name: 'Usuarios' })).toBeInTheDocument())
    await esperarFilas(10)
    expect(screen.queryByText(MENSAJE_ERROR)).not.toBeInTheDocument()
  })
})
