import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const esperarRuta = (ruta) =>
  waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))

// Cierra la sesión desde la zona del rol con el botón de la cabecera y comprueba lo que pide la HU43: se avisa al
// servidor, se vuelve al login con un aviso y la zona deja de ser accesible (también tras volver a cargar la página).
async function cerrarSesionDesdeSuZona(email, zona) {
  const user = userEvent.setup()
  const cierres = []
  server.events.on('request:start', ({ request }) => {
    if (request.method === 'POST' && new URL(request.url).pathname === '/api/auth/logout') cierres.push(request)
  })
  await abrirSesionPorApi(email)
  const { unmount } = renderApp(zona)

  try {
    await user.click(await screen.findByRole('button', { name: /cerrar sesión/i }))

    await esperarRuta('/login')
    expect(await screen.findByText('Has cerrado sesión.')).toBeInTheDocument()
    expect(cierres).toHaveLength(1)
  } finally {
    server.events.removeAllListeners()
  }

  unmount()
  renderApp(zona)
  await esperarRuta('/login')
}

describe('AppHeader', () => {
  it('appHeader_sinSesion_muestraIniciarSesionYRegistrarse', async () => {
    renderApp('/')

    expect(await screen.findByRole('link', { name: /iniciar sesión/i })).toHaveAttribute('href', '/login')
    expect(screen.getByRole('link', { name: /registrarse/i })).toHaveAttribute('href', '/register')
    expect(screen.queryByRole('button', { name: /cerrar sesión/i })).not.toBeInTheDocument()
  })

  it('appHeader_mientrasComprueba_laSesionNoMuestraNingunBoton', () => {
    renderApp('/')

    expect(screen.queryByRole('link', { name: /iniciar sesión/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cerrar sesión/i })).not.toBeInTheDocument()
  })

  it('appHeader_enlacesDeInvitado_recibenFocoConTeclado', async () => {
    renderApp('/')
    const loginLink = await screen.findByRole('link', { name: /iniciar sesión/i })
    const registerLink = screen.getByRole('link', { name: /registrarse/i })

    loginLink.focus()
    expect(loginLink).toHaveFocus()
    expect(loginLink).not.toHaveAttribute('tabindex', '-1')

    registerLink.focus()
    expect(registerLink).toHaveFocus()
    expect(registerLink).not.toHaveAttribute('tabindex', '-1')
  })

  it('appHeader_alHacerClicEnIniciarSesion_navegaAlLogin', async () => {
    const user = userEvent.setup()
    renderApp('/')

    await user.click(await screen.findByRole('link', { name: /iniciar sesión/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /iniciar sesión/i })).toBeInTheDocument()
  })

  it('appHeader_conSesion_muestraCerrarSesionYOcultaIniciarSesionYRegistrarse', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    expect(await screen.findByRole('button', { name: /cerrar sesión/i })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /iniciar sesión/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /registrarse/i })).not.toBeInTheDocument()
  })

  it('appHeader_enPaginaDeRolVacia_sigueMostrandoCerrarSesion', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    expect(await screen.findByRole('button', { name: /cerrar sesión/i })).toBeInTheDocument()
  })

  it('HU43-CP04: el cliente cierra sesión desde su zona, vuelve al login con un aviso y ya no puede entrar', async () => {
    await cerrarSesionDesdeSuZona('cliente@esibuy.test', '/products')
  })

  it('HU43-CP05: el vendedor cierra sesión desde su zona, vuelve al login con un aviso y ya no puede entrar', async () => {
    await cerrarSesionDesdeSuZona('vendedor@esibuy.test', '/seller/products')
  })

  it('HU43-CP06: el administrador cierra sesión desde su panel, vuelve al login con un aviso y ya no puede entrar', async () => {
    await cerrarSesionDesdeSuZona('admin@esibuy.test', '/admin')
  })

  it('HU43-CP08: si el servidor falla al cerrar sesión, la sesión del navegador se cierra igualmente', async () => {
    const user = userEvent.setup()
    server.use(http.post('/api/auth/logout', () => new HttpResponse(null, { status: 500 })))
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/products')

    await user.click(await screen.findByRole('button', { name: /cerrar sesión/i }))

    await esperarRuta('/login')
    expect(await screen.findByText('Has cerrado sesión.')).toBeInTheDocument()
  })

  it('appHeader_conSesionDeAdmin_muestraLaOpcionPanelDeControl', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/')

    expect(await screen.findByRole('link', { name: /panel de control/i })).toHaveAttribute('href', '/admin')
  })

  it('appHeader_conSesionDeCliente_noMuestraLaOpcionPanelDeControl', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    await screen.findByRole('button', { name: /cerrar sesión/i })
    expect(screen.queryByRole('link', { name: /panel de control/i })).not.toBeInTheDocument()
  })

  it('appHeader_conSesionDeVendedor_noMuestraLaOpcionPanelDeControl', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/')

    await screen.findByRole('button', { name: /cerrar sesión/i })
    expect(screen.queryByRole('link', { name: /panel de control/i })).not.toBeInTheDocument()
  })

  it('appHeader_sinSesion_noMuestraLaOpcionPanelDeControl', async () => {
    renderApp('/')

    await screen.findByRole('link', { name: /iniciar sesión/i })
    expect(screen.queryByRole('link', { name: /panel de control/i })).not.toBeInTheDocument()
  })

  it('appHeader_alPulsarPanelDeControl_navegaAlPanelDeAdmin', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/')

    await user.click(await screen.findByRole('link', { name: /panel de control/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /panel de control/i })).toBeInTheDocument()
  })

  it('loginPage_noMuestraLaCabeceraDeLaAplicacion', async () => {
    renderApp('/login')

    expect(await screen.findByRole('heading', { level: 1, name: /iniciar sesión/i })).toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: /cuenta/i })).not.toBeInTheDocument()
  })

  // Fallos de la demo del Sprint 1: el vendedor no tenía acceso a sus productos desde el menú
  it('HU15-CP04: con sesión de vendedor muestra el enlace a sus productos', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/')

    const enlace = await screen.findByRole('link', { name: /mis productos/i })
    expect(enlace).toHaveAttribute('href', '/seller/products')
  })

  it('HU15-CP04: con sesión de cliente no muestra el enlace a los productos del vendedor', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    await screen.findByRole('button', { name: /cerrar sesión/i })
    expect(screen.queryByRole('link', { name: /mis productos/i })).not.toBeInTheDocument()
  })

  it('HU4-CP11: el cliente y el vendedor tienen el enlace a su perfil', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    const { unmount } = renderApp('/')
    expect(await screen.findByRole('link', { name: /mi perfil/i })).toHaveAttribute('href', '/profile')
    unmount()

    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/')
    expect(await screen.findByRole('link', { name: /mi perfil/i })).toHaveAttribute('href', '/profile')
  })

  it('HU4-CP11: el administrador no tiene el enlace al perfil', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/')

    await screen.findByRole('button', { name: /cerrar sesión/i })
    expect(screen.queryByRole('link', { name: /mi perfil/i })).not.toBeInTheDocument()
  })

  it('HU4-CP11: al pulsar Mi perfil se abre el perfil del usuario', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    await user.click(await screen.findByRole('link', { name: /mi perfil/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /mi perfil/i })).toBeInTheDocument()
    expect(await screen.findByLabelText('Nombre')).toHaveValue('Carlos')
  })
})
