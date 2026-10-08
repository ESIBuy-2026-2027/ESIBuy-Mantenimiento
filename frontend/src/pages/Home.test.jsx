import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { renderWithProviders } from '../test/utils'
import Home from './Home'

describe('Home', () => {
  it('home_alRenderizar_muestraTituloDeBienvenida', () => {
    renderWithProviders(<Home />)

    const heading = screen.getByRole('heading', { level: 1 })
    expect(heading).toBeInTheDocument()
    expect(heading).toHaveTextContent(/bienvenid/i)
    expect(heading).toHaveTextContent(/esibuy/i)
  })

  it('home_alRenderizar_muestraTextoDescriptivo', () => {
    renderWithProviders(<Home />)

    expect(screen.getByText(/marketplace/i)).toBeInTheDocument()
    expect(screen.getByText(/clientes/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedores/i)).toBeInTheDocument()
    expect(screen.getByText(/administradores/i)).toBeInTheDocument()
  })

  it('home_alRenderizar_muestraEnlaceDeCrearCuentaHaciaRegistro', () => {
    renderWithProviders(<Home />)

    const link = screen.getByRole('link', { name: /crear mi cuenta/i })
    expect(link).toHaveAttribute('href', '/register')
  })
})
