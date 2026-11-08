import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import AddPaymentMethodPanel from './AddPaymentMethodPanel'

// Panel de alta de un método de pago (HU6), inspirado en la tarjeta «Payment Details» y adaptado a Fluent UI.

const URL_METODOS = '/api/customer/payment-methods'

// Cuenta las peticiones de alta que llegan al backend
function contarAltas() {
  let altas = 0
  server.use(
    http.post(URL_METODOS, () => {
      altas += 1
      return HttpResponse.json({ id: 'm-9', tipo: 'TARJETA', mascara: '•••• 1111', predeterminado: false }, { status: 201 })
    }),
  )
  return () => altas
}

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
  it('HU6-CP31: no envía una tarjeta con el número, la caducidad, el CVV o el titular con formato incorrecto', async () => {
    const altas = contarAltas()
    const usuario = userEvent.setup()
    abrirPanel()

    // 12 dígitos, mes 13, CVV de 2 dígitos y titular con números
    await usuario.type(screen.getByLabelText(/número de tarjeta/i), '4111 1111 1111')
    await usuario.type(screen.getByLabelText(/caducidad/i), '13/27')
    await usuario.type(screen.getByLabelText(/cvv/i), '12')
    await usuario.type(screen.getByLabelText(/titular/i), 'Juan 2')
    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))

    expect(screen.getByText('El número de tarjeta tiene entre 13 y 19 dígitos.')).toBeInTheDocument()
    expect(screen.getByText('Escribe la caducidad como MM/AA.')).toBeInTheDocument()
    expect(screen.getByText('El CVV tiene 3 o 4 dígitos.')).toBeInTheDocument()
    expect(screen.getByText(/el titular solo puede tener letras/i)).toBeInTheDocument()
    expect(altas()).toBe(0)
  })

  it('HU6-CP31: no envía una URL de PayPal o un teléfono de Bizum con formato incorrecto', async () => {
    const altas = contarAltas()
    const usuario = userEvent.setup()
    abrirPanel()

    await usuario.click(screen.getByRole('button', { name: 'PayPal' }))
    await usuario.type(screen.getByLabelText(/url de paypal/i), 'paypal.me sin esquema')
    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))
    expect(screen.getByText('Escribe una URL completa, como https://paypal.me/tu-cuenta.')).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Bizum' }))
    // Frontera: 8 dígitos no valen; el teléfono tiene 9
    await usuario.type(screen.getByLabelText(/teléfono/i), '61234567')
    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))
    expect(screen.getByText('El teléfono tiene 9 dígitos.')).toBeInTheDocument()

    expect(altas()).toBe(0)
  })
})

// Fallo de las pruebas del equipo: la caducidad no ponía la barra y los campos admitían más dígitos de la cuenta
describe('AddPaymentMethodPanel (HU6): formato mientras se escribe', () => {
  it('HU6-CP41: el número de tarjeta solo admite dígitos, se agrupa de 4 en 4 y no pasa de 19', async () => {
    const usuario = userEvent.setup()
    abrirPanel()
    const numero = screen.getByLabelText(/número de tarjeta/i)

    await usuario.type(numero, '4111abc111111111111')
    expect(numero).toHaveValue('4111 1111 1111 1111')

    await usuario.clear(numero)
    await usuario.type(numero, '1234567890123456789999')
    expect(numero).toHaveValue('1234 5678 9012 3456 789')
  })

  it('HU6-CP41: la caducidad pone la barra sola tras el mes, la quita al borrar y no pasa de MM/AA', async () => {
    const usuario = userEvent.setup()
    abrirPanel()
    const caducidad = screen.getByLabelText(/caducidad/i)

    await usuario.type(caducidad, '12')
    expect(caducidad).toHaveValue('12/')

    await usuario.type(caducidad, '{Backspace}')
    expect(caducidad).toHaveValue('12')

    await usuario.type(caducidad, '27999')
    expect(caducidad).toHaveValue('12/27')

    // Un mes de una cifra mayor que 1 se completa con un 0 delante
    await usuario.clear(caducidad)
    await usuario.type(caducidad, '4')
    expect(caducidad).toHaveValue('04/')
  })

  it('HU6-CP41: el CVV y el teléfono de Bizum solo admiten dígitos, hasta 4 y 9', async () => {
    const usuario = userEvent.setup()
    abrirPanel()

    const cvv = screen.getByLabelText(/cvv/i)
    await usuario.type(cvv, '12a345')
    expect(cvv).toHaveValue('1234')

    await usuario.click(screen.getByRole('button', { name: 'Bizum' }))
    const telefono = screen.getByLabelText(/teléfono/i)
    await usuario.type(telefono, '612 345 6789')
    expect(telefono).toHaveValue('612345678')
  })
})

