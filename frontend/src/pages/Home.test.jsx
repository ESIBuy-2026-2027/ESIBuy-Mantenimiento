import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthContext } from '../auth/AuthContext'
import { renderWithProviders } from '../test/utils'
import Home from './Home'

// La home muestra u oculta «Crear mi cuenta» según la sesión: se renderiza con un contexto de sesión explícito
function renderHome(user = null) {
  return renderWithProviders(
    <AuthContext.Provider value={{ user, loading: false, signIn: () => {}, signOut: () => {} }}>
      <Home />
    </AuthContext.Provider>
  )
}

describe('Home', () => {
  it('home_alRenderizar_muestraTituloDeBienvenida', () => {
    renderHome()

    const heading = screen.getByRole('heading', { level: 1 })
    expect(heading).toBeInTheDocument()
    expect(heading).toHaveTextContent(/bienvenid/i)
    expect(heading).toHaveTextContent(/esibuy/i)
  })

  it('home_alRenderizar_muestraTextoDescriptivo', () => {
    renderHome()

    expect(screen.getByText(/marketplace/i)).toBeInTheDocument()
    expect(screen.getByText(/clientes/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedores/i)).toBeInTheDocument()
    expect(screen.getByText(/administradores/i)).toBeInTheDocument()
  })

  it('home_alRenderizar_muestraEnlaceDeCrearCuentaHaciaRegistro', () => {
    renderHome()

    const link = screen.getByRole('link', { name: /crear mi cuenta/i })
    expect(link).toHaveAttribute('href', '/register')
  })

  // Fallos de la demo del Sprint 1: con la sesión iniciada se podía ir a crear otra cuenta
  it('HU1-CP25: con la sesión iniciada no ofrece crear una cuenta', () => {
    renderHome({ id: 'u-3', nombre: 'Carla', rol: 'CLIENTE' })

    expect(screen.queryByRole('link', { name: /crear mi cuenta/i })).not.toBeInTheDocument()
  })

  it('home_alRenderizar_explicaComoFuncionaParaQuienCompra', () => {
    renderHome()

    expect(screen.getByRole('heading', { level: 2, name: /cómo funciona/i })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /quiero comprar/i })).toHaveAttribute('aria-selected', 'true')
    const pasos = within(screen.getByRole('tabpanel')).getAllByRole('heading', { level: 3 })
    expect(pasos.map((paso) => paso.textContent)).toEqual([
      'Crea tu cuenta',
      'Busca y filtra',
      'Compra en un paso',
      'Sigue tu envío y valora',
    ])
  })

  it('home_alElegirOtroPerfil_muestraSusPasos', async () => {
    const user = userEvent.setup()
    renderHome()

    await user.click(screen.getByRole('tab', { name: /quiero vender/i }))

    const panel = screen.getByRole('tabpanel')
    expect(within(panel).getByRole('heading', { name: 'Registra tu tienda' })).toBeInTheDocument()
    expect(within(panel).queryByRole('heading', { name: 'Busca y filtra' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('tab', { name: /administro/i }))

    expect(within(screen.getByRole('tabpanel')).getByRole('heading', { name: 'Resuelve incidencias' })).toBeInTheDocument()
  })

  it('home_sinSesion_invitaAEmpezarHaciaElRegistro', () => {
    renderHome()

    expect(screen.getByRole('link', { name: /empezar ahora/i })).toHaveAttribute('href', '/register')
  })

  it('home_conSesion_noInvitaAEmpezar', () => {
    renderHome({ id: 'u-3', nombre: 'Carla', rol: 'CLIENTE' })

    expect(screen.queryByRole('link', { name: /empezar ahora/i })).not.toBeInTheDocument()
  })
})
