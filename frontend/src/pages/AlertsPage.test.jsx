import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis alertas» del cliente (AB#529) contra el contrato del backend: GET /api/customer/alerts devuelve
// { mensaje, alertas: [{ id, tipo, fecha, leida, producto: { id, nombre, precioAnterior, precioNuevo } }] }, de la más
// reciente a la más antigua. Marcarlas como leídas (y resaltar las no leídas) es de AB#531, al final del archivo.
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
      producto: {
        id: 'p-1',
        nombre: 'Auriculares inalámbricos',
        imagen: 'https://tienda.test/auriculares.png',
        precioAnterior: 59.9,
        precioNuevo: 53.91,
      },
    },
    {
      id: 'a-1',
      tipo: 'DISPONIBILIDAD',
      fecha: '2026-11-25T18:30:00Z',
      leida: true,
      producto: { id: 'p-3', nombre: 'Ratón ergonómico', imagen: '', precioAnterior: null, precioNuevo: null },
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
    expect(within(bajada).getByText('¡Bajada de precio!')).toBeInTheDocument()
    expect(within(bajada).getByRole('link', { name: 'Auriculares inalámbricos' })).toHaveAttribute('href', '/products/p-1')
    expect(within(bajada).getByRole('link', { name: 'Ver Auriculares inalámbricos' })).toHaveAttribute('href', '/products/p-1')
    expect(within(disponible).getByText('¡De nuevo en stock!')).toBeInTheDocument()
    expect(within(disponible).getByRole('link', { name: 'Ratón ergonómico' })).toHaveAttribute('href', '/products/p-3')
    expect(within(bajada).getByText(/26\/11\/2026/)).toBeInTheDocument()
  })

  it('HU529-CP26: una bajada de precio muestra el precio nuevo, el anterior tachado, el descuento y lo que se ahorra; una de disponibilidad no muestra precios', async () => {
    await abrirMisAlertas()

    const lista = await screen.findByRole('list', { name: 'Tus alertas' })
    const [bajada, disponible] = within(lista).getAllByRole('listitem')
    expect(within(bajada).getByText(/53,91/)).toBeInTheDocument()
    expect(within(bajada).getByText(/59,90/).closest('del')).not.toBeNull()
    expect(within(bajada).getByText('-10%')).toBeInTheDocument()
    expect(within(bajada).getByText(/Ahorras 5,99/)).toBeInTheDocument()
    expect(within(disponible).queryByText(/€/)).not.toBeInTheDocument()
    expect(within(disponible).getByText('Ya puedes comprarlo otra vez. ¡Que no se te escape!')).toBeInTheDocument()
  })

  it('HU529-CP26: cada alerta muestra la imagen del producto o, si no tiene, un marcador', async () => {
    await abrirMisAlertas()

    const lista = await screen.findByRole('list', { name: 'Tus alertas' })
    const [bajada, disponible] = within(lista).getAllByRole('listitem')
    expect(within(bajada).getByRole('img', { name: 'Auriculares inalámbricos' })).toHaveAttribute(
      'src',
      'https://tienda.test/auriculares.png',
    )
    expect(within(disponible).getByRole('img', { name: 'Sin imagen' })).toBeInTheDocument()
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

// Lectura de alertas (AB#531): PATCH /api/customer/alerts/{id}/read devuelve { mensaje, alerta } con la alerta leída.
// El servidor simulado guarda el estado, como el real: lo que se marca sigue marcado al volver a pedir la lista.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU531-CP08 a CP10, propuestos.
function servidorConEstado() {
  const estado = { alertas: structuredClone(ALERTAS.alertas), marcadas: [] }
  server.use(
    http.get(URL_ALERTAS, () => HttpResponse.json({ mensaje: ALERTAS.mensaje, alertas: estado.alertas })),
    http.patch(`${URL_ALERTAS}/:id/read`, ({ params }) => {
      const alerta = estado.alertas.find((candidata) => candidata.id === params.id)
      alerta.leida = true
      estado.marcadas.push(params.id)
      return HttpResponse.json({ mensaje: 'Alerta marcada como leída', alerta })
    }),
  )
  return estado
}

async function abrirMisAlertasConEstado() {
  const estado = servidorConEstado()
  await abrirSesionPorApi('cliente@esibuy.test')
  renderApp('/alerts')
  const lista = await screen.findByRole('list', { name: 'Tus alertas' })
  return { estado, lista }
}

describe('AlertsPage · lectura de alertas', () => {
  it('HU531-CP08: una alerta nueva lleva la etiqueta «Nueva» y el botón para marcarla; una leída no, y se anuncia como leída', async () => {
    const { lista } = await abrirMisAlertasConEstado()

    const [nueva, leida] = within(lista).getAllByRole('listitem')
    expect(within(nueva).getByText('Nueva')).toBeInTheDocument()
    expect(
      within(nueva).getByRole('button', { name: 'Marcar como leída la alerta de Auriculares inalámbricos' }),
    ).toBeInTheDocument()
    expect(within(leida).queryByText('Nueva')).not.toBeInTheDocument()
    expect(within(leida).queryByRole('button', { name: /Marcar como leída/ })).not.toBeInTheDocument()
    expect(within(leida).getByText('Leída')).toBeInTheDocument()
  })

  it('HU531-CP08: la página dice cuántas alertas quedan sin leer', async () => {
    await abrirMisAlertasConEstado()

    expect(screen.getByText('Tienes 1 alerta sin leer')).toBeInTheDocument()
  })

  it('HU531-CP09: al marcarla como leída sigue en la lista sin el distintivo de nueva, con un mensaje de éxito y el foco en «Ver producto»', async () => {
    const user = userEvent.setup()
    const { estado, lista } = await abrirMisAlertasConEstado()
    const [nueva] = within(lista).getAllByRole('listitem')

    await user.click(within(nueva).getByRole('button', { name: /Marcar como leída/ }))

    expect(await screen.findByText('Alerta marcada como leída')).toBeInTheDocument()
    expect(estado.marcadas).toEqual(['a-2'])
    expect(within(lista).getAllByRole('listitem')).toHaveLength(2)
    expect(within(nueva).queryByText('Nueva')).not.toBeInTheDocument()
    expect(within(nueva).queryByRole('button', { name: /Marcar como leída/ })).not.toBeInTheDocument()
    expect(within(nueva).getByText('Leída')).toBeInTheDocument()
    expect(within(nueva).getByRole('link', { name: 'Ver Auriculares inalámbricos' })).toHaveFocus()
    expect(screen.queryByText('Tienes 1 alerta sin leer')).not.toBeInTheDocument()
  })

  it('HU531-CP09: al marcarla como leída, la campanita de la cabecera deja de avisar', async () => {
    const user = userEvent.setup()
    const { lista } = await abrirMisAlertasConEstado()
    const campanita = screen.getByRole('link', { name: 'Mis alertas' })
    await waitFor(() => expect(campanita).toHaveAccessibleDescription('Tienes 1 alerta sin leer'))

    await user.click(within(lista).getByRole('button', { name: /Marcar como leída/ }))

    await waitFor(() => expect(campanita).not.toHaveAccessibleDescription(/alerta/))
  })

  it('HU531-CP10: si falla, avisa con un error y la alerta sigue siendo nueva', async () => {
    const user = userEvent.setup()
    const { lista } = await abrirMisAlertasConEstado()
    server.use(http.patch(`${URL_ALERTAS}/:id/read`, () => new HttpResponse(null, { status: 500 })))
    const [nueva] = within(lista).getAllByRole('listitem')

    await user.click(within(nueva).getByRole('button', { name: /Marcar como leída/ }))

    expect(
      await screen.findByText('No se ha podido marcar la alerta como leída. Inténtalo de nuevo.'),
    ).toBeInTheDocument()
    expect(within(nueva).getByText('Nueva')).toBeInTheDocument()
    expect(within(nueva).getByRole('button', { name: /Marcar como leída/ })).toBeEnabled()
  })

  it('HU531-CP10: si la alerta ya no existe, lo avisa y vuelve a pedir la lista', async () => {
    const user = userEvent.setup()
    const { estado, lista } = await abrirMisAlertasConEstado()
    server.use(
      http.patch(`${URL_ALERTAS}/:id/read`, () => {
        estado.alertas = estado.alertas.filter((alerta) => alerta.id !== 'a-2')
        return HttpResponse.json({ mensaje: 'La alerta no existe', code: 'ALERT_NOT_FOUND' }, { status: 404 })
      }),
    )

    await user.click(within(lista).getByRole('button', { name: /Marcar como leída/ }))

    expect(await screen.findByText('Esa alerta ya no existe. Hemos actualizado tu lista.')).toBeInTheDocument()
    await waitFor(() =>
      expect(within(screen.getByRole('list', { name: 'Tus alertas' })).getAllByRole('listitem')).toHaveLength(1),
    )
  })
})
