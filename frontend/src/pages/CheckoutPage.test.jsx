import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { addCartItem } from '../api/cart'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const CLIENTE = 'cliente@esibuy.test'
// Esperar a que termine el pago simulado, que tarda un instante.
const PAGO = { timeout: 5000 }

// Dos auriculares (con un 10 % de descuento) y un teclado, de TecnoVera (u-2), y un smartwatch de Otero (u-99): dos
// vendedores en el carrito, pero cada pedido es de uno solo.
const CARRITO_DE_DOS_VENDEDORES = [
  ['p-1', 2],
  ['p-2', 1],
  ['p-24', 1],
]

async function llenarElCarrito(productos) {
  await abrirSesionPorApi(CLIENTE)
  for (const [id, cantidad] of productos) await addCartItem(id, cantidad)
}

// Abre la pasarela de un vendedor y espera a su resumen (paso 1).
async function abrirPasarela({ productos = CARRITO_DE_DOS_VENDEDORES, vendedor = 'u-2' } = {}) {
  await llenarElCarrito(productos)
  renderApp(vendedor ? `/checkout?vendedor=${vendedor}` : '/checkout')
  await screen.findByRole('heading', { level: 1, name: /pasarela de pago/i })
  return screen.findByRole('region', { name: /resumen de la compra/i })
}

const continuar = (user, nombre) => user.click(screen.getByRole('button', { name: nombre }))

// Del resumen al paso del envío, esperando a las direcciones del cliente.
async function irAlEnvio(user) {
  await continuar(user, /continuar con el envío/i)
  return screen.findByRole('radiogroup', { name: /dirección de envío/i })
}

// Del resumen al paso del pago, esperando a los métodos de pago del cliente.
async function irAlPago(user) {
  await irAlEnvio(user)
  await continuar(user, /continuar con el pago/i)
  return screen.findByRole('radiogroup', { name: /método de pago/i })
}

const botonDePago = () => screen.getByRole('button', { name: /simular pago/i })
const lineaDe = (nombre) => screen.getByText(nombre).closest('li')

describe('CheckoutPage (HU10): acceso', () => {
  it('HU10-CP01: sin sesión, la pasarela de pago lleva a iniciar sesión', async () => {
    renderApp('/checkout')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU10-CP01: un vendedor no ve la pasarela del cliente: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/checkout')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
  })

  it('HU10-CP01: desde el carrito, «Pagar a…» lleva a la pasarela de ese vendedor', async () => {
    const user = userEvent.setup()
    await llenarElCarrito([['p-1', 1]])
    renderApp('/products')

    await user.click(await screen.findByRole('button', { name: 'Carrito, 1 producto' }))
    await user.click(await screen.findByRole('link', { name: /pagar a tecnovera/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /pasarela de pago/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/checkout$/)
    // Con el vendedor ya elegido: va directo al resumen de su compra
    expect(await screen.findByRole('region', { name: /resumen de la compra/i })).toBeInTheDocument()
  })
})

describe('CheckoutPage (HU10): un pedido, un solo vendedor', () => {
  it('HU10-CP28: con productos de dos vendedores, sin elegir uno, pregunta a cuál se paga', async () => {
    await llenarElCarrito(CARRITO_DE_DOS_VENDEDORES)
    renderApp('/checkout')

    const eleccion = await screen.findByRole('region', { name: /elegir vendedor/i })
    expect(within(eleccion).getByRole('link', { name: /pagar a tecnovera/i })).toHaveAttribute(
      'href',
      '/checkout?vendedor=u-2',
    )
    expect(within(eleccion).getByRole('link', { name: /pagar a otero hogar y tecnología/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /continuar con el envío/i })).not.toBeInTheDocument()
  })

  it('HU10-CP28: la pasarela de un vendedor solo lleva los productos de ese vendedor', async () => {
    const resumen = await abrirPasarela({ vendedor: 'u-2' })

    expect(within(resumen).getByText('Auriculares inalámbricos')).toBeInTheDocument()
    expect(within(resumen).getByText('Teclado mecánico')).toBeInTheDocument()
    expect(within(resumen).queryByText('Smartwatch deportivo')).not.toBeInTheDocument()
    expect(within(resumen).getByText(/197,32/)).toBeInTheDocument()
    expect(within(resumen).queryByText(/326,32/)).not.toBeInTheDocument()
  })

  it('HU10-CP28: si el carrito solo tiene un vendedor, va directo a su resumen', async () => {
    await llenarElCarrito([['p-24', 1]])
    renderApp('/checkout')

    const resumen = await screen.findByRole('region', { name: /resumen de la compra/i })
    expect(within(resumen).getByText('Smartwatch deportivo')).toBeInTheDocument()
  })

  it('HU10-CP03: con el carrito vacío lo indica, no ofrece pagar y permite volver a los productos', async () => {
    await abrirSesionPorApi(CLIENTE)
    renderApp('/checkout')

    expect(await screen.findByText(/tu carrito está vacío/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /continuar con el envío/i })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver los productos/i })).toHaveAttribute('href', '/products')
  })
})

