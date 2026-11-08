import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const esperarRuta = (ruta) =>
  waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))

// Del mock: Nora (nuevo@esibuy.test) es cliente Estándar y Carlos (cliente@esibuy.test), cliente Premium.
async function abrirPremium(email) {
  await abrirSesionPorApi(email)
  renderApp('/premium')
  await screen.findByRole('heading', { level: 1, name: /esibuy premium/i })
}

describe('PremiumPage (HU45): ventajas de ESIBuy Premium', () => {
  it('HU45-CP23: muestra las tres ventajas de ESIBuy Premium, cada una con su texto, y ningún precio', async () => {
    await abrirPremium('nuevo@esibuy.test')

    const lista = screen.getByRole('list', { name: /ventajas/i })
    const ventajas = within(lista).getAllByRole('listitem')
    expect(ventajas).toHaveLength(3)
    expect(within(ventajas[0]).getByRole('heading', { level: 3, name: 'Descuentos exclusivos' })).toBeInTheDocument()
    expect(within(ventajas[1]).getByRole('heading', { level: 3, name: 'Sin gastos de envío' })).toBeInTheDocument()
    expect(within(ventajas[2]).getByRole('heading', { level: 3, name: 'Envíos más rápidos' })).toBeInTheDocument()
    ventajas.forEach((ventaja) => expect(within(ventaja).getByRole('paragraph')).not.toBeEmptyDOMElement())
    expect(screen.getByRole('main')).not.toHaveTextContent(/€|\d+[.,]\d{2}|precio de la suscripción/i)
  })

  it('HU45-CP23: la jerarquía de encabezados empieza por un único h1', async () => {
    await abrirPremium('nuevo@esibuy.test')

    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1)
    expect(screen.getByRole('heading', { level: 2, name: /ventajas/i })).toBeInTheDocument()
  })

  it('HU45-CP24: sin sesión, la vista Premium lleva al login', async () => {
    renderApp('/premium')

    await esperarRuta('/login')
  })

  it('HU45-CP24: el vendedor y el administrador no entran en la vista Premium: vuelven a su inicio', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    const { unmount } = renderApp('/premium')
    await esperarRuta('/seller/products')
    unmount()

    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/premium')
    await esperarRuta('/admin')
  })

  it('HU45-CP25: el cliente Premium ve que ya es ESIBuy Premium y no se le ofrece la pasarela', async () => {
    await abrirPremium('cliente@esibuy.test')

    expect(await screen.findByRole('heading', { level: 2, name: /ya eres esibuy premium/i })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /hazte premium/i })).not.toBeInTheDocument()
  })

  it('HU45-CP26: el cliente Estándar ve Hazte Premium, que lleva a la pasarela de pago', async () => {
    await abrirPremium('nuevo@esibuy.test')

    const enlace = await screen.findByRole('link', { name: /hazte premium/i })
    expect(enlace).toHaveAttribute('href', '/premium/pago')
    expect(screen.queryByRole('heading', { name: /ya eres esibuy premium/i })).not.toBeInTheDocument()
  })
})
