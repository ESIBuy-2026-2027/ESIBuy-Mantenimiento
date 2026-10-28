import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import AddPaymentMethodPanel from './AddPaymentMethodPanel'

// Panel de alta de un método de pago (HU6), inspirado en la tarjeta «Payment Details» y adaptado a Fluent UI.

function abrirPanel() {
  const onSaved = vi.fn()
  const onCancel = vi.fn()
  renderWithProviders(<AddPaymentMethodPanel onSaved={onSaved} onCancel={onCancel} />)
  return { onSaved, onCancel }
}

describe('AddPaymentMethodPanel (HU6): tipo de método', () => {
  it('HU6-CP30: empieza con Tarjeta elegida y sus campos: número, caducidad, CVV y titular', () => {
    abrirPanel()

    expect(screen.getByRole('heading', { level: 2, name: /datos del método de pago/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Tarjeta', pressed: true })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'PayPal', pressed: false })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Bizum', pressed: false })).toBeInTheDocument()
    expect(screen.getByLabelText(/número de tarjeta/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/caducidad/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/cvv/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/titular/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/url de paypal/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/teléfono/i)).not.toBeInTheDocument()
  })

  it('HU6-CP30: al elegir PayPal o Bizum muestra solo su campo', async () => {
    const usuario = userEvent.setup()
    abrirPanel()

    await usuario.click(screen.getByRole('button', { name: 'PayPal' }))
    expect(screen.getByRole('button', { name: 'PayPal', pressed: true })).toBeInTheDocument()
    expect(screen.getByLabelText(/url de paypal/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/número de tarjeta/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/teléfono/i)).not.toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Bizum' }))
    expect(screen.getByLabelText(/teléfono/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/url de paypal/i)).not.toBeInTheDocument()
  })
})

describe('AddPaymentMethodPanel (HU6): validación', () => {
  it('HU6-CP31: no envía una tarjeta con el número, la caducidad, el CVV o el titular con formato incorrecto', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP31: no envía una URL de PayPal o un teléfono de Bizum con formato incorrecto', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('AddPaymentMethodPanel (HU6): guardar', () => {
  it('HU6-CP32: guarda solo los datos del tipo elegido y avisa de que se ha añadido', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP32: al cancelar o cerrar el panel se borran los datos escritos', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('AddPaymentMethodPanel (HU6): errores del backend', () => {
  it('HU6-CP33: un método rechazado por el backend muestra un aviso sin repetir los datos enviados', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP33: al superar el límite de métodos avisa de que ya hay 5', () => {
    expect.fail('Funcionalidad no implementada')
  })
})