describe('AddPaymentMethodPanel (HU6): guardar', () => {
  it('HU6-CP32: guarda solo los datos del tipo elegido y avisa de que se ha añadido', async () => {
    const peticion = {}
    const creado = { id: 'm-9', tipo: 'TARJETA', mascara: '•••• 1111', predeterminado: false }
    server.use(
      http.post(URL_METODOS, async ({ request }) => {
        peticion.cuerpo = await request.json()
        return HttpResponse.json(creado, { status: 201 })
      }),
    )
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel()

    // Se escribe algo en PayPal y luego se cambia a Tarjeta: lo de PayPal no se envía
    await usuario.click(screen.getByRole('button', { name: 'PayPal' }))
    await usuario.type(screen.getByLabelText(/url de paypal/i), 'https://paypal.me/juan')
    await usuario.click(screen.getByRole('button', { name: 'Tarjeta' }))
    await usuario.type(screen.getByLabelText(/número de tarjeta/i), '4111 1111 1111 1111')
    await usuario.type(screen.getByLabelText(/caducidad/i), '12/27')
    await usuario.type(screen.getByLabelText(/cvv/i), '123')
    await usuario.type(screen.getByLabelText(/titular/i), 'Juan Pérez')
    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))

    await waitFor(() => expect(onSaved).toHaveBeenCalledWith(creado))
    expect(peticion.cuerpo).toEqual({
      tipo: 'TARJETA',
      numeroTarjeta: '4111111111111111',
      titular: 'Juan Pérez',
      caducidad: '12/27',
      cvv: '123',
    })
  })

  it('HU6-CP32: al cancelar o cerrar el panel se borran los datos escritos', async () => {
    const altas = contarAltas()
    const usuario = userEvent.setup()
    const { onCancel } = abrirPanel()
    await usuario.type(screen.getByLabelText(/número de tarjeta/i), '4111111111111111')
    await usuario.type(screen.getByLabelText(/cvv/i), '123')

    await usuario.click(screen.getByRole('button', { name: /cancelar/i }))

    // Los datos de pago no se quedan en el panel aunque la página siga mostrándolo
    expect(onCancel).toHaveBeenCalled()
    expect(screen.getByLabelText(/número de tarjeta/i)).toHaveValue('')
    expect(screen.getByLabelText(/cvv/i)).toHaveValue('')
    expect(altas()).toBe(0)
  })
})

describe('AddPaymentMethodPanel (HU6): errores del backend', () => {
  it('HU6-CP33: un método rechazado por el backend muestra un aviso sin repetir los datos enviados', async () => {
    // Formato correcto, pero el backend lo rechaza (no pasa Luhn o la tarjeta está caducada)
    server.use(
      http.post(URL_METODOS, () =>
        HttpResponse.json({ mensaje: 'El método de pago es inválido', code: 'VALIDATION_ERROR' }, { status: 400 }),
      ),
    )
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel()
    await usuario.type(screen.getByLabelText(/número de tarjeta/i), '4111111111111112')
    await usuario.type(screen.getByLabelText(/caducidad/i), '01/20')
    await usuario.type(screen.getByLabelText(/cvv/i), '987')
    await usuario.type(screen.getByLabelText(/titular/i), 'Juan Pérez')

    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))

    expect(
      await screen.findByText('No se ha podido guardar el método de pago. Revisa los datos: puede que la tarjeta esté caducada.'),
    ).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
    // Ningún texto de la página repite el número ni el CVV (los campos los conservan para corregirlos)
    expect(document.body.textContent).not.toContain('4111111111111112')
    expect(document.body.textContent).not.toContain('987')
  })

  it('HU6-CP33: al superar el límite de métodos avisa de que ya hay 5', async () => {
    // P. ej., se ha añadido otro desde otra pestaña mientras el panel estaba abierto
    server.use(
      http.post(URL_METODOS, () =>
        HttpResponse.json({ code: 'PAYMENT_METHOD_LIMIT_REACHED' }, { status: 409 }),
      ),
    )
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel()
    await usuario.click(screen.getByRole('button', { name: 'Bizum' }))
    await usuario.type(screen.getByLabelText(/teléfono/i), '612345678')

    await usuario.click(screen.getByRole('button', { name: /guardar método de pago/i }))

    expect(await screen.findByText('Ya tienes 5 métodos de pago. Elimina uno para añadir otro.')).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })
})
