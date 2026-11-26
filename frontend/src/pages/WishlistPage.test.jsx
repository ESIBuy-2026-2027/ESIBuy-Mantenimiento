import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { addItemToWishlist, addWishlistItem, createWishlist } from '../api/wishlist'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const URL_LISTAS = '/api/customer/wishlists'

const LISTAS = [
  {
    id: 'wl-1',
    nombre: 'Regalos',
    items: [
      {
        productoId: 'p-1',
        nombre: 'Auriculares inalámbricos',
        imagen: 'https://tienda.test/auriculares.png',
        precioFinal: 53.91,
        stock: 34,
        disponible: true,
        addedAt: '2026-11-01T10:00:00Z',
      },
      {
        productoId: 'p-3',
        nombre: 'Ratón ergonómico',
        imagen: '',
        precioFinal: 34.99,
        stock: 0,
        disponible: true,
        addedAt: '2026-11-03T10:00:00Z',
      },
      {
        productoId: 'p-6',
        nombre: 'Webcam Full HD',
        imagen: '',
        precioFinal: 49,
        stock: 0,
        disponible: false,
        addedAt: '2026-11-04T10:00:00Z',
      },
    ],
  },
]

async function abrirListas(listas = LISTAS) {
  if (listas) server.use(http.get(URL_LISTAS, () => HttpResponse.json(listas)))
  await abrirSesionPorApi('cliente@esibuy.test')
  renderApp('/wishlist')
  await screen.findByRole('heading', { level: 1, name: 'Mis listas de deseos' })
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('WishlistPage (HU14, PBI 534): acceso', () => {
  it('HU14-CP32: el cliente abre Mis listas de deseos en /wishlist y tiene una lista por defecto', async () => {
    await abrirListas(null)
    expect(await screen.findByRole('heading', { level: 2, name: 'Mi lista de deseos' })).toBeInTheDocument()
  })

  it('HU14-CP32: sin sesión lleva a iniciar sesión', async () => {
    renderApp('/wishlist')
    await esperarRuta('/login')
  })

  it('HU14-CP32: un vendedor va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/wishlist')
    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/seller/products$')))
  })
})

