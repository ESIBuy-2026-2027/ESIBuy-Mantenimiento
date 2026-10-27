import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'
import { CARRITO_VACIO, conCarrito } from '../../test/cart'
import { renderWithProviders } from '../../test/utils'
import CartWidget from './CartWidget'

const CARRITO = {
  grupos: [
    {
      vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
      total: 35.43,
      lineas: [
        { productoId: 'p-1', nombre: 'Auriculares inalámbricos', imagen: '', cantidad: 2, precioUnitario: 12.5, subtotal: 25, stock: 34 },
        { productoId: 'p-2', nombre: 'Teclado mecánico', imagen: '', cantidad: 1, precioUnitario: 10.43, subtotal: 10.43, stock: 1 },
      ],
    },
    {
      vendedor: { id: 'u-5', nombreComercial: 'Casa Vicente' },
      total: 10,
      lineas: [
        { productoId: 'p-8', nombre: 'Lámpara de escritorio LED', imagen: '', cantidad: 1, precioUnitario: 10, subtotal: 10, stock: 5 },
      ],
    },
  ],
  totalProductos: 4,
  total: 45.43,
}

const pintar = (carrito = {}) => renderWithProviders(conCarrito(<CartWidget />, { cart: CARRITO, ...carrito }))

const botonDelCarrito = () => screen.getByRole('button', { name: /^carrito/i })

async function abrirPanel() {
  await userEvent.click(botonDelCarrito())
  return screen.findByRole('region', { name: /tu carrito/i })
}

describe('CartWidget (HU9): botón flotante', () => {
  it('HU9-CP05: indica cuántos productos hay en el carrito', () => {
    pintar()

    expect(botonDelCarrito()).toHaveAccessibleName('Carrito, 4 productos')
  })

  it('HU9-CP05: con un solo producto lo dice en singular', () => {
    pintar({ cart: { ...CARRITO, totalProductos: 1 } })

    expect(botonDelCarrito()).toHaveAccessibleName('Carrito, 1 producto')
  })

  it('HU9-CP05: con el carrito vacío lo indica', () => {
    pintar({ cart: CARRITO_VACIO })

    expect(botonDelCarrito()).toHaveAccessibleName('Carrito, vacío')
  })

  it('HU9-CP04: quien no tiene carrito (no es cliente) no ve el botón', () => {
    pintar({ status: 'idle', cart: CARRITO_VACIO })

    expect(screen.queryByRole('button', { name: /^carrito/i })).not.toBeInTheDocument()
  })

  it('HU9-CP05: el panel está cerrado hasta que se pulsa el botón', () => {
    pintar()

    expect(screen.queryByRole('region', { name: /tu carrito/i })).not.toBeInTheDocument()
  })
})

