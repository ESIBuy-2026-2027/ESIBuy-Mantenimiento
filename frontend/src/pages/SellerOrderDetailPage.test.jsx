import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

describe('SellerOrderDetailPage', () => {
  it('sellerOrderDetailPage: muestra productos, cliente, dirección y total reales', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/seller/orders/o-103')

    expect(await screen.findByRole('heading', { level: 1, name: 'Pedido ESB-2026-000103' })).toBeInTheDocument()
    expect(screen.getByText('Carlos Cliente')).toBeInTheDocument()
    expect(screen.getByText(/Calle Mayor 1/i)).toBeInTheDocument()

    const tabla = screen.getByRole('table', { name: 'Productos del pedido' })
    expect(within(tabla).getByText('Auriculares')).toBeInTheDocument()
    expect(within(tabla).getByText('Teclado')).toBeInTheDocument()
  })

  it('sellerOrderDetailPage: avanza el estado linealmente', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/seller/orders/o-201')

    await user.click(await screen.findByRole('button', { name: 'Marcar como preparando' }))

    expect(await screen.findByText('Estado del pedido actualizado.')).toBeInTheDocument()
    expect(screen.getAllByText('Preparando').length).toBeGreaterThan(0)
  })

  it('sellerOrderDetailPage: otro pedido del vendedor no se confunde con un pedido ajeno', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/seller/orders/o-102')

    expect(await screen.findByText('Pedido no encontrado.')).toBeInTheDocument()
  })
})
