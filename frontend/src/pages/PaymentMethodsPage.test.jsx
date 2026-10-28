import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Vista «Métodos de pago» del cliente (HU6, AB#269 y AB#113), con los mocks de MSW del backend.
// Del mock de métodos de pago: Carlos (cliente@esibuy.test) tiene una tarjeta predeterminada (•••• 1111) y un Bizum
// (•••• 678).

const URL_METODOS = '/api/customer/payment-methods'

async function abrirMetodosDePago(email = 'cliente@esibuy.test') {
  await abrirSesionPorApi(email)
  renderApp('/payment-methods')
  await screen.findByRole('heading', { level: 1, name: /métodos de pago/i })
}

const tarjetasDeMetodo = () => within(screen.getByRole('list', { name: /tus métodos de pago/i })).getAllByRole('listitem')

describe('PaymentMethodsPage (HU6): listado', () => {
  it('HU6-CP26: muestra cada método con su tipo y su máscara, sin otros datos de pago', async () => {
    await abrirMetodosDePago()

    const metodos = await screen.findAllByRole('listitem')
    expect(metodos).toHaveLength(2)
    expect(tarjetasDeMetodo()[0]).toHaveTextContent('Tarjeta')
    expect(tarjetasDeMetodo()[0]).toHaveTextContent('•••• 1111')
    expect(tarjetasDeMetodo()[1]).toHaveTextContent('Bizum')
    expect(tarjetasDeMetodo()[1]).toHaveTextContent('•••• 678')
  })

  it('HU6-CP26: indica cuál es el método predeterminado', async () => {
    await abrirMetodosDePago()

    const [tarjeta, bizum] = await screen.findAllByRole('listitem')
    expect(within(tarjeta).getByText('Predeterminado')).toBeInTheDocument()
    expect(within(bizum).queryByText('Predeterminado')).not.toBeInTheDocument()
  })

  it('HU6-CP26: sin métodos muestra un estado vacío con el botón para añadir el primero', async () => {
    server.use(http.get(URL_METODOS, () => HttpResponse.json([])))

    await abrirMetodosDePago()

    expect(await screen.findByRole('heading', { level: 2, name: /aún no tienes métodos de pago/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /añadir método de pago/i })).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /tus métodos de pago/i })).not.toBeInTheDocument()
  })

  it('HU6-CP26: si falla la carga avisa y deja reintentar', async () => {
    // Solo la primera petición falla: al reintentar responde el mock normal
    server.use(http.get(URL_METODOS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    const usuario = userEvent.setup()

    await abrirMetodosDePago()

    expect(await screen.findByText(/no se han podido cargar tus métodos de pago/i)).toBeInTheDocument()
    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))
    expect(await screen.findAllByRole('listitem')).toHaveLength(2)
  })
})

describe('PaymentMethodsPage (HU6): acceso', () => {
  it('HU6-CP27: el cliente tiene el enlace «Métodos de pago» en la cabecera y llega a la vista', async () => {
    const usuario = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    const cuenta = await screen.findByRole('navigation', { name: 'Cuenta' })
    await usuario.click(within(cuenta).getByRole('link', { name: /métodos de pago/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /métodos de pago/i })).toBeInTheDocument()
  })

  it('HU6-CP27: un vendedor o un administrador no acceden a la vista', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('PaymentMethodsPage (HU6): predeterminado', () => {
  it('HU6-CP28: marcar otro método como predeterminado lo cambia en la lista', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP28: si el método ya no existe avisa y recarga la lista', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('PaymentMethodsPage (HU6): límite', () => {
  it('HU6-CP29: con 5 métodos no deja añadir otro y explica por qué', () => {
    expect.fail('Funcionalidad no implementada')
  })
})
