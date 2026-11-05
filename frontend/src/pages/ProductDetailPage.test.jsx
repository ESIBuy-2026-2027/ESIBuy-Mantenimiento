import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
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

// Responde el detalle con estos datos (sobre los de ejemplo) en lugar de los del mock, sin valoraciones en la lista
// (HU13): así la media y el número de valoraciones son los del detalle.
const responderCon = (cambios = {}) =>
  server.use(
    http.get('/api/products/:id', () => HttpResponse.json({ ...DETALLE, ...cambios })),
    http.get('/api/products/:id/valoraciones', () => HttpResponse.json([])),
  )

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
      http.post('/api/customer/cart/items', async ({ request }) => {
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

// Valoraciones de los clientes (HU13, T368): GET /api/products/{id}/valoraciones, sin el cliente ni la fecha. La media
// y el número de valoraciones se calculan con esta lista; si no hay ninguna, se usan los del catálogo (decisión del
// equipo: el backend aún no rellena valoracion ni numValoraciones).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU13-CP18 a CP20, propuestos.
const VALORACIONES = [
  { id: 'v-1', productoId: 'p-2', puntuacion: 5, comentario: 'Muy buen tacto, lo recomiendo.' },
  { id: 'v-2', productoId: 'p-2', puntuacion: 4, comentario: '' },
  { id: 'v-3', productoId: 'p-2', puntuacion: 3, comentario: 'Hace algo de ruido.' },
]

const responderValoraciones = (valoraciones) =>
  server.use(http.get('/api/products/:id/valoraciones', () => HttpResponse.json(valoraciones)))

describe('ProductDetailPage (HU13): valoraciones de los clientes', () => {
  it('HU13-CP18: muestra cada valoración del producto con sus estrellas y su comentario', async () => {
    responderCon()
    responderValoraciones(VALORACIONES)
    await abrirDetalle()

    const lista = await screen.findByRole('list', { name: /valoraciones de los clientes/i })
    const valoraciones = within(lista).getAllByRole('listitem')
    expect(valoraciones).toHaveLength(3)
    expect(within(valoraciones[0]).getByRole('img', { name: /valoración: 5,0 de 5/i })).toBeInTheDocument()
    expect(within(valoraciones[0]).getByText('Muy buen tacto, lo recomiendo.')).toBeInTheDocument()
    expect(within(valoraciones[2]).getByText('Hace algo de ruido.')).toBeInTheDocument()
  })

  it('HU13-CP18: una valoración sin comentario muestra solo sus estrellas', async () => {
    responderCon()
    responderValoraciones([VALORACIONES[1]])
    await abrirDetalle()

    const lista = await screen.findByRole('list', { name: /valoraciones de los clientes/i })
    const [valoracion] = within(lista).getAllByRole('listitem')
    expect(within(valoracion).getByRole('img', { name: /valoración: 4,0 de 5/i })).toBeInTheDocument()
    expect(valoracion).toHaveTextContent(/^$/)
  })

  it('HU13-CP19: la media y el número de valoraciones salen de la lista aunque el catálogo no las tenga', async () => {
    responderCon({ valoracion: null, numValoraciones: 0 })
    responderValoraciones(VALORACIONES)
    await abrirDetalle()

    // (5 + 4 + 3) / 3 = 4
    expect((await screen.findAllByRole('img', { name: /valoración: 4,0 de 5/i })).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/^3 valoraciones$/i).length).toBeGreaterThan(0)
    expect(screen.queryByText(/^sin valoraciones$/i)).not.toBeInTheDocument()
  })

  it('HU13-CP19: si la lista está vacía se mantienen la media y el número del catálogo', async () => {
    responderCon()
    responderValoraciones([])
    await abrirDetalle()

    expect(await screen.findByText(/todavía no hay valoraciones de este producto/i)).toBeInTheDocument()
    expect(screen.getAllByText(/29 valoraciones/i).length).toBeGreaterThan(0)
  })

  it('HU13-CP20: un producto sin valoraciones lo indica en su sección', async () => {
    responderCon({ valoracion: null, numValoraciones: 0 })
    responderValoraciones([])
    await abrirDetalle()

    const seccion = await screen.findByRole('region', { name: /valoraciones de los clientes/i })
    expect(within(seccion).getByText(/todavía no hay valoraciones de este producto/i)).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /valoraciones de los clientes/i })).not.toBeInTheDocument()
  })

  it('HU13-CP20: si fallan las valoraciones avisa y permite reintentar, sin perder el producto', async () => {
    const user = userEvent.setup()
    let fallado = false
    responderCon()
    server.use(
      http.get('/api/products/:id/valoraciones', () => {
        if (!fallado) {
          fallado = true
          return new HttpResponse(null, { status: 500 })
        }
        return HttpResponse.json(VALORACIONES)
      }),
    )
    await abrirDetalle()

    expect(await screen.findByText(/no se han podido cargar las valoraciones/i)).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'Teclado mecánico' })).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('list', { name: /valoraciones de los clientes/i })).toBeInTheDocument()
  })
})