describe('CartWidget (HU9): panel del carrito', () => {
  it('HU9-CP05: separa los productos por proveedor, con una cabecera «Proveedor: total» por cada uno', async () => {
    pintar()
    const panel = await abrirPanel()

    const cabeceras = within(panel).getAllByRole('heading', { level: 3 })
    expect(cabeceras.map((cabecera) => cabecera.textContent.replace(/ /g, ' '))).toEqual([
      'TecnoVera: 35,43 €',
      'Casa Vicente: 10,00 €',
    ])
  })

  it('HU9-CP05: cada proveedor agrupa sus propios productos', async () => {
    pintar()
    const panel = await abrirPanel()

    const grupoTecno = within(panel).getByRole('group', { name: /tecnovera/i })
    expect(within(grupoTecno).getByText('Auriculares inalámbricos')).toBeInTheDocument()
    expect(within(grupoTecno).getByText('Teclado mecánico')).toBeInTheDocument()
    expect(within(grupoTecno).queryByText('Lámpara de escritorio LED')).not.toBeInTheDocument()
    const grupoCasa = within(panel).getByRole('group', { name: /casa vicente/i })
    expect(within(grupoCasa).getByText('Lámpara de escritorio LED')).toBeInTheDocument()
  })

  it('HU9-CP05: cada producto muestra su cantidad y lo que cuesta ese producto', async () => {
    pintar()
    const panel = await abrirPanel()

    const grupo = within(panel).getByRole('group', { name: /tecnovera/i })
    expect(within(grupo).getByLabelText('Cantidad de Auriculares inalámbricos')).toHaveTextContent('2')
    expect(within(grupo).getByText(/25,00/)).toBeInTheDocument()
    expect(within(grupo).getByText(/12,50 por unidad/i)).toBeInTheDocument()
  })

  it('HU9-CP05: cada producto es un enlace a su detalle', async () => {
    pintar()
    const panel = await abrirPanel()

    expect(within(panel).getByRole('link', { name: 'Teclado mecánico' })).toHaveAttribute('href', '/products/p-2')
  })

  it('HU9-CP05: el panel se cierra con el botón Cerrar y con Escape', async () => {
    const user = userEvent.setup()
    pintar()
    const panel = await abrirPanel()

    await user.click(within(panel).getByRole('button', { name: /cerrar carrito/i }))
    expect(screen.queryByRole('region', { name: /tu carrito/i })).not.toBeInTheDocument()

    await abrirPanel()
    await user.keyboard('{Escape}')
    expect(screen.queryByRole('region', { name: /tu carrito/i })).not.toBeInTheDocument()
  })

  it('HU9-CP05: con el carrito vacío lo indica y no ofrece pagar', async () => {
    pintar({ cart: CARRITO_VACIO })
    const panel = await abrirPanel()

    expect(within(panel).getByText(/tu carrito está vacío/i)).toBeInTheDocument()
    expect(within(panel).queryByRole('link', { name: /proceder al pago/i })).not.toBeInTheDocument()
    expect(within(panel).queryByRole('button', { name: /vaciar carrito/i })).not.toBeInTheDocument()
  })

  it('HU9-CP08: «Proceder al pago» lleva a la pasarela de pago', async () => {
    pintar()
    const panel = await abrirPanel()

    expect(within(panel).getByRole('link', { name: /proceder al pago/i })).toHaveAttribute('href', '/checkout')
  })
})

describe('CartWidget (HU9): modificar el carrito', () => {
  it('HU9-CP06: se puede subir y bajar la cantidad de un producto', async () => {
    const user = userEvent.setup()
    const setQuantity = vi.fn().mockResolvedValue(undefined)
    pintar({ setQuantity })
    const panel = await abrirPanel()
    const grupo = within(panel).getByRole('group', { name: /tecnovera/i })
    const linea = within(grupo).getByText('Auriculares inalámbricos').closest('li')

    await user.click(within(linea).getByRole('button', { name: /aumentar cantidad/i }))
    await user.click(within(linea).getByRole('button', { name: /disminuir cantidad/i }))

    expect(setQuantity).toHaveBeenNthCalledWith(1, 'p-1', 3)
    expect(setQuantity).toHaveBeenNthCalledWith(2, 'p-1', 1)
  })

  it('HU9-CP06: no se puede pedir más de lo que hay en existencias', async () => {
    pintar()
    const panel = await abrirPanel()
    const linea = within(panel).getByText('Teclado mecánico').closest('li')

    expect(within(linea).getByRole('button', { name: /aumentar cantidad/i })).toBeDisabled()
  })

  it('HU9-CP06: se puede quitar un producto del carrito', async () => {
    const user = userEvent.setup()
    const remove = vi.fn().mockResolvedValue(undefined)
    pintar({ remove })
    const panel = await abrirPanel()

    await user.click(within(panel).getByRole('button', { name: /quitar teclado mecánico/i }))

    expect(remove).toHaveBeenCalledWith('p-2')
  })

  it('HU9-CP06: se puede vaciar el carrito entero', async () => {
    const user = userEvent.setup()
    const clear = vi.fn().mockResolvedValue(undefined)
    pintar({ clear })
    const panel = await abrirPanel()

    await user.click(within(panel).getByRole('button', { name: /vaciar carrito/i }))

    expect(clear).toHaveBeenCalledTimes(1)
  })

  it('HU9-CP06: si el servidor rechaza la cantidad por existencias, lo avisa', async () => {
    const user = userEvent.setup()
    const setQuantity = vi.fn().mockRejectedValue(new ApiError(409, 'Sin existencias', 'INSUFFICIENT_STOCK'))
    pintar({ setQuantity })
    const panel = await abrirPanel()
    const linea = within(panel).getByText('Auriculares inalámbricos').closest('li')

    await user.click(within(linea).getByRole('button', { name: /aumentar cantidad/i }))

    expect(await within(panel).findByRole('alert')).toHaveTextContent(/no hay existencias suficientes/i)
  })

  it('HU9-CP06: si falla al quitar un producto, avisa de que no se ha podido', async () => {
    const user = userEvent.setup()
    const remove = vi.fn().mockRejectedValue(new ApiError(500, 'Error'))
    pintar({ remove })
    const panel = await abrirPanel()

    await user.click(within(panel).getByRole('button', { name: /quitar teclado mecánico/i }))

    expect(await within(panel).findByRole('alert')).toHaveTextContent(/no se ha podido actualizar el carrito/i)
  })
})

