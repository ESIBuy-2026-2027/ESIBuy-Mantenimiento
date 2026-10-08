import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import StatusConfirmDialog from './StatusConfirmDialog'

function renderConfirmacion(props = {}) {
  const handlers = { onConfirm: vi.fn(), onCancel: vi.fn() }
  renderWithProviders(
    <StatusConfirmDialog
      titulo="Activar usuario"
      pregunta="¿Seguro que quieres activar a Lucía Torres Vega?"
      confirmar="Activar"
      enviando={false}
      {...handlers}
      {...props}
    />,
  )
  return handlers
}

const boton = (texto) => screen.getByText(texto, { selector: 'button' })

describe('StatusConfirmDialog', () => {
  it('statusConfirmDialog_muestraElTituloLaPreguntaYLosDosBotones', () => {
    renderConfirmacion()

    expect(screen.getByText('Activar usuario')).toBeInTheDocument()
    expect(screen.getByText('¿Seguro que quieres activar a Lucía Torres Vega?')).toBeInTheDocument()
    expect(boton('Cancelar')).toBeInTheDocument()
    expect(boton('Activar')).toBeInTheDocument()
  })

  it('statusConfirmDialog_alConfirmar_avisaDeLaConfirmacion', async () => {
    const user = userEvent.setup()
    const { onConfirm, onCancel } = renderConfirmacion()

    await user.click(boton('Activar'))

    expect(onConfirm).toHaveBeenCalledTimes(1)
    expect(onCancel).not.toHaveBeenCalled()
  })

  it('statusConfirmDialog_alCancelar_avisaDeLaCancelacion', async () => {
    const user = userEvent.setup()
    const { onConfirm, onCancel } = renderConfirmacion()

    await user.click(boton('Cancelar'))

    expect(onCancel).toHaveBeenCalledTimes(1)
    expect(onConfirm).not.toHaveBeenCalled()
  })

  it('statusConfirmDialog_mientrasSeEnvia_deshabilitaLosBotones', () => {
    renderConfirmacion({ enviando: true })

    expect(boton('Activar')).toBeDisabled()
    expect(boton('Cancelar')).toBeDisabled()
  })
})
