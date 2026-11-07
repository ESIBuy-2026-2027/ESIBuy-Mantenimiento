import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis incidencias» (HU44, T289 y T290) contra el contrato del backend (PR #224): GET /api/perfil/incidencias devuelve
// las incidencias del usuario en sesión, de las más antiguas a las más recientes, por páginas (page desde 1).
// Del mock de incidencias:
//   Carlos (cliente@esibuy.test): 12 incidencias, así que dos páginas de 10; la segunda, con las dos más recientes.
//   Vera (vendedor@esibuy.test): 2 incidencias, una sola página.
//   Vicente (vendedor-vacio@esibuy.test): ninguna.

const URL_INCIDENCIAS = '/api/perfil/incidencias'
const CLIENTE = 'cliente@esibuy.test'
const VENDEDOR = 'vendedor@esibuy.test'
const VENDEDOR_SIN_INCIDENCIAS = 'vendedor-vacio@esibuy.test'

async function abrirMisIncidencias(email = CLIENTE) {
  await abrirSesionPorApi(email)
  renderApp('/incidents')
  await screen.findByRole('heading', { level: 1, name: /mis incidencias/i })
}

const lista = () => screen.findByRole('list', { name: 'Tus incidencias' })
const incidencias = async () => within(await lista()).getAllByRole('listitem')

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('IncidentsPage (HU44): lista', () => {
  it('HU44-CP27: abre en la última página, la de las incidencias más recientes', async () => {
    await abrirMisIncidencias()

    const filas = await incidencias()
    expect(filas).toHaveLength(2)
    expect(filas[0]).toHaveTextContent('Valoración que no se publica')
    expect(filas[1]).toHaveTextContent('Aviso de stock')
    expect(screen.getByText('Página 2 de 2')).toBeInTheDocument()
    expect(screen.getByText(/Mostrando 11–12 de 12 incidencias/)).toBeInTheDocument()
  })

  it('HU44-CP27: pide la primera página para saber cuántas hay y después la última', async () => {
    const paginas = []
    server.events.on('request:start', ({ request }) => {
      const url = new URL(request.url)
      if (url.pathname === URL_INCIDENCIAS) paginas.push(`${url.searchParams.get('page')}/${url.searchParams.get('size')}`)
    })

    await abrirMisIncidencias()
    await lista()

    await waitFor(() => expect(paginas).toEqual(['1/10', '2/10']))
    server.events.removeAllListeners()
  })

  it('HU44-CP27: la paginación lleva a la página anterior y a la siguiente', async () => {
    const user = userEvent.setup()
    await abrirMisIncidencias()
    await lista()

    await user.click(screen.getByRole('button', { name: /anterior/i }))

    await screen.findByText('Página 1 de 2')
    const filas = await incidencias()
    expect(filas).toHaveLength(10)
    expect(filas[0]).toHaveTextContent('No me llega el pedido')
    expect(filas[9]).toHaveTextContent('Imagen que no carga')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await screen.findByText('Página 2 de 2')
    expect(await incidencias()).toHaveLength(2)
  })

  it('HU44-CP27: con una sola página no hay paginación y se ven todas, con el estado y la respuesta', async () => {
    await abrirMisIncidencias(VENDEDOR)

    const filas = await incidencias()
    expect(filas).toHaveLength(2)
    expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument()
    expect(filas[0]).toHaveTextContent('Abierta')
    expect(filas[0]).toHaveTextContent('Ya lo hemos corregido, prueba de nuevo.')
    expect(filas[1]).toHaveTextContent('Pendiente')
    expect(filas[1]).toHaveTextContent('Sin respuesta todavía')
  })

  it('HU44-CP27: mientras carga muestra un indicador de carga y no la lista', async () => {
    server.use(
      http.get(URL_INCIDENCIAS, async () => {
        await delay(150)
        return HttpResponse.json({ items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 })
      }),
    )
    await abrirMisIncidencias()

    expect(screen.getByText('Cargando incidencias…')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Tus incidencias' })).not.toBeInTheDocument()
    await screen.findByText(/todavía no has reportado ninguna incidencia/i)
    expect(screen.queryByText('Cargando incidencias…')).not.toBeInTheDocument()
  })

  it('HU44-CP27: si no se pueden cargar lo avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get(URL_INCIDENCIAS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirMisIncidencias()

    expect(await screen.findByText('No se han podido cargar tus incidencias. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Tus incidencias' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await lista()
    expect(screen.queryByText(/no se han podido cargar tus incidencias/i)).not.toBeInTheDocument()
  })

  it('HU44-CP27: sin incidencias invita a reportar la primera', async () => {
    await abrirMisIncidencias(VENDEDOR_SIN_INCIDENCIAS)

    expect(await screen.findByText('Todavía no has reportado ninguna incidencia')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /reportar mi primera incidencia/i })).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Tus incidencias' })).not.toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument()
  })

  it('HU44-CP27: el título y el subtítulo explican la vista', async () => {
    await abrirMisIncidencias()

    expect(screen.getByText(/reporta un problema y consulta la respuesta de un administrador/i)).toBeInTheDocument()
  })
})

describe('IncidentsPage (HU44): nueva incidencia', () => {
  it('HU44-CP28: el botón Nueva incidencia lleva al formulario', async () => {
    const user = userEvent.setup()
    await abrirMisIncidencias()
    await lista()

    await user.click(screen.getByRole('link', { name: /^nueva incidencia$/i }))

    await esperarRuta('/incidents/new')
  })

  it('HU44-CP28: sin incidencias, reportar la primera también lleva al formulario', async () => {
    const user = userEvent.setup()
    await abrirMisIncidencias(VENDEDOR_SIN_INCIDENCIAS)

    await user.click(await screen.findByRole('link', { name: /reportar mi primera incidencia/i }))

    await esperarRuta('/incidents/new')
  })
})
