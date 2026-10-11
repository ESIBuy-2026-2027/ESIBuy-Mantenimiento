import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

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

  it('appHeader_alCerrarSesion_vuelveALaHomeYMuestraBotonesDeInvitado', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    await user.click(await screen.findByRole('button', { name: /cerrar sesión/i }))

    expect(await screen.findByRole('link', { name: /iniciar sesión/i })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/$/))
  })

  it('appHeader_tras_cerrarSesion_laRutaProtegidaYaNoEsAccesible', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')
    await user.click(await screen.findByRole('button', { name: /cerrar sesión/i }))
    await screen.findByRole('link', { name: /iniciar sesión/i })

    await user.click(screen.getByRole('link', { name: /iniciar sesión/i }))

    expect(await screen.findByLabelText(/correo electrónico/i)).toBeInTheDocument()
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
})
