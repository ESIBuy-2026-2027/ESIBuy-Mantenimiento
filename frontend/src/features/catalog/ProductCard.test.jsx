import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { conCarrito } from '../../test/cart'
import { renderWithProviders } from '../../test/utils'
import ProductCard from './ProductCard'

const PRODUCTO = {
  id: 'p-1',
  nombre: 'Teclado mecánico',
  imagen: 'https://tienda.test/teclado.png',
  categoriaId: 'cat-1',
  vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
  precio: 89.5,
  descuento: null,
  precioFinal: 89.5,
  stock: 12,
  disponible: true,
  valoracion: 4.5,
  numValoraciones: 12,
}

const pintar = (producto = PRODUCTO, carrito = {}, categoryName = 'Tecnología') =>
  renderWithProviders(conCarrito(<ProductCard product={producto} categoryName={categoryName} />, carrito))

describe('ProductCard', () => {
  it('HU7-CP02: muestra el nombre, la imagen y la categoría del producto', () => {
    pintar()

    expect(screen.getByText('Teclado mecánico')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Teclado mecánico' })).toHaveAttribute('src', PRODUCTO.imagen)
    expect(screen.getByText('Tecnología')).toBeInTheDocument()
  })

  it('HU7-CP02: muestra la valoración en estrellas y cuántas valoraciones tiene', () => {
    pintar()

    expect(screen.getByRole('img', { name: /valoración: 4,5 de 5/i })).toBeInTheDocument()
    expect(screen.getByText('4,5')).toBeInTheDocument()
    expect(screen.getByText('(12)')).toBeInTheDocument()
  })

  it('HU7-CP02: un producto sin valoraciones lo indica en lugar de mostrar estrellas', () => {
    pintar({ ...PRODUCTO, valoracion: null, numValoraciones: 0 })

    expect(screen.getByText(/sin valoraciones/i)).toBeInTheDocument()
    expect(screen.queryByRole('img', { name: /valoración:/i })).not.toBeInTheDocument()
  })

  it('HU7-CP02: un producto sin imagen muestra un marcador en su lugar', () => {
    pintar({ ...PRODUCTO, imagen: '' })

    expect(screen.queryByRole('img', { name: 'Teclado mecánico' })).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: /sin imagen/i })).toBeInTheDocument()
  })

  it('HU8-CP06: la tarjeta es un enlace al detalle del producto', () => {
    pintar()

    expect(screen.getByRole('link', { name: /teclado mecánico/i })).toHaveAttribute('href', '/products/p-1')
  })

  it('HU8-CP06: el identificador del producto se codifica en la dirección', () => {
    pintar({ ...PRODUCTO, id: 'a/b c' })

    expect(screen.getByRole('link', { name: /teclado mecánico/i })).toHaveAttribute('href', '/products/a%2Fb%20c')
  })

  it('HU7-CP02: si no se conoce la categoría muestra un guion', () => {
    pintar(PRODUCTO, {}, null)

    expect(screen.getByText('—')).toBeInTheDocument()
  })

  it('HU9-CP01: la tarjeta muestra el precio del producto', () => {
    pintar()

    expect(screen.getByText(/89,50/)).toBeInTheDocument()
  })

  it('HU9-CP01: con descuento muestra el precio final, el original tachado y el porcentaje', () => {
    pintar({ ...PRODUCTO, precio: 59.9, descuento: { porcentaje: 10 }, precioFinal: 53.91 })

    expect(screen.getByText(/53,91/)).toBeInTheDocument()
    expect(screen.getByText(/59,90/).closest('s')).not.toBeNull()
    expect(screen.getByText('-10 %')).toBeInTheDocument()
  })

  it('HU9-CP02: la tarjeta tiene un botón para añadir una unidad al carrito', async () => {
    const add = vi.fn().mockResolvedValue(undefined)
    pintar(PRODUCTO, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(add).toHaveBeenCalledWith('p-1', 1)
  })

  it('HU9-CP02: el botón no forma parte del enlace, así que añadir no abre el detalle', () => {
    pintar()

    expect(screen.getByRole('button', { name: /añadir al carrito/i }).closest('a')).toBeNull()
    expect(screen.getByRole('link', { name: /teclado mecánico/i })).not.toHaveTextContent(/añadir/i)
  })
})

