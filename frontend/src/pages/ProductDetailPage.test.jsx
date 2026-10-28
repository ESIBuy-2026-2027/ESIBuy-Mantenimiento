import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const CLIENTE = 'cliente@esibuy.test'

const DETALLE = {
  id: 'p-2',
  nombre: 'Teclado mecánico',
  descripcion: 'Teclado con switches táctiles y retroiluminación RGB.',
  imagen: 'https://tienda.test/teclado.png',
  categoriaId: 'cat-1',
  vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
  precio: 89.5,
  descuento: null,
  precioFinal: 89.5,
  stock: 12,
  disponible: true,
  valoracion: 4.7,
  numValoraciones: 29,
}

// Responde el detalle con estos datos (sobre los de ejemplo) en lugar de los del mock.
const responderCon = (cambios = {}) =>
  server.use(http.get('/api/products/:id', () => HttpResponse.json({ ...DETALLE, ...cambios })))

async function abrirDetalle(ruta = '/products/p-2') {
  await abrirSesionPorApi(CLIENTE)
  renderApp(ruta)
  return screen.findByRole('heading', { level: 1 })
}

describe('ProductDetailPage (HU8): acceso', () => {
  it('HU8-CP01: sin sesión, el detalle de un producto lleva a iniciar sesión', async () => {
    renderApp('/products/p-2')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU8-CP01: un vendedor no ve el detalle del cliente: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/products/p-2')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
  })

  it('HU8-CP01: un cliente ve el detalle del producto con su nombre como título', async () => {
    responderCon()

    expect(await abrirDetalle()).toHaveTextContent('Teclado mecánico')
  })
})

