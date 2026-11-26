import { afterEach, describe, it, expect, vi } from 'vitest'
import { act, fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

// Aviso de alertas sin leer justo al iniciar sesión (AB#529): el login lleva al cliente a su inicio con
// `state.trasIniciarSesion`, y ahí se avisa si tiene alertas sin leer, con un enlace a «Mis alertas».
// El cliente de ejemplo de los mocks (cliente@esibuy.test) tiene una alerta sin leer (el teclado mecánico baja de
// 89,50 € a 74,90 €) y dos leídas.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP29, propuesto.

const TRAS_INICIAR_SESION = { pathname: '/products', state: { trasIniciarSesion: true } }
const AVISO = '¡Baja un 16 %!'
const URL_ALERTAS = '/api/customer/alerts'

const alerta = (id, tipo, leida, producto) => ({
  id,
  tipo,
  fecha: '2026-11-26T10:00:00Z',
  leida,
  producto: { precioAnterior: null, precioNuevo: null, imagen: '', ...producto },
})

// Se va sola a los 8 s (AB#531). Los temporizadores simulados también avanzan solos, para que MSW y los findBy sigan
// funcionando; se adelantan a mano con margen para lo que tarde el propio test.
const pasar = (ms) => act(() => vi.advanceTimersByTime(ms))

afterEach(() => {
  vi.useRealTimers()
})

async function abrirNotificacion() {
  await abrirSesionPorApi('cliente@esibuy.test')
  renderApp(TRAS_INICIAR_SESION)
  const notificacion = await screen.findByRole('status', { name: 'Notificación de alertas' })
  // Ya está pintada; esto deja correr los efectos pendientes, entre ellos el que arranca la cuenta de los 8 s
  await act(async () => {})
  return notificacion
}

describe('AlertsLoginNotice', () => {
  it('HU529-CP29: al iniciar sesión avisa de las alertas sin leer con una notificación que enlaza a Mis alertas', async () => {
    const user = userEvent.setup()
    const notificacion = await abrirNotificacion()

    expect(notificacion).toHaveTextContent(AVISO)
    expect(notificacion).toHaveTextContent('Tienes 1 alerta sin leer')
    await user.click(within(notificacion).getByRole('link', { name: 'Ver alertas' }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/alerts$/))
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })

  it('HU529-CP29: el aviso no se puede cerrar a mano: se va solo a los 8 segundos', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const notificacion = await abrirNotificacion()

    expect(within(notificacion).queryByRole('button')).not.toBeInTheDocument()
    await pasar(7000)
    expect(screen.getByText(AVISO)).toBeInTheDocument()
    await pasar(1500)

    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })

  it('HU529-CP29: sin alertas sin leer no hay aviso al iniciar sesión', async () => {
    server.use(http.get(URL_ALERTAS, () => HttpResponse.json({ mensaje: 'No tienes alertas', alertas: [] })))
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(TRAS_INICIAR_SESION)

    expect(await screen.findByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.queryByRole('status', { name: 'Notificación de alertas' })).not.toBeInTheDocument()
  })

  it('HU529-CP29: si no viene de iniciar sesión, no hay aviso aunque tenga alertas sin leer', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/products')

    // Las alertas ya han llegado: la campanita lleva su número (AB#531), pero ni suena ni sale la notificación
    expect(await screen.findByTestId('contador-alertas')).toBeInTheDocument()
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Mis alertas' }).querySelector('[data-sonando]')).toBeNull()
  })
})

// Desde AB#531 la notificación sale abajo a la derecha y destaca la alerta sin leer más reciente, mientras la
// campanita suena. PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU531-CP12, propuesto.
describe('AlertsLoginNotice · alerta destacada', () => {
  it('HU531-CP12: destaca la alerta sin leer más reciente con la foto, el porqué en grande y los precios, y la campanita suena', async () => {
    const notificacion = await abrirNotificacion()

    expect(within(notificacion).getByText('¡Bajada de precio!')).toBeInTheDocument()
    expect(within(notificacion).getByText('Teclado mecánico')).toBeInTheDocument()
    expect(within(notificacion).getByText(AVISO)).toBeInTheDocument()
    expect(within(notificacion).getByText(/74,90/)).toBeInTheDocument()
    expect(within(notificacion).getByText(/89,50/).closest('del')).not.toBeNull()
    expect(within(notificacion).getByRole('img', { name: 'Teclado mecánico' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Mis alertas' }).querySelector('[data-sonando="true"]')).not.toBeNull()
  })

  it('HU531-CP12: con varias sin leer destaca la más reciente y dice cuántas más hay; las leídas no cuentan', async () => {
    server.use(
      http.get(URL_ALERTAS, () =>
        HttpResponse.json({
          mensaje: 'Alertas consultadas correctamente',
          alertas: [
            alerta('a-4', 'DISPONIBILIDAD', true, { id: 'p-9', nombre: 'Altavoz' }),
            alerta('a-3', 'DISPONIBILIDAD', false, { id: 'p-2', nombre: 'Ratón inalámbrico' }),
            alerta('a-2', 'PRODUCTO_NUEVO', false, { id: 'p-4', nombre: 'Auriculares' }),
            alerta('a-1', 'BAJADA_PRECIO', false, { id: 'p-1', nombre: 'Teclado', precioAnterior: 50, precioNuevo: 40 }),
          ],
        }),
      ),
    )
    const notificacion = await abrirNotificacion()

    expect(within(notificacion).getByText('¡De nuevo en stock!')).toBeInTheDocument()
    expect(within(notificacion).getByText('Ratón inalámbrico')).toBeInTheDocument()
    expect(within(notificacion).getByText('¡Vuelve a estar disponible!')).toBeInTheDocument()
    expect(within(notificacion).getByRole('img', { name: 'Sin imagen' })).toBeInTheDocument()
    expect(within(notificacion).queryByText(/€/)).not.toBeInTheDocument()
    expect(within(notificacion).getByText('+2 alertas más sin leer')).toBeInTheDocument()
  })

  it('HU531-CP12: un producto nuevo de una categoría favorita se cuenta como novedad', async () => {
    server.use(
      http.get(URL_ALERTAS, () =>
        HttpResponse.json({
          mensaje: 'Alertas consultadas correctamente',
          alertas: [alerta('a-1', 'PRODUCTO_NUEVO', false, { id: 'p-4', nombre: 'Auriculares' })],
        }),
      ),
    )
    const notificacion = await abrirNotificacion()

    expect(within(notificacion).getByText('¡Novedad!')).toBeInTheDocument()
    expect(within(notificacion).getByText('¡Nuevo en tu categoría favorita!')).toBeInTheDocument()
  })

  it('HU531-CP12: con el ratón encima la cuenta se para, y al quitarlo termina de contar', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const notificacion = await abrirNotificacion()

    fireEvent.mouseEnter(notificacion)
    await pasar(12000)
    expect(screen.getByText(AVISO)).toBeInTheDocument()
    fireEvent.mouseLeave(notificacion)
    await pasar(8500)

    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })

  it('HU531-CP12: con el foco en «Ver alertas» la cuenta se para', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const notificacion = await abrirNotificacion()

    act(() => within(notificacion).getByRole('link', { name: 'Ver alertas' }).focus())
    await pasar(12000)

    expect(screen.getByText(AVISO)).toBeInTheDocument()
  })

  it('HU531-CP12: Escape no la cierra', async () => {
    const user = userEvent.setup()
    await abrirNotificacion()

    await user.keyboard('{Escape}')

    expect(screen.getByText(AVISO)).toBeInTheDocument()
  })
})