// Valorar el producto (HU13, T367): POST /api/products/{id}/valoraciones con { puntuacion, comentario }. El backend
// decide si se puede: 403 PRODUCT_NOT_PURCHASED si el cliente no lo ha comprado y 409 REVIEW_ALREADY_EXISTS si ya lo
// valoró; el frontend no puede saberlo antes de enviar.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU13-CP21 a CP23, propuestos.
function responderAlValorar(respuesta) {
  const enviado = {}
  server.use(
    http.post('/api/products/:id/valoraciones', async ({ params, request }) => {
      enviado.producto = params.id
      enviado.cuerpo = await request.json()
      return respuesta(enviado.cuerpo)
    }),
  )
  return enviado
}

const rechazarCon = (status, code) => () => HttpResponse.json({ code }, { status })

async function abrirFormulario() {
  await abrirDetalle()
  return screen.findByRole('form', { name: /valora este producto/i })
}

describe('ProductDetailPage (HU13): valorar el producto', () => {
  it('HU13-CP21: el cliente valora con estrellas y un comentario, y su valoración aparece en la lista', async () => {
    const user = userEvent.setup()
    let valoraciones = []
    responderCon()
    server.use(http.get('/api/products/:id/valoraciones', () => HttpResponse.json(valoraciones)))
    const enviado = responderAlValorar((cuerpo) => {
      valoraciones = [{ id: 'v-9', productoId: 'p-2', ...cuerpo }]
      return HttpResponse.json(valoraciones[0], { status: 201 })
    })
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('radio', { name: '4 estrellas' }))
    await user.type(within(formulario).getByLabelText(/comentario/i), 'Muy cómodo para escribir.')
    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await screen.findByText(/gracias por tu valoración/i)).toBeInTheDocument()
    expect(enviado).toEqual({ producto: 'p-2', cuerpo: { puntuacion: 4, comentario: 'Muy cómodo para escribir.' } })
    const lista = await screen.findByRole('list', { name: /valoraciones de los clientes/i })
    expect(within(lista).getByText('Muy cómodo para escribir.')).toBeInTheDocument()
    expect(screen.queryByRole('form', { name: /valora este producto/i })).not.toBeInTheDocument()
  })

  it('HU13-CP21: el comentario es opcional: sin él se envía vacío', async () => {
    const user = userEvent.setup()
    responderCon()
    const enviado = responderAlValorar((cuerpo) => HttpResponse.json({ id: 'v-9', ...cuerpo }, { status: 201 }))
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('radio', { name: '5 estrellas' }))
    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await screen.findByText(/gracias por tu valoración/i)).toBeInTheDocument()
    expect(enviado.cuerpo).toEqual({ puntuacion: 5, comentario: '' })
  })

  it('HU13-CP22: sin puntuación no se envía y se pide elegir una', async () => {
    const user = userEvent.setup()
    responderCon()
    const enviado = responderAlValorar(() => HttpResponse.json({}, { status: 201 }))
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await within(formulario).findByText(/elige una puntuación de 1 a 5 estrellas/i)).toBeInTheDocument()
    expect(enviado.cuerpo).toBeUndefined()
  })

  it('HU13-CP22: el comentario admite como mucho 500 caracteres', async () => {
    responderCon()
    const formulario = await abrirFormulario()

    expect(within(formulario).getByLabelText(/comentario/i)).toHaveAttribute('maxLength', '500')
  })

  it('HU13-CP23: si no ha comprado el producto se le avisa y el formulario sigue ahí', async () => {
    const user = userEvent.setup()
    responderCon()
    responderAlValorar(rechazarCon(403, 'PRODUCT_NOT_PURCHASED'))
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('radio', { name: '3 estrellas' }))
    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await within(formulario).findByText(/solo puedes valorar productos que has comprado/i)).toBeInTheDocument()
    expect(screen.getByRole('form', { name: /valora este producto/i })).toBeInTheDocument()
  })

  it('HU13-CP23: si ya lo había valorado se le avisa', async () => {
    const user = userEvent.setup()
    responderCon()
    responderAlValorar(rechazarCon(409, 'REVIEW_ALREADY_EXISTS'))
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('radio', { name: '2 estrellas' }))
    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await within(formulario).findByText(/ya has valorado este producto/i)).toBeInTheDocument()
  })

  it('HU13-CP23: si falla el envío avisa y conserva lo escrito para reintentar', async () => {
    const user = userEvent.setup()
    responderCon()
    responderAlValorar(() => new HttpResponse(null, { status: 500 }))
    const formulario = await abrirFormulario()

    await user.click(within(formulario).getByRole('radio', { name: '4 estrellas' }))
    await user.type(within(formulario).getByLabelText(/comentario/i), 'Buen producto')
    await user.click(within(formulario).getByRole('button', { name: /enviar valoración/i }))

    expect(await within(formulario).findByText(/no se ha podido enviar tu valoración/i)).toBeInTheDocument()
    expect(within(formulario).getByLabelText(/comentario/i)).toHaveValue('Buen producto')
    expect(within(formulario).getByRole('radio', { name: '4 estrellas' })).toBeChecked()
  })
})

