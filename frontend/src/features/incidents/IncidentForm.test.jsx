import { describe, it, expect, vi } from 'vitest'
import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import IncidentForm from './IncidentForm'

const campoAsunto = () => screen.getByLabelText(/^asunto/i)
const campoDescripcion = () => screen.getByLabelText(/^descripción/i)
const enviar = () => screen.getByRole('button', { name: /enviar incidencia/i })

function renderizar(props = {}) {
  const onSubmit = vi.fn()
  renderWithProviders(<IncidentForm saving={false} onSubmit={onSubmit} {...props} />)
  return onSubmit
}

describe('IncidentForm (HU44): campos', () => {
  it('HU44-CP29: pide el asunto y la descripción, obligatorios, con su contador de caracteres', () => {
    renderizar()

    expect(campoAsunto()).toBeRequired()
    expect(campoDescripcion()).toBeRequired()
    expect(screen.getByText('0/100')).toBeInTheDocument()
    expect(screen.getByText('0/1000')).toBeInTheDocument()
    expect(enviar()).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /cancelar/i })).toBeInTheDocument()
  })

  it('HU44-CP29: el contador cuenta lo escrito sin los espacios de los extremos', async () => {
    const user = userEvent.setup()
    renderizar()

    await user.type(campoAsunto(), '  Hola  ')
    await user.type(campoDescripcion(), 'Descripción')

    expect(screen.getByText('4/100')).toBeInTheDocument()
    expect(screen.getByText('11/1000')).toBeInTheDocument()
  })
})

describe('IncidentForm (HU44): validación', () => {
  it('HU44-CP29: enviar vacío marca los dos campos, con su mensaje, y no envía nada', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar()

    await user.click(enviar())

    expect(campoAsunto()).toHaveAccessibleDescription(/El asunto es obligatorio\./)
    expect(campoDescripcion()).toHaveAccessibleDescription(/La descripción es obligatoria\./)
    expect(campoAsunto()).toBeInvalid()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('HU44-CP29: un asunto o una descripción con solo espacios se tratan como vacíos', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar()

    await user.type(campoAsunto(), '   ')
    await user.type(campoDescripcion(), '   ')
    await user.click(enviar())

    expect(campoAsunto()).toHaveAccessibleDescription(/El asunto es obligatorio\./)
    expect(campoDescripcion()).toHaveAccessibleDescription(/La descripción es obligatoria\./)
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('HU44-CP29: pasarse del máximo avisa al momento y no deja enviar', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar()

    fireEvent.change(campoAsunto(), { target: { value: 'a'.repeat(101) } })
    fireEvent.change(campoDescripcion(), { target: { value: 'b'.repeat(1001) } })

    expect(campoAsunto()).toHaveAccessibleDescription(/El asunto no puede superar los 100 caracteres\./)
    expect(campoDescripcion()).toHaveAccessibleDescription(/La descripción no puede superar los 1000 caracteres\./)
    expect(screen.getByText('101/100')).toBeInTheDocument()

    await user.click(enviar())

    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('HU44-CP29: 100 caracteres de asunto y 1000 de descripción valen, aunque lleven espacios en los extremos', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar()

    fireEvent.change(campoAsunto(), { target: { value: `  ${'a'.repeat(100)}  ` } })
    fireEvent.change(campoDescripcion(), { target: { value: `\n${'b'.repeat(1000)}\n` } })
    await user.click(enviar())

    expect(onSubmit).toHaveBeenCalledWith({ asunto: 'a'.repeat(100), descripcion: 'b'.repeat(1000) })
  })

  it('HU44-CP29: el aviso de un campo desaparece al corregirlo', async () => {
    const user = userEvent.setup()
    renderizar()
    await user.click(enviar())

    await user.type(campoAsunto(), 'Pedido')

    expect(campoAsunto()).not.toHaveAccessibleDescription(/obligatorio/)
    expect(campoDescripcion()).toHaveAccessibleDescription(/La descripción es obligatoria\./)
  })

  it('HU44-CP29: al enviar entrega el asunto y la descripción sin espacios en los extremos', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar()

    await user.type(campoAsunto(), '  No me llega el pedido ')
    await user.type(campoDescripcion(), ' Lo pedí hace una semana  ')
    await user.click(enviar())

    expect(onSubmit).toHaveBeenCalledTimes(1)
    expect(onSubmit).toHaveBeenCalledWith({ asunto: 'No me llega el pedido', descripcion: 'Lo pedí hace una semana' })
  })
})

describe('IncidentForm (HU44): envío', () => {
  it('HU44-CP29: mientras se envía, el botón queda deshabilitado y no se envía dos veces', async () => {
    const user = userEvent.setup()
    const onSubmit = renderizar({ saving: true })

    await user.type(campoAsunto(), 'Asunto')
    await user.type(campoDescripcion(), 'Descripción')
    await user.type(campoDescripcion(), '{Enter}')

    expect(enviar()).toBeDisabled()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('HU44-CP29: si el envío falla, muestra el aviso y conserva lo escrito', async () => {
    const user = userEvent.setup()
    renderizar({ saveError: 'No se ha podido crear la incidencia.' })

    await user.type(campoAsunto(), 'Asunto')

    expect(screen.getByText('No se ha podido crear la incidencia.')).toBeInTheDocument()
    expect(campoAsunto()).toHaveValue('Asunto')
    expect(enviar()).toBeEnabled()
  })
})
