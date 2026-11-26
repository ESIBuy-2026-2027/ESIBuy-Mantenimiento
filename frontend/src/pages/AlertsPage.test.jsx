import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis alertas» del cliente (AB#529) contra el contrato del backend: GET /api/customer/alerts devuelve
// { mensaje, alertas: [{ id, tipo, fecha, leida, producto: { id, nombre, precioAnterior, precioNuevo } }] }, de la más
// reciente a la más antigua. Marcarlas como leídas (y resaltar las no leídas) es de AB#531.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU529-CP26 a CP28, propuestos.

const URL_ALERTAS = '/api/customer/alerts'

const ALERTAS = {
  mensaje: 'Alertas consultadas correctamente',
  alertas: [
    {
      id: 'a-2',
      tipo: 'BAJADA_PRECIO',
      fecha: '2026-11-26T10:00:00Z',
      leida: false,
      producto: { id: 'p-1', nombre: 'Auriculares inalámbricos', precioAnterior: 59.9, precioNuevo: 53.91 },
    },
    {
      id: 'a-1',
      tipo: 'DISPONIBILIDAD',
      fecha: '2026-11-25T18:30:00Z',
      leida: true,
      producto: { id: 'p-3', nombre: 'Ratón ergonómico', precioAnterior: null, precioNuevo: null },
    },
  ],
}

const esperarRuta = (ruta) =>
  waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))

async function abrirMisAlertas(respuesta = ALERTAS) {
  server.use(http.get(URL_ALERTAS, () => HttpResponse.json(respuesta)))
  await abrirSesionPorApi('cliente@esibuy.test')
  renderApp('/alerts')
  expect(await screen.findByRole('heading', { level: 1, name: 'Mis alertas' })).toBeInTheDocument()
}

describe('AlertsPage', () => {
  it('HU529-CP26: muestra cada alerta con su tipo, el producto enlazado a su detalle y la fecha, de la más reciente a la más antigua', async () => {
    await abrirMisAlertas()

    const lista = await screen.findByRole('list', { name: 'Tus alertas' })
    const [bajada, disponible] = within(lista).getAllByRole('listitem')
    expect(within(bajada).getByText('Ha bajado de precio')).toBeInTheDocument()
    expect(within(bajada).getByRole('link', { name: 'Auriculares inalámbricos' })).toHaveAttribute('href', '/products/p-1')
    expect(within(disponible).getByText('Vuelve a estar disponible')).toBeInTheDocument()
    expect(within(disponible).getByRole('link', { name: 'Ratón ergonómico' })).toHaveAttribute('href', '/products/p-3')
    expect(within(bajada).getByText(/26\/11\/2026/)).toBeInTheDocument()
  })

  it('HU529-CP26: una bajada de precio muestra el precio anterior y el nuevo; una de disponibilidad no muestra precios', async () => {
    await abrirMisAlertas()

    const lista = await screen.findByRole('list', { name: 'Tus alertas' })
    const [bajada, disponible] = within(lista).getAllByRole('listitem')
    expect(within(bajada).getByText(/59,90/)).toBeInTheDocument()
    expect(within(bajada).getByText(/53,91/)).toBeInTheDocument()
    expect(within(disponible).queryByText(/€/)).not.toBeInTheDocument()
  })

  it('HU529-CP27: sin alertas muestra un estado vacío que lleva a los favoritos', async () => {
    await abrirMisAlertas({ mensaje: 'No tienes alertas', alertas: [] })

    expect(await screen.findByText('No tienes alertas')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver mis favoritos' })).toHaveAttribute('href', '/favorites')
  })

  it('HU529-CP27: muestra un indicador mientras carga', async () => {
    server.use(
      http.get(URL_ALERTAS, async () => {
        await delay('infinite')
        return HttpResponse.json(ALERTAS)
      }),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/alerts')

    expect(await screen.findByText('Cargando tus alertas…')).toBeInTheDocument()
  })

  it('HU529-CP27: si falla avisa con un error y «Reintentar» las vuelve a pedir', async () => {
    const user = userEvent.setup()
    let peticiones = 0
    server.use(
      http.get(URL_ALERTAS, () => {
        peticiones += 1
        return peticiones === 1 ? new HttpResponse(null, { status: 500 }) : HttpResponse.json(ALERTAS)
      }),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/alerts')

    expect(await screen.findByText('No se han podido cargar tus alertas. Inténtalo de nuevo.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    expect(await screen.findByRole('list', { name: 'Tus alertas' })).toBeInTheDocument()
  })

  it('HU529-CP28: sin sesión, Mis alertas lleva a iniciar sesión', async () => {
    renderApp('/alerts')

    await esperarRuta('/login')
  })

  it('HU529-CP28: un vendedor no tiene alertas de favoritos: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/alerts')

    await esperarRuta('/seller/products')
  })
})