describe('ProductDetailPage (HU8): información del producto', () => {
  it('HU8-CP02: muestra la imagen, la categoría, el proveedor y la descripción', async () => {
    responderCon()
    await abrirDetalle()

    expect(screen.getByRole('img', { name: 'Teclado mecánico' })).toHaveAttribute('src', DETALLE.imagen)
    expect(await screen.findAllByText('Tecnología')).not.toHaveLength(0)
    expect(screen.getByText(/vendido por/i)).toHaveTextContent('TecnoVera')
    expect(screen.getByText(DETALLE.descripcion)).toBeInTheDocument()
  })

  it('HU8-CP02: muestra la valoración en estrellas, su nota y cuántas valoraciones tiene', async () => {
    responderCon()
    await abrirDetalle()

    expect(screen.getAllByRole('img', { name: /valoración: 4,7 de 5/i }).length).toBeGreaterThan(0)
    expect(screen.getAllByText('4,7').length).toBeGreaterThan(0)
    expect(screen.getAllByText(/29 valoraciones/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP02: con una sola valoración lo dice en singular', async () => {
    responderCon({ numValoraciones: 1 })
    await abrirDetalle()

    expect(screen.getAllByText(/1 valoración$/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP02: la ficha recoge la categoría, el proveedor, la disponibilidad y la referencia', async () => {
    responderCon()
    await abrirDetalle()

    const ficha = screen.getByRole('region', { name: /información del producto/i })
    expect(within(ficha).getByText('Categoría')).toBeInTheDocument()
    expect(within(ficha).getByText('Proveedor')).toBeInTheDocument()
    expect(within(ficha).getByText('TecnoVera')).toBeInTheDocument()
    expect(within(ficha).getByText('Disponibilidad')).toBeInTheDocument()
    expect(within(ficha).getByText('Referencia')).toBeInTheDocument()
    expect(within(ficha).getByText('p-2')).toBeInTheDocument()
  })

  it('HU8-CP02: con los datos de ejemplo muestra un producto completo', async () => {
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products/p-2')

    expect(await screen.findByRole('heading', { level: 1, name: 'Teclado mecánico' })).toBeInTheDocument()
    expect(screen.getByText(/vendido por/i)).toHaveTextContent('TecnoVera')
    expect(screen.getAllByText(/\d+ valoraciones/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP07: un producto sin valoraciones lo indica en lugar de mostrar estrellas', async () => {
    responderCon({ valoracion: null, numValoraciones: 0 })
    await abrirDetalle()

    expect(screen.getAllByText(/sin valoraciones/i).length).toBeGreaterThan(0)
    expect(screen.queryByRole('img', { name: /valoración:/i })).not.toBeInTheDocument()
  })

  it('HU8-CP07: un producto sin imagen muestra un marcador en su lugar', async () => {
    responderCon({ imagen: '' })
    await abrirDetalle()

    expect(screen.queryByRole('img', { name: 'Teclado mecánico' })).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: /sin imagen/i })).toBeInTheDocument()
  })

  it('HU8-CP07: un producto sin descripción lo indica', async () => {
    responderCon({ descripcion: '' })
    await abrirDetalle()

    expect(screen.getByText(/este producto no tiene descripción/i)).toBeInTheDocument()
  })
})

describe('ProductDetailPage (HU8): precio', () => {
  it('HU8-CP03: sin descuento muestra solo el precio', async () => {
    responderCon()
    await abrirDetalle()

    expect(screen.getAllByText(/89,50/).length).toBeGreaterThan(0)
    expect(screen.queryByText(/ahorras/i)).not.toBeInTheDocument()
  })

  it('HU8-CP03: con descuento muestra el precio final, el original tachado, el porcentaje y lo que se ahorra', async () => {
    responderCon({ precio: 59.9, descuento: { porcentaje: 10 }, precioFinal: 53.91 })
    await abrirDetalle()

    expect(screen.getByText(/53,91/)).toBeInTheDocument()
    expect(screen.getByText(/59,90/).closest('s')).not.toBeNull()
    expect(screen.getByText('-10 %')).toBeInTheDocument()
    expect(screen.getByText(/ahorras 5,99/i)).toBeInTheDocument()
  })

  it('HU8-CP03: el precio lo calcula el servidor: se muestra el que llega, no uno recalculado', async () => {
    responderCon({ precio: 100, descuento: { porcentaje: 25 }, precioFinal: 70 })
    await abrirDetalle()

    expect(screen.getByText(/70,00/)).toBeInTheDocument()
    expect(screen.getByText(/ahorras 30,00/i)).toBeInTheDocument()
  })
})

describe('ProductDetailPage (HU8): disponibilidad', () => {
  it('HU8-CP04: con existencias muestra que está en stock', async () => {
    responderCon({ stock: 12 })
    await abrirDetalle()

    expect(screen.getAllByText(/en stock/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP04: con pocas unidades avisa de cuántas quedan', async () => {
    responderCon({ stock: 3 })
    await abrirDetalle()

    expect(screen.getAllByText(/solo quedan 3 unidades/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP04: con una sola unidad lo dice en singular', async () => {
    responderCon({ stock: 1 })
    await abrirDetalle()

    expect(screen.getAllByText(/solo queda 1 unidad/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP04: sin existencias muestra que está agotado', async () => {
    responderCon({ stock: 0 })
    await abrirDetalle()

    expect(screen.getAllByText(/agotado/i).length).toBeGreaterThan(0)
  })

  it('HU8-CP04: un producto no disponible lo indica aunque tenga existencias', async () => {
    responderCon({ disponible: false, stock: 8 })
    await abrirDetalle()

    expect(screen.getAllByText(/no disponible/i).length).toBeGreaterThan(0)
    expect(screen.queryByText(/en stock/i)).not.toBeInTheDocument()
  })
})

describe('ProductDetailPage (HU8): estados de la pantalla', () => {
  it('HU8-CP05: mientras carga muestra un indicador de carga', async () => {
    server.use(
      http.get('/api/products/:id', async () => {
        await new Promise((resolver) => setTimeout(resolver, 200))
        return HttpResponse.json(DETALLE)
      }),
    )
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products/p-2')

    expect(await screen.findByText(/cargando producto/i)).toBeInTheDocument()
    expect(await screen.findByRole('heading', { level: 1, name: 'Teclado mecánico' })).toBeInTheDocument()
  })

  it('HU8-CP05: si el producto no existe lo indica y ofrece volver a los productos', async () => {
    server.use(http.get('/api/products/:id', () => new HttpResponse(null, { status: 404 })))
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products/inexistente')

    expect(await screen.findByText(/no se ha encontrado el producto/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /volver a los productos/i })).toHaveAttribute('href', '/products')
  })

  it('HU8-CP05: si falla la carga avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    let fallado = false
    server.use(
      http.get('/api/products/:id', () => {
        if (!fallado) {
          fallado = true
          return new HttpResponse(null, { status: 500 })
        }
        return HttpResponse.json(DETALLE)
      }),
    )
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products/p-2')

    expect(await screen.findByText(/no se ha podido cargar el producto/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('heading', { level: 1, name: 'Teclado mecánico' })).toBeInTheDocument()
  })

  it('HU8-CP06: desde el detalle se puede volver a los productos', async () => {
    responderCon()
    await abrirDetalle()

    expect(screen.getAllByRole('link', { name: /volver a los productos/i })[0]).toHaveAttribute('href', '/products')
  })

  it('HU8-CP05: un producto que el catálogo no ofrece (no disponible) se trata como no encontrado', async () => {
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products/p-6')

    expect(await screen.findByText(/no se ha encontrado el producto/i)).toBeInTheDocument()
  })
})

describe('ProductDetailPage (HU9): añadir al carrito', () => {
  const CARRITO = { grupos: [], totalProductos: 1, total: 89.5 }

  // Guarda el cuerpo de lo que se envía al carrito.
  function capturarAlta() {
    const enviado = { cuerpo: null }
    server.use(
      http.post('/api/cart/items', async ({ request }) => {
        enviado.cuerpo = await request.json()
        return HttpResponse.json(CARRITO)
      }),
    )
    return enviado
  }

  it('HU9-CP03: el detalle tiene un botón para añadir el producto al carrito', async () => {
    const user = userEvent.setup()
    responderCon()
    const enviado = capturarAlta()
    await abrirDetalle()

    await user.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    await waitFor(() => expect(enviado.cuerpo).toEqual({ productoId: 'p-2', cantidad: 1 }))
    expect(await screen.findByText(/añadido/i)).toBeInTheDocument()
  })

  it('HU9-CP03: se puede elegir la cantidad antes de añadir', async () => {
    const user = userEvent.setup()
    responderCon()
    const enviado = capturarAlta()
    await abrirDetalle()

    await user.click(screen.getByRole('button', { name: /aumentar cantidad/i }))
    await user.click(screen.getByRole('button', { name: /aumentar cantidad/i }))
    expect(screen.getByLabelText('Cantidad')).toHaveTextContent('3')
    await user.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    await waitFor(() => expect(enviado.cuerpo).toEqual({ productoId: 'p-2', cantidad: 3 }))
  })

  it('HU9-CP03: la cantidad no baja de una unidad', async () => {
    responderCon()
    await abrirDetalle()

    expect(screen.getByLabelText('Cantidad')).toHaveTextContent('1')
    expect(screen.getByRole('button', { name: /disminuir cantidad/i })).toBeDisabled()
  })

  it('HU9-CP03: la cantidad no puede superar las existencias', async () => {
    const user = userEvent.setup()
    responderCon({ stock: 2 })
    await abrirDetalle()

    await user.click(screen.getByRole('button', { name: /aumentar cantidad/i }))

    expect(screen.getByLabelText('Cantidad')).toHaveTextContent('2')
    expect(screen.getByRole('button', { name: /aumentar cantidad/i })).toBeDisabled()
  })

  it('HU9-CP03: un producto agotado no se puede añadir ni se elige cantidad', async () => {
    responderCon({ stock: 0 })
    await abrirDetalle()

    expect(screen.getByRole('button', { name: /agotado/i })).toBeDisabled()
    expect(screen.queryByLabelText('Cantidad')).not.toBeInTheDocument()
  })
})
