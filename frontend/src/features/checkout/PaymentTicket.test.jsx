import { afterEach, describe, expect, it, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import { renderWithProviders } from '../../test/utils'
import PaymentTicket from './PaymentTicket'

const PEDIDOS = [
  { id: 'o-1', vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' }, total: 197.32, lineas: [{}, {}] },
  { id: 'o-2', vendedor: { id: 'u-99', nombreComercial: 'Otero Hogar y Tecnología' }, total: 129, lineas: [{}] },
]
const TARJETA = { id: 'mp-1', tipo: 'TARJETA', mascara: '**** **** **** 4242', predeterminado: true }
const PAGADO_EL = new Date(2026, 9, 28, 10, 30)

// jsdom no trae matchMedia: se simula si el usuario ha pedido reducir el movimiento.
const preferirMenosMovimiento = (reducir) =>
  vi.stubGlobal('matchMedia', (consulta) => ({
    matches: reducir && consulta.includes('prefers-reduced-motion: reduce'),
    media: consulta,
    addEventListener: () => {},
    removeEventListener: () => {},
  }))

const renderTicket = () =>
  renderWithProviders(<PaymentTicket orders={PEDIDOS} paymentMethod={TARJETA} paidAt={PAGADO_EL} />)

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('PaymentTicket (HU10): contenido del recibo', () => {
  it('HU10-CP10: muestra el total pagado, la suma de los pedidos', () => {
    renderTicket()

    const recibo = screen.getByRole('region', { name: /recibo del pago/i })
    expect(within(recibo).getByText(/326,32/)).toBeInTheDocument()
  })

  it('HU10-CP10: muestra la fecha del pago', () => {
    renderTicket()

    expect(screen.getByText(/28 de octubre de 2026/i)).toBeInTheDocument()
  })

  it('HU10-CP10: lista cada pedido con su número, su proveedor y su importe', () => {
    renderTicket()

    const pedidos = screen.getAllByRole('listitem', { name: /pedido/i })
    expect(pedidos).toHaveLength(2)
    expect(pedidos[0]).toHaveAccessibleName('Pedido o-1')
    expect(pedidos[0]).toHaveTextContent(/tecnovera/i)
    expect(pedidos[0]).toHaveTextContent(/197,32/)
    expect(pedidos[1]).toHaveAccessibleName('Pedido o-2')
    expect(pedidos[1]).toHaveTextContent(/otero hogar y tecnología/i)
    expect(pedidos[1]).toHaveTextContent(/129,00/)
  })

  it('HU10-CP10: muestra el método de pago usado solo con su máscara', () => {
    renderTicket()

    const metodo = screen.getByRole('group', { name: /pagado con/i })
    expect(metodo).toHaveTextContent(/tarjeta/i)
    expect(metodo).toHaveTextContent('**** **** **** 4242')
  })
})

describe('PaymentTicket (HU10): accesibilidad de la decoración', () => {
  it('HU10-CP11: el código de barras es decorativo y queda oculto a los lectores de pantalla', () => {
    renderTicket()

    expect(screen.getByTestId('codigo-de-barras')).toHaveAttribute('aria-hidden', 'true')
  })

  it('HU10-CP11: celebra el pago con confeti decorativo', () => {
    preferirMenosMovimiento(false)
    renderTicket()

    expect(screen.getByTestId('confeti')).toHaveAttribute('aria-hidden', 'true')
  })

  it('HU10-CP11: sin confeti si el usuario prefiere reducir el movimiento', () => {
    preferirMenosMovimiento(true)
    renderTicket()

    expect(screen.queryByTestId('confeti')).not.toBeInTheDocument()
    expect(screen.getByRole('region', { name: /recibo del pago/i })).toBeInTheDocument()
  })
})
