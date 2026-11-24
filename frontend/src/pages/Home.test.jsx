import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { AuthContext } from '../auth/AuthContext'
import { renderWithProviders } from '../test/utils'
import Home from './Home'

function renderHome(user = null) {
  return renderWithProviders(
    <AuthContext.Provider value={{ user, loading: false, signIn: () => {}, signOut: () => {} }}>
      <Home />
    </AuthContext.Provider>
  )
}

describe('Home', () => {
  it('home_alRenderizar_muestraLaPresentacionPrincipal', () => {
    renderHome()

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/bienvenido a esibuy/i)
    expect(screen.getByText(/marketplace de la comunidad universitaria/i)).toBeInTheDocument()
    expect(screen.getByRole('img', { name: /ilustración de un portátil/i })).toHaveAttribute(
      'src',
      '/images/esibuy-home-illustration.png',
    )
  })

  it('home_sinSesion_muestraAccesoYRegistro', () => {
    renderHome()

    expect(screen.getByRole('link', { name: /iniciar sesión/i })).toHaveAttribute('href', '/login')
    expect(screen.getByRole('link', { name: /crear cuenta/i })).toHaveAttribute('href', '/register')
  })

  it('home_conSesion_noOfreceCrearOtraCuenta', () => {
    renderHome({ id: 'u-3', nombre: 'Carla', rol: 'CLIENTE' })

    expect(screen.queryByRole('link', { name: /crear cuenta/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /iniciar sesión/i })).not.toBeInTheDocument()
  })

  it('home_alRenderizar_muestraLasVentajasPrincipales', () => {
    renderHome()

    expect(screen.getByText('Compras seguras')).toBeInTheDocument()
    expect(screen.getByText('Seguimiento de pedidos')).toBeInTheDocument()
    expect(screen.getByText('Gran variedad')).toBeInTheDocument()
  })

  it('home_alRenderizar_muestraElAccesoAlCatalogo', () => {
    renderHome()

    expect(screen.getByRole('link', { name: /ver catálogo/i })).toHaveAttribute('href', '/products')
  })

  it('home_sinSesion_invitaARegistrarseAlFinal', () => {
    renderHome()

    expect(screen.getByRole('link', { name: /crear mi cuenta/i })).toHaveAttribute('href', '/register')
  })
})
