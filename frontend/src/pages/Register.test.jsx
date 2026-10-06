import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../test/utils'
import Register from './Register'

describe('Register', () => {
  it('register_alRenderizar_muestraOpcionesClienteYVendedor', () => {
    renderWithProviders(<Register />)

    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.getByText(/cliente/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedor/i)).toBeInTheDocument()
  })
  
  it('register_alElegirCliente_muestraFormularioDeCliente', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /cliente/i }))

    expect(screen.getByRole('heading', { name: /registro de cliente/i })).toBeInTheDocument()
  })

  it('register_alElegirVendedor_muestraFormularioDeVendedor', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /vendedor/i }))

    expect(screen.getByRole('heading', { name: /registro de vendedor/i })).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraLosCamposDelFormulario', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /cliente/i }))

    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de nacimiento/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/teléfono/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument()
  })

  it('register_alEnviarFormularioClienteVacio_muestraErroresDeCamposObligatorios', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /cliente/i }))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i)).toHaveLength(6)
  })
})