describe('ProductCard (HU14): favoritos', () => {
  const pintarConFavoritos = (producto = PRODUCTO, props = {}) =>
    renderWithProviders(
      conCarrito(
        <ProductCard
          product={producto}
          categoryName="Tecnología"
          onAddFavorite={vi.fn().mockResolvedValue(undefined)}
          {...props}
        />,
      ),
    )

  it('HU14-CP21: la tarjeta tiene un corazón para añadir el producto a favoritos, fuera del enlace al detalle', () => {
    pintarConFavoritos()

    const corazon = screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' })
    expect(corazon).toBeEnabled()
    expect(corazon.closest('a')).toBeNull()
  })

  it('HU14-CP21: un producto agotado también se puede añadir a favoritos', () => {
    pintarConFavoritos({ ...PRODUCTO, stock: 0 })

    expect(screen.getByRole('button', { name: /a favoritos/i })).toBeEnabled()
  })

  it('HU14-CP21: un producto no disponible no tiene el corazón', () => {
    pintarConFavoritos({ ...PRODUCTO, disponible: false })

    expect(screen.queryByRole('button', { name: /favoritos/i })).not.toBeInTheDocument()
  })

  it('HU14-CP21: sin onAddFavorite la tarjeta no ofrece favoritos', () => {
    pintar()

    expect(screen.queryByRole('button', { name: /favoritos/i })).not.toBeInTheDocument()
  })

  it('HU14-CP22: al pulsar el corazón pide añadir ese producto a favoritos', async () => {
    const onAddFavorite = vi.fn().mockResolvedValue(undefined)
    pintarConFavoritos(PRODUCTO, { onAddFavorite })

    await userEvent.click(screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' }))

    expect(onAddFavorite).toHaveBeenCalledWith('p-1')
  })

  it('HU14-CP22: mientras se añade, el corazón queda deshabilitado', async () => {
    pintarConFavoritos(PRODUCTO, { onAddFavorite: vi.fn(() => new Promise(() => {})) })

    await userEvent.click(screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' }))

    expect(screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' })).toBeDisabled()
  })

  it('HU14-CP22: un producto que ya está en favoritos tiene el corazón relleno para quitarlo', () => {
    pintarConFavoritos(PRODUCTO, { favorite: true, onRemoveFavorite: vi.fn().mockResolvedValue(undefined) })

    const corazon = screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' })
    expect(corazon).toBeEnabled()
    expect(corazon).toHaveAttribute('title', 'Quitar de favoritos')
    expect(screen.queryByRole('button', { name: /añadir .* a favoritos/i })).not.toBeInTheDocument()
  })

  it.each([
    ['el producto ya no está disponible (404)', { status: 404, code: 'PRODUCT_NOT_FOUND' }, 'Este producto ya no está disponible.'],
    ['falla por otro motivo', { status: 500 }, 'No se ha podido añadir a favoritos. Inténtalo de nuevo.'],
  ])('HU14-CP24: si %s, la tarjeta lo avisa y el corazón se puede volver a pulsar', async (_caso, error, mensaje) => {
    pintarConFavoritos(PRODUCTO, { onAddFavorite: vi.fn().mockRejectedValue(error) })

    await userEvent.click(screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(mensaje)
    expect(screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' })).toBeEnabled()
  })

  it('HU14-CP24: al volver a pulsar el corazón desaparece el aviso anterior', async () => {
    const onAddFavorite = vi.fn().mockRejectedValueOnce({ status: 500 }).mockResolvedValueOnce(undefined)
    pintarConFavoritos(PRODUCTO, { onAddFavorite })
    const corazon = () => screen.getByRole('button', { name: 'Añadir Teclado mecánico a favoritos' })

    await userEvent.click(corazon())
    await screen.findByRole('alert')
    await userEvent.click(corazon())

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(onAddFavorite).toHaveBeenCalledTimes(2)
  })

  it('HU14-CP37: al pulsar el corazón relleno pide quitar ese producto de favoritos', async () => {
    const onRemoveFavorite = vi.fn().mockResolvedValue(undefined)
    pintarConFavoritos(PRODUCTO, { favorite: true, onRemoveFavorite })

    await userEvent.click(screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' }))

    expect(onRemoveFavorite).toHaveBeenCalledWith('p-1')
  })

  it('HU14-CP37: mientras se quita, el corazón queda deshabilitado', async () => {
    pintarConFavoritos(PRODUCTO, { favorite: true, onRemoveFavorite: vi.fn(() => new Promise(() => {})) })

    await userEvent.click(screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' }))

    expect(screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' })).toBeDisabled()
  })

  it('HU14-CP37: si no se puede quitar, la tarjeta lo avisa y el corazón se puede volver a pulsar', async () => {
    pintarConFavoritos(PRODUCTO, { favorite: true, onRemoveFavorite: vi.fn().mockRejectedValue({ status: 500 }) })

    await userEvent.click(screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('No se ha podido quitar de favoritos. Inténtalo de nuevo.')
    expect(screen.getByRole('button', { name: 'Quitar Teclado mecánico de favoritos' })).toBeEnabled()
  })
})
