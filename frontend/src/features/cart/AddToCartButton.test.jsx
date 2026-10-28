import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { conCarrito } from '../../test/cart'
import { renderWithProviders } from '../../test/utils'
import AddToCartButton from './AddToCartButton'

const PRODUCTO = { id: 'p-1', nombre: 'Teclado mecánico', stock: 12, disponible: true }

const pintar = (props = {}, carrito = {}) =>
  renderWithProviders(conCarrito(<AddToCartButton product={PRODUCTO} {...props} />, carrito))

describe('AddToCartButton', () => {
  it('HU9-CP02: al pulsarlo añade una unidad del producto y confirma con «Añadido»', async () => {
    const add = vi.fn().mockResolvedValue(undefined)
    pintar({}, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(add).toHaveBeenCalledWith('p-1', 1)
    expect(await screen.findByText(/añadido/i)).toBeInTheDocument()
  })

  it('HU9-CP02: añade la cantidad que se le indica', async () => {
    const add = vi.fn().mockResolvedValue(undefined)
    pintar({ quantity: 3 }, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(add).toHaveBeenCalledWith('p-1', 3)
  })

  it('HU9-CP02: mientras se añade queda desactivado, para no enviar el mismo producto dos veces', async () => {
    let terminar
    const add = vi.fn(() => new Promise((resolver) => (terminar = resolver)))
    pintar({}, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(screen.getByRole('button', { name: /añadiendo/i })).toBeDisabled()
    terminar()
    expect(await screen.findByText(/añadido/i)).toBeInTheDocument()
  })

  it('HU9-CP02: si no hay existencias suficientes lo avisa y se puede volver a intentar', async () => {
    const add = vi.fn().mockRejectedValue(new ApiError(409, 'Sin existencias', 'INSUFFICIENT_STOCK'))
    pintar({}, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent(/no hay existencias suficientes/i)
    expect(screen.getByRole('button', { name: /añadir al carrito/i })).toBeEnabled()
  })

  it('HU9-CP02: si falla por otro motivo avisa de que no se ha podido añadir', async () => {
    const add = vi.fn().mockRejectedValue(new ApiError(500, 'Error'))
    pintar({}, { add })

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent(/no se ha podido añadir/i)
  })

  it('HU9-CP02: el aviso desaparece al volver a intentarlo', async () => {
    const add = vi.fn().mockRejectedValueOnce(new ApiError(500, 'Error')).mockResolvedValue(undefined)
    pintar({}, { add })
    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))
    await screen.findByRole('alert')

    await userEvent.click(screen.getByRole('button', { name: /añadir al carrito/i }))

    await waitFor(() => expect(screen.queryByRole('alert')).not.toBeInTheDocument())
  })

  it('HU9-CP02: un producto agotado no se puede añadir', async () => {
    const add = vi.fn()
    pintar({ product: { ...PRODUCTO, stock: 0 } }, { add })

    const boton = screen.getByRole('button', { name: /agotado/i })
    expect(boton).toBeDisabled()
    await userEvent.click(boton)
    expect(add).not.toHaveBeenCalled()
  })

  it('HU9-CP02: un producto no disponible no se puede añadir', () => {
    pintar({ product: { ...PRODUCTO, disponible: false } })

    expect(screen.getByRole('button', { name: /no disponible/i })).toBeDisabled()
  })
})
