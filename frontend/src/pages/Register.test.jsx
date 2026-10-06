import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../test/utils'
import Register from './Register'

async function renderYElegirCliente() {
  renderWithProviders(<Register />)
  await userEvent.click(screen.getByRole('button', { name: /cliente/i }))
}

describe('Register', () => {
  it('register_alRenderizar_muestraOpcionesClienteYVendedor', () => {
    renderWithProviders(<Register />)

    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.getByText(/cliente/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedor/i)).toBeInTheDocument()
  })
  
  it('register_alElegirCliente_muestraFormularioDeCliente', async () => {
    await renderYElegirCliente()

    expect(screen.getByRole('heading', { name: /registro de cliente/i })).toBeInTheDocument()
  })

  it('register_alElegirVendedor_muestraFormularioDeVendedor', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /vendedor/i }))

    expect(screen.getByRole('heading', { name: /registro de vendedor/i })).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraLosCamposDelFormulario', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de nacimiento/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/teléfono/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument()
  })

  it('register_alEnviarFormularioClienteVacio_muestraErroresDeCamposObligatorios', async () => {
    await renderYElegirCliente()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i)).toHaveLength(6)
  })

  it('register_alIntroducirFechaDeMenorDeEdad_muestraErrorDeEdad', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/fecha de nacimiento/i), '2020-01-01')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/mayor de edad/i)).toBeInTheDocument()
  })

  it('register_alIntroducirEmailInvalido_muestraErrorDeFormato', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/email/i), 'esto-no-es-un-email')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/email válido/i)).toBeInTheDocument()
  })

  it('register_alIntroducirDniInvalido_muestraErrorDeFormato', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/dni/i), '1234')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/dni válido/i)).toBeInTheDocument()
  })
})