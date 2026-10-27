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

// Dos auriculares y un teclado, de TecnoVera (u-2), y un smartwatch de Otero (u-99): dos proveedores.
const CARRITO_DE_DOS_PROVEEDORES = [
  ['p-1', 2],
  ['p-2', 1],
  ['p-24', 1],
]

async function abrirPasarela(productos = CARRITO_DE_DOS_PROVEEDORES) {
  await abrirSesionPorApi(CLIENTE)
  for (const [id, cantidad] of productos) await addCartItem(id, cantidad)
  renderApp('/checkout')
  return screen.findByRole('heading', { level: 1, name: /pasarela de pago/i })
}

const botonDePago = () => screen.getByRole('button', { name: /simular pago/i })

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

  it('HU10-CP01: desde el carrito, «Proceder al pago» lleva a la pasarela', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi(CLIENTE)
    await addCartItem('p-1', 1)
    renderApp('/products')

    await user.click(await screen.findByRole('button', { name: 'Carrito, 1 producto' }))
    await user.click(await screen.findByRole('link', { name: /proceder al pago/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /pasarela de pago/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/checkout$/)
  })
})

describe('CheckoutPage (HU10): resumen del pedido', () => {
  it('HU10-CP02: muestra un pedido por proveedor, con la cabecera «Proveedor: total»', async () => {
    await abrirPasarela()

    const tecnovera = screen.getByRole('region', { name: /pedido de tecnovera/i })
    expect(within(tecnovera).getByRole('heading', { level: 2 })).toHaveTextContent(/tecnovera: 197,32/i)
    const otero = screen.getByRole('region', { name: /pedido de otero hogar y tecnología/i })
    expect(within(otero).getByRole('heading', { level: 2 })).toHaveTextContent(/otero hogar y tecnología: 129,00/i)
  })

  it('HU10-CP02: cada pedido lista solo los productos de su proveedor, con cantidad y precio', async () => {
    await abrirPasarela()

    const tecnovera = screen.getByRole('region', { name: /pedido de tecnovera/i })
    expect(within(tecnovera).getByText('Auriculares inalámbricos')).toBeInTheDocument()
    expect(within(tecnovera).getByText('Teclado mecánico')).toBeInTheDocument()
    expect(within(tecnovera).queryByText('Smartwatch deportivo')).not.toBeInTheDocument()
    expect(within(tecnovera).getByText(/107,82/)).toBeInTheDocument()
    const otero = screen.getByRole('region', { name: /pedido de otero/i })
    expect(within(otero).getByText('Smartwatch deportivo')).toBeInTheDocument()
  })

  it('HU10-CP02: avisa de que se hace un pedido independiente por cada proveedor', async () => {
    await abrirPasarela()

    expect(screen.getByText(/un pedido por cada proveedor/i)).toBeInTheDocument()
  })

  it('HU10-CP02: muestra el importe total a pagar', async () => {
    await abrirPasarela()

    const resumen = screen.getByRole('region', { name: /resumen del pago/i })
    expect(within(resumen).getByText(/326,32/)).toBeInTheDocument()
  })

  it('HU10-CP03: con el carrito vacío lo indica, no ofrece pagar y permite volver a los productos', async () => {
    await abrirSesionPorApi(CLIENTE)
    renderApp('/checkout')

    expect(await screen.findByText(/tu carrito está vacío/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /simular pago/i })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver los productos/i })).toHaveAttribute('href', '/products')
  })
})

describe('CheckoutPage (HU10): simular el pago', () => {
  it('HU10-CP04: mientras se procesa el pago lo indica y no deja pulsar otra vez', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    await user.click(botonDePago())

    expect(screen.getByRole('button', { name: /procesando pago/i })).toBeDisabled()
  })

  it('HU10-CP04: tras pagar se confirma un pedido por cada proveedor', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    await user.click(botonDePago())

    expect(await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)).toBeInTheDocument()
    const pedidos = screen.getAllByRole('listitem', { name: /pedido/i })
    expect(pedidos).toHaveLength(2)
    expect(within(pedidos[0]).getByText(/tecnovera/i)).toBeInTheDocument()
    expect(within(pedidos[0]).getByText(/197,32/)).toBeInTheDocument()
    expect(within(pedidos[1]).getByText(/otero hogar y tecnología/i)).toBeInTheDocument()
    expect(within(pedidos[1]).getByText(/129,00/)).toBeInTheDocument()
  })

  it('HU10-CP04: tras pagar el carrito queda vacío', async () => {
    const user = userEvent.setup()
    await abrirPasarela()

    await user.click(botonDePago())

    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
    expect(await screen.findByRole('button', { name: 'Carrito, vacío' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /seguir comprando/i })).toHaveAttribute('href', '/products')
  })

  it('HU10-CP06: cada pedido se pide con un solo proveedor, uno detrás de otro', async () => {
    const user = userEvent.setup()
    const cuerpos = []
    // Anota lo que se envía y deja que responda el mock de siempre.
    server.use(
      http.post('/api/orders', async ({ request }) => {
        cuerpos.push(await request.clone().json())
        return undefined
      }),
    )
    await abrirPasarela()

    await user.click(botonDePago())

    await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)
    expect(cuerpos).toEqual([{ vendedorId: 'u-2' }, { vendedorId: 'u-99' }])
  })
})

describe('CheckoutPage (HU10): si falla algún pedido', () => {
  const fallarElDe = (vendedorId, veces = Infinity) => {
    let fallos = 0
    server.use(
      http.post('/api/orders', async ({ request }) => {
        const cuerpo = await request.clone().json()
        if (cuerpo.vendedorId === vendedorId && fallos < veces) {
          fallos += 1
          return new HttpResponse(null, { status: 500 })
        }
        return undefined
      }),
    )
  }

  it('HU10-CP05: confirma los pedidos que sí se han creado e indica cuál ha fallado', async () => {
    const user = userEvent.setup()
    fallarElDe('u-99')
    await abrirPasarela()

    await user.click(botonDePago())

    expect(await screen.findByRole('heading', { level: 1, name: /pago realizado/i }, PAGO)).toBeInTheDocument()
    expect(screen.getAllByRole('listitem', { name: /pedido/i })).toHaveLength(1)
    expect(screen.getByRole('alert')).toHaveTextContent(/no se ha podido crear el pedido de otero hogar y tecnología/i)
  })

  it('HU10-CP05: la compra del pedido que ha fallado se conserva en el carrito', async () => {
    const user = userEvent.setup()
    fallarElDe('u-99')
    await abrirPasarela()

    await user.click(botonDePago())

    await screen.findByRole('alert', {}, PAGO)
    expect(await screen.findByRole('button', { name: 'Carrito, 1 producto' })).toBeInTheDocument()
  })

  it('HU10-CP05: se puede reintentar el pago de lo que falló, sin repetir lo ya pagado', async () => {
    const user = userEvent.setup()
    fallarElDe('u-99', 1)
    await abrirPasarela()
    await user.click(botonDePago())
    await screen.findByRole('alert', {}, PAGO)

    await user.click(await screen.findByRole('button', { name: /reintentar el pago/i }))

    await waitFor(() => expect(screen.queryByRole('alert')).not.toBeInTheDocument(), PAGO)
    expect(screen.getAllByRole('listitem', { name: /pedido/i })).toHaveLength(2)
    expect(await screen.findByRole('button', { name: 'Carrito, vacío' })).toBeInTheDocument()
  })
})