describe('CheckoutPage (HU10): revisar y editar la compra', () => {
  it('HU10-CP02: el resumen muestra cada producto con su cantidad, su precio y el total del pedido', async () => {
    const resumen = await abrirPasarela()

    expect(within(lineaDe('Auriculares inalámbricos')).getByText(/107,82/)).toBeInTheDocument()
    expect(within(lineaDe('Teclado mecánico')).getAllByText(/89,50/).length).toBeGreaterThan(0)
    expect(within(resumen).getByText(/total a pagar/i)).toBeInTheDocument()
    expect(within(resumen).getAllByText(/197,32/).length).toBeGreaterThan(0)
  })

  it('HU10-CP29: se puede cambiar la cantidad de un producto y el total se actualiza', async () => {
    const user = userEvent.setup()
    const resumen = await abrirPasarela()

    await user.click(within(lineaDe('Auriculares inalámbricos')).getByRole('button', { name: /aumentar cantidad/i }))

    await waitFor(() =>
      expect(screen.getByLabelText('Cantidad de Auriculares inalámbricos')).toHaveTextContent('3'),
    )
    // 3 × 53,91 + 89,50
    expect(await within(resumen).findByText(/251,23/)).toBeInTheDocument()
  })

  it('HU10-CP29: la cantidad no puede pasar de las existencias', async () => {
    await abrirPasarela({ productos: [['p-2', 12]] })

    expect(
      within(lineaDe('Teclado mecánico')).getByRole('button', { name: /aumentar cantidad/i }),
    ).toBeDisabled()
  })

  it('HU10-CP29: se puede quitar un producto y el total se actualiza', async () => {
    const user = userEvent.setup()
    const resumen = await abrirPasarela()

    await user.click(within(lineaDe('Teclado mecánico')).getByRole('button', { name: /quitar teclado mecánico/i }))

    await waitFor(() => expect(within(resumen).queryByText('Teclado mecánico')).not.toBeInTheDocument())
    expect(within(resumen).getAllByText(/107,82/).length).toBeGreaterThan(0)
  })

  it('HU10-CP30: un producto con descuento muestra su precio original tachado, el porcentaje y el precio final', async () => {
    await abrirPasarela()

    const auriculares = lineaDe('Auriculares inalámbricos')
    expect(within(auriculares).getByText(/59,90/).closest('s')).not.toBeNull()
    expect(within(auriculares).getByText(/-10 %/)).toBeInTheDocument()
    expect(within(auriculares).getAllByText(/53,91/).length).toBeGreaterThan(0)
    const teclado = lineaDe('Teclado mecánico')
    expect(within(teclado).queryByText(/ %/)).not.toBeInTheDocument()
  })
})

describe('CheckoutPage (HU10): pasos de la compra', () => {
  it('HU10-CP31: la compra va por pasos y cada uno enseña solo lo suyo', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    const pasos = screen.getByRole('list', { name: /pasos de la compra/i })
    expect(within(pasos).getByText(/1\. compra/i)).toHaveAttribute('aria-current', 'step')
    expect(screen.queryByRole('radiogroup')).not.toBeInTheDocument()

    await irAlEnvio(user)
    expect(within(pasos).getByText(/2\. envío/i)).toHaveAttribute('aria-current', 'step')
    expect(screen.queryByRole('radiogroup', { name: /método de pago/i })).not.toBeInTheDocument()

    await continuar(user, /continuar con el pago/i)
    expect(await screen.findByRole('radiogroup', { name: /método de pago/i })).toBeInTheDocument()
    expect(within(pasos).getByText(/3\. pago/i)).toHaveAttribute('aria-current', 'step')
    expect(screen.queryByRole('radiogroup', { name: /dirección de envío/i })).not.toBeInTheDocument()
  })

  it('HU10-CP31: se puede volver atrás sin perder lo elegido', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlEnvio(user)
    await user.click(screen.getByRole('radio', { name: /calle toledo 5/i }))
    await continuar(user, /continuar con el pago/i)
    await screen.findByRole('radiogroup', { name: /método de pago/i })

    await continuar(user, /volver al envío/i)

    expect(await screen.findByRole('radio', { name: /calle toledo 5/i })).toBeChecked()
  })
})

