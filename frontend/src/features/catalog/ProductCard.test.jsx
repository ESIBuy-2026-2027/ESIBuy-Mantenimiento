import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderWithProviders } from '../../test/utils'
import ProductCard from './ProductCard'

const PRODUCTO = {
  id: 'p-1',
  nombre: 'Teclado mecánico',
  imagen: 'https://tienda.test/teclado.png',
  categoriaId: 'cat-1',
  valoracion: 4.5,
  numValoraciones: 12,
}

describe('ProductCard', () => {
  it('HU7-CP02: muestra el nombre, la imagen y la categoría del producto', () => {
    renderWithProviders(<ProductCard product={PRODUCTO} categoryName="Tecnología" />)

    expect(screen.getByText('Teclado mecánico')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Teclado mecánico' })).toHaveAttribute('src', PRODUCTO.imagen)
    expect(screen.getByText('Tecnología')).toBeInTheDocument()
  })

  it('HU7-CP02: muestra la valoración en estrellas y cuántas valoraciones tiene', () => {
    renderWithProviders(<ProductCard product={PRODUCTO} categoryName="Tecnología" />)

    expect(screen.getByRole('img', { name: /valoración: 4,5 de 5/i })).toBeInTheDocument()
    expect(screen.getByText('4,5')).toBeInTheDocument()
    expect(screen.getByText('(12)')).toBeInTheDocument()
  })

  it('HU7-CP02: un producto sin valoraciones lo indica en lugar de mostrar estrellas', () => {
    renderWithProviders(
      <ProductCard product={{ ...PRODUCTO, valoracion: null, numValoraciones: 0 }} categoryName="Tecnología" />,
    )

    expect(screen.getByText(/sin valoraciones/i)).toBeInTheDocument()
    expect(screen.queryByRole('img', { name: /valoración:/i })).not.toBeInTheDocument()
  })

  it('HU7-CP02: un producto sin imagen muestra un marcador en su lugar', () => {
    renderWithProviders(<ProductCard product={{ ...PRODUCTO, imagen: '' }} categoryName="Tecnología" />)

    expect(screen.queryByRole('img', { name: 'Teclado mecánico' })).not.toBeInTheDocument()
    expect(screen.getByRole('img', { name: /sin imagen/i })).toBeInTheDocument()
  })

  it('HU8-CP06: la tarjeta es un enlace al detalle del producto', () => {
    renderWithProviders(<ProductCard product={PRODUCTO} categoryName="Tecnología" />)

    expect(screen.getByRole('link', { name: /teclado mecánico/i })).toHaveAttribute('href', '/products/p-1')
  })

  it('HU8-CP06: el identificador del producto se codifica en la dirección', () => {
    renderWithProviders(<ProductCard product={{ ...PRODUCTO, id: 'a/b c' }} categoryName="Tecnología" />)

    expect(screen.getByRole('link', { name: /teclado mecánico/i })).toHaveAttribute('href', '/products/a%2Fb%20c')
  })

  it('HU7-CP02: si no se conoce la categoría muestra un guion', () => {
    renderWithProviders(<ProductCard product={PRODUCTO} />)

    expect(screen.getByText('—')).toBeInTheDocument()
  })
})