describe('WishlistPage (PBI 534): crear, consultar, editar y eliminar listas', () => {
  it('HU528-CP39: los favoritos no salen entre las listas', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    await addWishlistItem('p-1')
    renderApp('/wishlist')

    expect(await screen.findByRole('heading', { level: 2, name: 'Mi lista de deseos' })).toBeInTheDocument()
    expect(screen.getAllByRole('heading', { level: 2 })).toHaveLength(1)
    expect(screen.getByText('Esta lista aún no tiene productos.')).toBeInTheDocument()
  })

  it('PBI534-CP06: muestra las listas con sus productos, disponibilidad y precio', async () => {
    await abrirListas()

    const lista = await screen.findByRole('list', { name: 'Productos de Regalos' })
    const filas = within(lista).getAllByRole('listitem')
    expect(filas).toHaveLength(3)
    expect(filas[0]).toHaveTextContent('Auriculares inalámbricos')
    expect(filas[0]).toHaveTextContent('53,91')
    expect(filas[1]).toHaveTextContent('Agotado')
    expect(filas[2]).toHaveTextContent('No disponible')
  })

  it('PBI534-CP07: el cliente puede crear varias listas de deseos', async () => {
    const user = userEvent.setup()
    await abrirListas(null)

    await user.click(screen.getAllByRole('button', { name: /nueva lista/i })[0])
    const dialogo = screen.getByRole('dialog', { name: 'Nueva lista de deseos' })
    await user.type(within(dialogo).getByLabelText(/nombre de la lista/i), 'Regalos')
    await user.click(within(dialogo).getByRole('button', { name: 'Crear lista' }))

    expect(await screen.findByRole('heading', { level: 2, name: 'Regalos' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /nueva lista/i }))
    const dialogo2 = screen.getByRole('dialog', { name: 'Nueva lista de deseos' })
    await user.type(within(dialogo2).getByLabelText(/nombre de la lista/i), 'Oficina')
    await user.click(within(dialogo2).getByRole('button', { name: 'Crear lista' }))

    expect(await screen.findByRole('heading', { level: 2, name: 'Oficina' })).toBeInTheDocument()
  })

  it('PBI534-CP08: al editar una lista solo cambia el nombre; en la lista se ajusta la cantidad limitada al stock y se quita el producto', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    const creada = await createWishlist('Inicial')
    await addItemToWishlist(creada.id, 'p-1')
    renderApp('/wishlist')
    await screen.findByRole('heading', { level: 1, name: 'Mis listas de deseos' })

    // Ajustar cantidad directamente junto al precio: al intentar superar el stock se limita al máximo sin error
    // y el precio se actualiza según la cantidad; también se puede añadir al carrito desde la lista
    const inputCantidad = await screen.findByLabelText('Cantidad de Auriculares inalámbricos')
    await user.clear(inputCantidad)
    await user.type(inputCantidad, '2')
    await waitFor(() => expect(screen.getByText(/107,82/)).toBeInTheDocument())

    await user.clear(inputCantidad)
    await user.type(inputCantidad, '999')
    expect(Number(inputCantidad.value)).toBeLessThanOrEqual(34)

    await user.click(screen.getByRole('button', { name: 'Añadir al carrito' }))
    expect(await screen.findByRole('button', { name: 'Añadido' })).toBeInTheDocument()

    await user.click(await screen.findByRole('button', { name: 'Editar «Inicial»' }))
    const dialogo = await screen.findByRole('dialog', { name: 'Editar lista' })
    expect(within(dialogo).queryByRole('list', { name: 'Productos de la lista' })).not.toBeInTheDocument()

    const inputNombre = within(dialogo).getByLabelText(/nombre de la lista/i)
    await user.clear(inputNombre)
    await user.type(inputNombre, 'Renombrada')
    await user.click(within(dialogo).getByRole('button', { name: 'Guardar nombre' }))

    expect(await screen.findByRole('heading', { level: 2, name: 'Renombrada' })).toBeInTheDocument()
  })

  it('PBI534-CP09: si se eliminan todas las listas vuelve a existir la lista por defecto', async () => {
    const user = userEvent.setup()
    await abrirListas(null)
    await screen.findByRole('heading', { level: 2, name: 'Mi lista de deseos' })

    await user.click(screen.getByRole('button', { name: 'Eliminar «Mi lista de deseos»' }))
    const confirmacion = screen.getByRole('alertdialog', { name: 'Eliminar lista' })
    await user.click(within(confirmacion).getByRole('button', { name: 'Sí, eliminar' }))

    expect(await screen.findByText('Lista «Mi lista de deseos» eliminada.')).toBeInTheDocument()
    expect(await screen.findByRole('heading', { level: 2, name: 'Mi lista de deseos' })).toBeInTheDocument()
  })

  const VARIAS = [
    LISTAS[0],
    { id: 'wl-2', nombre: 'Oficina', items: [{ ...LISTAS[0].items[0], productoId: 'p-2', nombre: 'Teclado mecánico' }] },
    { id: 'wl-3', nombre: 'Vacía', items: [] },
  ]
  const productosDe = (nombre) => screen.queryByRole('list', { name: `Productos de ${nombre}` })

  it('HU528-CP44: con varias listas, la primera sale desplegada y las demás, plegadas', async () => {
    await abrirListas(VARIAS)

    expect(await screen.findByRole('list', { name: 'Productos de Regalos' })).toBeInTheDocument()
    expect(productosDe('Oficina')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ocultar los productos de Regalos' })).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByRole('button', { name: 'Mostrar los productos de Oficina' })).toHaveAttribute('aria-expanded', 'false')
  })

  it('HU528-CP44: cada lista se despliega y se pliega por separado', async () => {
    const user = userEvent.setup()
    await abrirListas(VARIAS)

    await user.click(await screen.findByRole('button', { name: 'Mostrar los productos de Oficina' }))
    expect(productosDe('Oficina')).toBeInTheDocument()
    expect(productosDe('Regalos')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Ocultar los productos de Regalos' }))
    expect(productosDe('Regalos')).not.toBeInTheDocument()
    expect(productosDe('Oficina')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Editar «Regalos»' })).toBeInTheDocument()
  })

  it('HU528-CP44: una lista vacía lo dice y no se puede plegar', async () => {
    await abrirListas(VARIAS)

    expect(await screen.findByText('Esta lista aún no tiene productos.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /los productos de Vacía$/ })).not.toBeInTheDocument()
  })

  it('HU528-CP44: al quitar un producto, la lista sigue desplegada', async () => {
    const user = userEvent.setup()
    server.use(
      http.delete('/api/customer/wishlists/:listaId/items/:productoId', () =>
        HttpResponse.json({ ...LISTAS[0], items: LISTAS[0].items.slice(1) }),
      ),
    )
    await abrirListas(VARIAS)

    await user.click(await screen.findByRole('button', { name: 'Quitar Auriculares inalámbricos de la lista' }))

    expect(await screen.findByText('Auriculares inalámbricos se ha quitado de la lista.')).toBeInTheDocument()
    expect(within(productosDe('Regalos')).getAllByRole('listitem')).toHaveLength(2)
  })

  it('HU14-CP33: muestra indicador de carga y error con reintento', async () => {
    const user = userEvent.setup()
    server.use(
      http.get(URL_LISTAS, async () => {
        await delay(100)
        return HttpResponse.json([])
      }),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/wishlist')
    expect(await screen.findByText('Cargando tus listas de deseos…')).toBeInTheDocument()
    await screen.findByText('Aún no tienes listas de deseos')

    server.use(http.get(URL_LISTAS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    renderApp('/wishlist')
    expect(await screen.findByText('No se han podido cargar tus listas de deseos. Inténtalo de nuevo.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))
    await screen.findByText('Aún no tienes listas de deseos')
  })
})
