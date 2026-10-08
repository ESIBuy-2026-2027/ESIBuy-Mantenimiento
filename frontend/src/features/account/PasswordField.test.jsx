import { describe, it, expect, vi } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { PasswordField, PasswordRequirements } from './PasswordField'

function renderConTema(ui) {
  return render(<FluentProvider theme={webLightTheme}>{ui}</FluentProvider>)
}

describe('PasswordField', () => {
  it('passwordField_alRenderizar_esObligatorioYOcultaElTexto', () => {
    renderConTema(<PasswordField label="Password" value="secreto" onChange={vi.fn()} maxLength={64} />)

    const campo = screen.getByLabelText(/^password/i)
    expect(campo).toBeRequired()
    expect(campo).toHaveAttribute('type', 'password')
    expect(campo).toHaveAttribute('maxlength', '64')
    expect(campo).toHaveValue('secreto')
  })

  it('passwordField_alPulsarElOjo_alternaEntreMostrarYOcultar', async () => {
    const user = userEvent.setup()
    renderConTema(<PasswordField label="Repetir password" value="" onChange={vi.fn()} />)
    const campo = screen.getByLabelText(/^repetir password/i)

    await user.click(screen.getByRole('button', { name: /^mostrar repetir password$/i }))
    expect(campo).toHaveAttribute('type', 'text')

    await user.click(screen.getByRole('button', { name: /^ocultar repetir password$/i }))
    expect(campo).toHaveAttribute('type', 'password')
  })

  it('passwordField_alEscribir_avisaDelCambio', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    renderConTema(<PasswordField label="Password" value="" onChange={onChange} />)

    await user.type(screen.getByLabelText(/^password/i), 'a')

    expect(onChange).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ value: 'a' }))
  })

  it('passwordField_conError_muestraElMensaje', () => {
    renderConTema(<PasswordField label="Password" value="" onChange={vi.fn()} error="Este campo es obligatorio" />)

    expect(screen.getByText('Este campo es obligatorio')).toBeInTheDocument()
  })
})

describe('PasswordRequirements', () => {
  const iconos = (nombre) =>
    within(screen.getByRole('list', { name: /requisitos del password/i })).queryAllByRole('img', { name: nombre })

  it('passwordRequirements_sinPassword_muestraLosCincoRequisitosPendientes', () => {
    renderConTema(<PasswordRequirements password="" />)

    expect(screen.getAllByRole('listitem')).toHaveLength(5)
    expect(iconos(/pendiente/i)).toHaveLength(5)
    expect(iconos(/cumplido/i)).toHaveLength(0)
  })

  it('passwordRequirements_conPasswordQueLosCumpleTodos_losMarcaTodosComoCumplidos', () => {
    renderConTema(<PasswordRequirements password="Password123!" />)

    expect(iconos(/cumplido/i)).toHaveLength(5)
  })

  it('passwordRequirements_conPasswordParcial_soloMarcaLosQueCumple', () => {
    renderConTema(<PasswordRequirements password="abcdefgh" />)

    const requisito = (texto) => screen.getByText(texto).closest('li')
    expect(within(requisito('Al menos 8 caracteres')).getByRole('img', { name: /cumplido/i })).toBeInTheDocument()
    expect(within(requisito('Una minúscula')).getByRole('img', { name: /cumplido/i })).toBeInTheDocument()
    expect(within(requisito('Una mayúscula')).getByRole('img', { name: /pendiente/i })).toBeInTheDocument()
  })
})