describe('CheckoutPage (HU10): elegir la dirección de envío', () => {
  it('HU10-CP21: lista las direcciones de envío del cliente y marca la principal', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    const direcciones = await irAlEnvio(user)

    expect(within(direcciones).getAllByRole('radio')).toHaveLength(2)
    expect(within(direcciones).getByRole('radio', { name: /calle mayor 1/i })).toBeChecked()
    expect(within(direcciones).getByRole('radio', { name: /calle toledo 5/i })).not.toBeChecked()
  })

  it('HU10-CP21: se puede elegir otra dirección de envío antes de pagar', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlEnvio(user)

    await user.click(screen.getByRole('radio', { name: /calle toledo 5/i }))

    expect(screen.getByRole('radio', { name: /calle toledo 5/i })).toBeChecked()
    expect(screen.getByRole('radio', { name: /calle mayor 1/i })).not.toBeChecked()
  })

  it('HU10-CP22: sin direcciones de envío no deja continuar y lleva a sus direcciones para añadir una', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/customer/addresses', () => HttpResponse.json([])))
    await abrirPasarela()
    await continuar(user, /continuar con el envío/i)

    expect(await screen.findByText(/no tienes ninguna dirección de envío/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /añadir una dirección/i })).toHaveAttribute('href', '/addresses')
    expect(screen.getByRole('button', { name: /continuar con el pago/i })).toBeDisabled()
  })

  it('HU10-CP22: si no se pueden cargar las direcciones de envío lo indica y deja reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/customer/addresses', () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirPasarela()
    await continuar(user, /continuar con el envío/i)

    expect(await screen.findByText(/no se han podido cargar las direcciones de envío/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /continuar con el pago/i })).toBeDisabled()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('radio', { name: /calle mayor 1/i })).toBeChecked()
    expect(screen.getByRole('button', { name: /continuar con el pago/i })).toBeEnabled()
  })
})

describe('CheckoutPage (HU10): elegir el método de pago', () => {
  it('HU10-CP07: lista los métodos de pago guardados del cliente y marca el predeterminado', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    const metodos = await irAlPago(user)

    expect(within(metodos).getAllByRole('radio')).toHaveLength(2)
    expect(within(metodos).getByRole('radio', { name: /tarjeta.*1111/i })).toBeChecked()
    expect(within(metodos).getByRole('radio', { name: /bizum.*678/i })).not.toBeChecked()
  })

  it('HU10-CP07: se puede elegir otro método de pago antes de pagar', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlPago(user)

    await user.click(screen.getByRole('radio', { name: /bizum.*678/i }))

    expect(screen.getByRole('radio', { name: /bizum.*678/i })).toBeChecked()
    expect(screen.getByRole('radio', { name: /tarjeta.*1111/i })).not.toBeChecked()
  })

  it('HU10-CP08: sin métodos de pago guardados no deja pagar y lleva a sus métodos de pago para añadir uno', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/customer/payment-methods', () => HttpResponse.json([])))
    await abrirPasarela()
    await irAlEnvio(user)
    await continuar(user, /continuar con el pago/i)

    expect(await screen.findByText(/no tienes ningún método de pago/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /añadir un método de pago/i })).toHaveAttribute('href', '/payment-methods')
    expect(botonDePago()).toBeDisabled()
  })

  it('HU10-CP08: si no se pueden cargar los métodos de pago lo indica y deja reintentar', async () => {
    const user = userEvent.setup()
    server.use(
      http.get('/api/customer/payment-methods', () => new HttpResponse(null, { status: 500 }), { once: true }),
    )
    await abrirPasarela()
    await irAlEnvio(user)
    await continuar(user, /continuar con el pago/i)

    expect(await screen.findByText(/no se han podido cargar los métodos de pago/i)).toBeInTheDocument()
    expect(botonDePago()).toBeDisabled()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('radio', { name: /tarjeta.*1111/i })).toBeChecked()
    expect(botonDePago()).toBeEnabled()
  })
})

