import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Supervisión de incidencias del administrador (HU49, T304) contra el contrato del backend (PR #232):
// GET /api/admin/incidencias devuelve las incidencias de todos, de las más antiguas a las más recientes, por páginas
// (page desde 1), cada una con su autor ({email, rol}, o null si la cuenta se ha eliminado). Del mock de incidencias:
//   15 incidencias en total, así que dos páginas de 10.
//   Página 1: las 10 más antiguas, todas de Carlos (cliente@esibuy.test).
//   Página 2: dos de Carlos, dos de Vera (vendedor@esibuy.test) y una de una cuenta eliminada, la más reciente.

const URL_INCIDENCIAS = '/api/admin/incidencias'
const ADMIN = 'admin@esibuy.test'
const PAGINA_VACIA = { items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 }

async function abrirSupervision() {
  await abrirSesionPorApi(ADMIN)
  renderApp('/admin/incidents')
  await screen.findByRole('heading', { level: 1, name: 'Incidencias' })
}

const lista = () => screen.findByRole('list', { name: 'Incidencias de los usuarios' })
const incidencias = async () => within(await lista()).getAllByRole('listitem')

async function irALaSegundaPagina(user) {
  await lista()
  await user.click(screen.getByRole('button', { name: /siguiente/i }))
  await screen.findByText('Página 2 de 2')
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('AdminIncidentsPage (HU49): lista', () => {
  it('HU49-CP17: abre en la primera página, la de las incidencias más antiguas', async () => {
    await abrirSupervision()

    const filas = await incidencias()
    expect(filas).toHaveLength(10)
    expect(filas[0]).toHaveTextContent('No me llega el pedido')
    expect(filas[9]).toHaveTextContent('Imagen que no carga')
    expect(screen.getByText('Página 1 de 2')).toBeInTheDocument()
    expect(screen.getByText(/Mostrando 1–10 de 15 incidencias/)).toBeInTheDocument()
  })

  it('HU49-CP17: pide una sola vez la primera página de tamaño 10', async () => {
    const paginas = []
    server.events.on('request:start', ({ request }) => {
      const url = new URL(request.url)
      if (url.pathname === URL_INCIDENCIAS) paginas.push(`${url.searchParams.get('page')}/${url.searchParams.get('size')}`)
    })

    await abrirSupervision()
    await lista()

    expect(paginas).toEqual(['1/10'])
    server.events.removeAllListeners()
  })

  it('HU49-CP17: el título y el subtítulo explican la vista', async () => {
    await abrirSupervision()

    expect(screen.getByText(/las incidencias que reportan los clientes y los vendedores/i)).toBeInTheDocument()
  })

  it('HU49-CP18: cada incidencia muestra su autor, su estado y su respuesta', async () => {
    await abrirSupervision()

    const filas = await incidencias()
    expect(filas[1]).toHaveTextContent('Cobro duplicado')
    expect(filas[1]).toHaveTextContent('cliente@esibuy.test')
    expect(filas[1]).toHaveTextContent('Cliente')
    expect(filas[1]).toHaveTextContent('Abierta')
    expect(filas[1]).toHaveTextContent('Estamos revisando el cobro con el banco.')
  })

  it('HU49-CP18: las incidencias de un vendedor muestran su correo y el rol Vendedor', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    expect(filas).toHaveLength(5)
    expect(filas[2]).toHaveTextContent('No puedo subir la imagen de un producto')
    expect(filas[2]).toHaveTextContent('vendedor@esibuy.test')
    expect(filas[2]).toHaveTextContent('Vendedor')
  })

  it('HU49-CP19: la incidencia de una cuenta eliminada dice Cuenta eliminada', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    expect(filas[4]).toHaveTextContent('Cuenta eliminada')
    expect(filas[3]).not.toHaveTextContent('Cuenta eliminada')
  })

  it('HU49-CP20: la paginación lleva a la página siguiente y vuelve a la anterior', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    expect(screen.getByText(/Mostrando 11–15 de 15 incidencias/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /anterior/i }))

    await screen.findByText('Página 1 de 2')
    expect(await incidencias()).toHaveLength(10)
  })

  it('HU49-CP20: el administrador no tiene botones para crear incidencias', async () => {
    await abrirSupervision()
    await lista()

    expect(screen.queryByRole('link', { name: /nueva incidencia/i })).not.toBeInTheDocument()
  })
})

describe('AdminIncidentsPage (HU49): estados', () => {
  it('HU49-CP21: mientras carga muestra un indicador de carga y no la lista', async () => {
    server.use(
      http.get(URL_INCIDENCIAS, async () => {
        await delay(150)
        return HttpResponse.json(PAGINA_VACIA)
      }),
    )
    await abrirSupervision()

    expect(screen.getByText('Cargando incidencias…')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()
    await screen.findByText('No hay incidencias')
    expect(screen.queryByText('Cargando incidencias…')).not.toBeInTheDocument()
  })

  it('HU49-CP21: sin incidencias lo dice, sin lista ni paginación', async () => {
    server.use(http.get(URL_INCIDENCIAS, () => HttpResponse.json(PAGINA_VACIA)))
    await abrirSupervision()

    expect(await screen.findByText('No hay incidencias')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument()
  })

  it('HU49-CP21: si no se pueden cargar lo avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get(URL_INCIDENCIAS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirSupervision()

    expect(await screen.findByText('No se han podido cargar las incidencias. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await lista()
    expect(screen.queryByText(/no se han podido cargar las incidencias/i)).not.toBeInTheDocument()
  })
})

describe('AdminIncidentsPage (HU49): acceso', () => {
  it('HU49-CP22: un cliente que abre la supervisión vuelve a su inicio', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/admin/incidents')

    await esperarRuta('/products')
    expect(screen.queryByRole('heading', { level: 1, name: 'Incidencias' })).not.toBeInTheDocument()
  })

  it('HU49-CP22: un vendedor que abre la supervisión vuelve a su inicio', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/admin/incidents')

    await esperarRuta('/seller/products')
  })

  it('HU49-CP22: sin sesión, la supervisión lleva al login', async () => {
    renderApp('/admin/incidents')

    await esperarRuta('/login')
  })
})
