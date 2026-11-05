import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis favoritos» del cliente (HU14, T363 y T364) contra el contrato del backend (PR #205, #206, #208 y #209):
// GET /api/customer/wishlist devuelve la lista con los datos actuales de cada producto y
// DELETE /api/customer/wishlist/items/{productoId} quita uno y devuelve la lista actualizada.

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('WishlistPage (HU14): acceso', () => {
  it('HU14-CP32: el cliente abre Mis favoritos en /wishlist', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/wishlist')

    expect(await screen.findByRole('heading', { level: 1, name: 'Mis favoritos' })).toBeInTheDocument()
  })

  it('HU14-CP32: sin sesión, Mis favoritos lleva a iniciar sesión', async () => {
    renderApp('/wishlist')

    await esperarRuta('/login')
  })

  it('HU14-CP32: un vendedor no tiene lista de deseos: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/wishlist')

    await esperarRuta('/seller/products')
  })
})
