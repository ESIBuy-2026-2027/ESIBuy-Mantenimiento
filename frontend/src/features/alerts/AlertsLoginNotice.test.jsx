import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

// Aviso de alertas sin leer justo al iniciar sesión (AB#529): el login lleva al cliente a su inicio con
// `state.trasIniciarSesion`, y ahí se avisa si tiene alertas sin leer, con un enlace a «Mis alertas».
// El cliente de ejemplo de los mocks (cliente@esibuy.test) tiene una alerta sin leer y otra leída.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP29, propuesto.

const TRAS_INICIAR_SESION = { pathname: '/products', state: { trasIniciarSesion: true } }
const AVISO = 'Tienes 1 alerta sin leer de tus productos favoritos.'

describe('AlertsLoginNotice', () => {
  it('HU529-CP29: al iniciar sesión avisa de las alertas sin leer con una notificación en la cabecera que enlaza a Mis alertas', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(TRAS_INICIAR_SESION)

    // Como una notificación del móvil, sale del botón de la cuenta: va en la cabecera, no en la página
    const notificacion = await screen.findByRole('status', { name: 'Notificación de alertas' })
    expect(notificacion).toHaveTextContent('Novedades en tus favoritos')
    expect(notificacion).toHaveTextContent(AVISO)
    expect(within(screen.getByRole('banner')).getByRole('status', { name: 'Notificación de alertas' })).toBe(notificacion)
    await user.click(within(notificacion).getByRole('link', { name: 'Ver alertas' }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/alerts$/))
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })

  it('HU529-CP29: el aviso se puede cerrar', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(TRAS_INICIAR_SESION)

    await user.click(await screen.findByRole('button', { name: 'Cerrar el aviso de alertas' }))

    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })

  it('HU529-CP29: sin alertas sin leer no hay aviso al iniciar sesión', async () => {
    server.use(http.get('/api/customer/alerts', () => HttpResponse.json({ mensaje: 'No tienes alertas', alertas: [] })))
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(TRAS_INICIAR_SESION)

    expect(await screen.findByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.queryByText(/alertas? sin leer/)).not.toBeInTheDocument()
  })

  it('HU529-CP29: si no viene de iniciar sesión, no hay aviso aunque tenga alertas sin leer', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/products')

    // Las alertas ya han llegado: la campanita lleva su número (AB#531)
    expect(await screen.findByTestId('contador-alertas')).toBeInTheDocument()
    expect(screen.queryByText(AVISO)).not.toBeInTheDocument()
  })
})
