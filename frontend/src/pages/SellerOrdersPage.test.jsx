import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

describe('SellerOrdersPage', () => {
  it('sellerOrdersPage: el vendedor consulta pedidos reales y abre el detalle', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/seller/orders')

    const enlace = await screen.findByRole('link', { name: 'ESB-2026-000103' })
    expect(screen.getAllByText(/Carlos Cliente/i).length).toBeGreaterThan(0)
    await user.click(enlace)

    await waitFor(() =>
      expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/orders\/o-103$/),
    )
  })

  it('sellerOrdersPage: un vendedor sin pedidos muestra el estado vacío', async () => {
    await abrirSesionPorApi('vendedor-vacio@esibuy.test')
    renderApp('/seller/orders')

    expect(await screen.findByRole('heading', { name: 'Todavía no tienes pedidos' })).toBeInTheDocument()
  })
})