describe('CartWidget (HU9): estados', () => {
  it('HU9-CP07: mientras carga el carrito lo indica', async () => {
    pintar({ status: 'loading', cart: CARRITO_VACIO })
    const panel = await abrirPanel()

    expect(within(panel).getByText(/cargando carrito/i)).toBeInTheDocument()
  })

  it('HU9-CP07: si no carga, avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    const reload = vi.fn()
    pintar({ status: 'failed', cart: CARRITO_VACIO, reload })
    const panel = await abrirPanel()

    expect(within(panel).getByText(/no se ha podido cargar el carrito/i)).toBeInTheDocument()
    await user.click(within(panel).getByRole('button', { name: /reintentar/i }))

    expect(reload).toHaveBeenCalledTimes(1)
  })
})

describe('CartWidget (HU9): con el carrito del servidor', () => {
  it('HU9-CP04: un cliente ve el carrito y un vendedor no', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/seller/products')
    await screen.findByRole('heading', { level: 1, name: /mis productos/i })

    expect(screen.queryByRole('button', { name: /^carrito/i })).not.toBeInTheDocument()
  })

  it('HU9-CP05: un producto añadido desde el listado aparece en el carrito, bajo su proveedor', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/products')
    expect(await screen.findByRole('button', { name: 'Carrito, vacío' })).toBeInTheDocument()

    const lista = await screen.findByRole('list', { name: /^productos$/i })
    await waitFor(() => expect(within(lista).getAllByRole('listitem').length).toBeGreaterThan(0))
    const tarjeta = within(lista).getByRole('link', { name: /auriculares/i }).closest('li')
    await user.click(within(tarjeta).getByRole('button', { name: /añadir al carrito/i }))

    await user.click(await screen.findByRole('button', { name: 'Carrito, 1 producto' }))
    const panel = await screen.findByRole('region', { name: /tu carrito/i })
    expect(within(panel).getByRole('heading', { level: 3 }).textContent.replace(/ /g, ' ')).toBe('TecnoVera: 53,91 €')
    expect(within(panel).getByText('Auriculares inalámbricos')).toBeInTheDocument()
  })

  it('HU9-CP07: el carrito vive en el servidor: al recargar la página sigue ahí', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    const primera = renderApp('/products')
    const lista = await screen.findByRole('list', { name: /^productos$/i })
    await waitFor(() => expect(within(lista).getAllByRole('listitem').length).toBeGreaterThan(0))
    await user.click(within(within(lista).getByRole('link', { name: /auriculares/i }).closest('li')).getByRole('button', { name: /añadir al carrito/i }))
    await screen.findByRole('button', { name: 'Carrito, 1 producto' })
    primera.unmount()

    renderApp('/products')

    expect(await screen.findByRole('button', { name: 'Carrito, 1 producto' })).toBeInTheDocument()
  })
})