describe('CheckoutPage (HU10): simular el pago', () => {
  it('HU10-CP04: mientras se procesa el pago lo indica y no deja pulsar otra vez', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(screen.getByRole('button', { name: /procesando pago/i })).toBeDisabled()
    // Se espera a que termine para que el pago no siga en marcha durante el test siguiente.
    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
  })

  it('HU10-CP04: tras pagar se confirma el pedido del vendedor y solo sus productos salen del carrito', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)).toBeInTheDocument()
    expect(screen.getAllByRole('listitem', { name: /pedido/i })).toHaveLength(1)
    // Lo de Otero sigue en el carrito, sin pagar
    expect(await screen.findByRole('button', { name: 'Carrito, 1 producto' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /pagar a otro vendedor/i })).toHaveAttribute('href', '/checkout')
    expect(screen.getByRole('link', { name: /seguir comprando/i })).toHaveAttribute('href', '/products')
  })

  it('HU10-CP04: si no queda nada más en el carrito, no ofrece pagar a otro vendedor', async () => {
    const user = userEvent.setup()
    await abrirPasarela({ productos: [['p-1', 1]] })
    await irAlPago(user)

    await user.click(botonDePago())

    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
    expect(await screen.findByRole('button', { name: 'Carrito, vacío' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /pagar a otro vendedor/i })).not.toBeInTheDocument()
  })

  it('HU10-CP09: el pedido se crea con el vendedor, la dirección, el método de pago y el total que vio el cliente', async () => {
    const user = userEvent.setup()
    const cuerpos = []
    // Anota lo que se envía y deja que responda el mock de siempre.
    server.use(
      http.post('/api/customer/orders', async ({ request }) => {
        cuerpos.push(await request.clone().json())
        return undefined
      }),
    )
    await abrirPasarela()
    await irAlEnvio(user)
    await user.click(screen.getByRole('radio', { name: /calle toledo 5/i }))
    await continuar(user, /continuar con el pago/i)
    await screen.findByRole('radiogroup', { name: /método de pago/i })
    await user.click(screen.getByRole('radio', { name: /bizum.*678/i }))

    await user.click(botonDePago())

    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
    expect(cuerpos).toEqual([{ vendedorId: 'u-2', direccionId: 'd-2', paymentMethodId: 'm-2', totalEsperado: 197.32 }])
  })
})

describe('CheckoutPage (HU10): si el pedido no se puede hacer', () => {
  const responder = (estado, cuerpo, opciones) =>
    server.use(http.post('/api/customer/orders', () => HttpResponse.json(cuerpo, { status: estado }), opciones))

  it('HU10-CP26: si el pago se rechaza no se confirma el pedido, se avisa y se puede probar con otro método', async () => {
    const user = userEvent.setup()
    responder(402, { code: 'PAYMENT_REJECTED', mensaje: 'El pago ha sido rechazado' }, { once: true })
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(await screen.findByRole('alert', {}, PAGO)).toHaveTextContent(/el pago ha sido rechazado/i)
    expect(screen.queryByRole('heading', { level: 1, name: /pago realizado/i })).not.toBeInTheDocument()
    // La compra sigue en el carrito, sin pagar
    expect(await screen.findByRole('button', { name: 'Carrito, 4 productos' })).toBeInTheDocument()

    await user.click(screen.getByRole('radio', { name: /bizum.*678/i }))
    await user.click(botonDePago())

    expect(await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)).toBeInTheDocument()
  })

  it('HU10-CP27: si el total ha cambiado vuelve al resumen y pide revisar la compra', async () => {
    const user = userEvent.setup()
    responder(409, { code: 'TOTAL_MISMATCH', mensaje: 'El total del pedido ha cambiado' }, { once: true })
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(await screen.findByRole('alert', {}, PAGO)).toHaveTextContent(/el importe de tu compra ha cambiado/i)
    expect(await screen.findByRole('region', { name: /resumen de la compra/i })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { level: 1, name: /pago realizado/i })).not.toBeInTheDocument()
  })

  it('HU10-CP27: si falta stock dice de qué producto y vuelve al resumen para ajustarlo', async () => {
    const user = userEvent.setup()
    responder(
      409,
      { code: 'INSUFFICIENT_STOCK', mensaje: 'No queda stock suficiente de «Teclado mecánico»' },
      { once: true },
    )
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(await screen.findByRole('alert', {}, PAGO)).toHaveTextContent(/stock suficiente de «teclado mecánico»/i)
    expect(await screen.findByRole('region', { name: /resumen de la compra/i })).toBeInTheDocument()
  })

  it('HU10-CP27: un fallo inesperado avisa con un mensaje genérico y se puede reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.post('/api/customer/orders', () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    expect(await screen.findByRole('alert', {}, PAGO)).toHaveTextContent(/no se ha podido completar el pedido/i)
    expect(botonDePago()).toBeEnabled()
  })
})

describe('CheckoutPage (HU10): recibo del pago', () => {
  it('HU10-CP10: tras pagar muestra un recibo con el total pagado, el pedido y el método de pago usado', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    const recibo = await screen.findByRole('region', { name: /recibo del pago/i }, PAGO)
    expect(within(recibo).getAllByText(/197,32/).length).toBeGreaterThan(0)
    expect(within(recibo).getByRole('listitem', { name: /pedido o-1/i })).toHaveTextContent(/tecnovera/i)
    expect(within(recibo).getByText(/1111/)).toBeInTheDocument()
  })
})

describe('CheckoutPage: acceso a los pedidos (HU11)', () => {
  it('HU11-CP19: tras pagar se puede ir a Mis pedidos', async () => {
    const user = userEvent.setup()
    await abrirPasarela()
    await irAlPago(user)

    await user.click(botonDePago())

    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
    expect(screen.getByRole('link', { name: /ver mis pedidos/i })).toHaveAttribute('href', '/orders')
  })
})
