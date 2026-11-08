import { describe, expect, it, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import PremiumCheckout from './PremiumCheckout'

function renderCheckout(props = {}) {
  const valores = {
    description: 'Al simular el pago se completará tu alta.',
    cancelLabel: 'Volver',
    onPay: vi.fn().mockResolvedValue(undefined),
    onCancel: vi.fn(),
    ...props,
  }
  renderWithProviders(<PremiumCheckout {...valores} />)
  return valores
}

describe('PremiumCheckout (HU45): pasarela de pago simulada de ESIBuy Premium', () => {
  it('HU45-CP20: muestra el resumen de ESIBuy Premium sin ningún precio y avisa de que es una simulación', () => {
    renderCheckout()

    expect(screen.getByRole('heading', { level: 1, name: /pasarela de pago/i })).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 2, name: 'ESIBuy Premium' })).toBeInTheDocument()
    expect(screen.getByText('Al simular el pago se completará tu alta.')).toBeInTheDocument()
    expect(screen.getByText(/no se realiza ningún cobro real/i)).toBeInTheDocument()
    expect(document.body).not.toHaveTextContent(/€|\d+[.,]\d{2}/)
  })

  it('HU45-CP20: no pide datos de tarjeta: solo tiene los botones de simular el pago y de volver', () => {
    renderCheckout()

    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: /simular pago/i })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Volver' })).toBeEnabled()
  })

  it('HU45-CP20: Simular pago llama a onPay y, mientras tanto, muestra Procesando pago… deshabilitado', async () => {
    let terminar
    const onPay = vi.fn(() => new Promise((resolver) => { terminar = resolver }))
    renderCheckout({ onPay })

    await userEvent.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(onPay).toHaveBeenCalledTimes(1)
    expect(screen.getByRole('button', { name: /procesando pago/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Volver' })).toBeDisabled()
    terminar()
    expect(await screen.findByRole('button', { name: /simular pago/i })).toBeEnabled()
  })

  it('HU45-CP20: el botón de volver llama a onCancel sin simular el pago', async () => {
    const { onCancel, onPay } = renderCheckout()

    await userEvent.click(screen.getByRole('button', { name: 'Volver' }))

    expect(onCancel).toHaveBeenCalledTimes(1)
    expect(onPay).not.toHaveBeenCalled()
  })

  it('HU45-CP20: el error que se le indica se muestra en un aviso', () => {
    renderCheckout({ error: 'No se ha podido completar el pago.' })

    expect(screen.getByText('No se ha podido completar el pago.')).toBeInTheDocument()
  })
})
