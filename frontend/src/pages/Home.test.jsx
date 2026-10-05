import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
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

  it('home_alRenderizar_muestraEnlaceIniciarSesionConHrefLogin', () => {
    renderWithProviders(<Home />)

    const link = screen.getByRole('link', { name: /iniciar sesión/i })
    expect(link).toHaveAttribute('href', '/login')
  })

  it('home_alRenderizar_muestraEnlaceRegistrarseConHrefRegister', () => {
    renderWithProviders(<Home />)

    const link = screen.getByRole('link', { name: /registrarse/i })
    expect(link).toHaveAttribute('href', '/register')
  })

  it('home_alEnfocarEnlaces_recibenFocoConTeclado', () => {
    renderWithProviders(<Home />)
    const loginLink = screen.getByRole('link', { name: /iniciar sesión/i })
    const registerLink = screen.getByRole('link', { name: /registrarse/i })

    loginLink.focus()
    expect(loginLink).toHaveFocus()
    expect(loginLink).not.toHaveAttribute('tabindex', '-1')

    registerLink.focus()
    expect(registerLink).toHaveFocus()
    expect(registerLink).not.toHaveAttribute('tabindex', '-1')
  })

  it('home_alHacerClicEnIniciarSesion_navegaALogin', async () => {
    render(
      <FluentProvider theme={webLightTheme}>
        <MemoryRouter initialEntries={['/']}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<h1>Página de login</h1>} />
          </Routes>
        </MemoryRouter>
      </FluentProvider>
    )

    const link = screen.getByRole('link', { name: /iniciar sesión/i })
    expect(link).toHaveAttribute('href', '/login')

    await userEvent.click(link)

    expect(screen.getByRole('heading', { name: /página de login/i })).toBeInTheDocument()
  })
})