// Lista de deseos del cliente (HU14) con el teclado (p-2) dentro.
const CON_EL_TECLADO = {
  items: [
    {
      productoId: 'p-2',
      nombre: 'Teclado mecánico',
      imagen: '',
      precioFinal: 89.5,
      stock: 12,
      disponible: true,
      addedAt: '2026-11-01T10:00:00Z',
    },
  ],
}

describe('ProductDetailPage (HU14): favoritos', () => {
  it('HU14-CP38: el detalle tiene un botón para añadir el producto a favoritos', async () => {
    responderCon()
    await abrirDetalle()

    expect(await screen.findByRole('button', { name: 'Añadir a favoritos' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Quitar de favoritos' })).not.toBeInTheDocument()
  })

  it('HU14-CP38: si el producto ya está en favoritos, el botón permite quitarlo', async () => {
    responderCon()
    server.use(http.get('/api/customer/wishlist', () => HttpResponse.json(CON_EL_TECLADO)))
    await abrirDetalle()

    expect(await screen.findByRole('button', { name: 'Quitar de favoritos' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Añadir a favoritos' })).not.toBeInTheDocument()
  })

  it('HU14-CP38: un producto agotado también se puede añadir a favoritos', async () => {
    responderCon({ stock: 0 })
    await abrirDetalle()

    expect(await screen.findByRole('button', { name: 'Añadir a favoritos' })).toBeEnabled()
  })

  it('HU14-CP38: si no se pueden leer los favoritos, se ofrece añadirlo sin avisar de ningún error', async () => {
    responderCon()
    server.use(http.get('/api/customer/wishlist', () => new HttpResponse(null, { status: 500 })))
    await abrirDetalle()

    expect(await screen.findByRole('button', { name: 'Añadir a favoritos' })).toBeEnabled()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('HU14-CP39: al pulsar Añadir a favoritos se añade el producto y el botón pasa a quitarlo', async () => {
    const user = userEvent.setup()
    let cuerpo = null
    responderCon()
    server.use(
      http.post('/api/customer/wishlist/items', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json({ items: [{ productoId: 'p-2', addedAt: '2026-11-05T10:00:00Z' }] })
      }),
    )
    await abrirDetalle()

    await user.click(await screen.findByRole('button', { name: 'Añadir a favoritos' }))

    expect(await screen.findByRole('button', { name: 'Quitar de favoritos' })).toBeEnabled()
    expect(cuerpo).toEqual({ productoId: 'p-2' })
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products\/p-2$/)
  })

  it('HU14-CP39: mientras se añade, el botón queda deshabilitado', async () => {
    const user = userEvent.setup()
    responderCon()
    server.use(
      http.post('/api/customer/wishlist/items', async () => {
        await delay('infinite')
      }),
    )
    await abrirDetalle()

    await user.click(await screen.findByRole('button', { name: 'Añadir a favoritos' }))

    expect(screen.getByRole('button', { name: 'Añadir a favoritos' })).toBeDisabled()
  })
})
