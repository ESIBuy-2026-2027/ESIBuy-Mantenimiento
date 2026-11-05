import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

// Del mock de estadísticas: Vera (vendedor@esibuy.test) tiene 6 pedidos con 16 unidades, 8 valoraciones y 23
// productos; Vicente (vendedor-vacio@esibuy.test) no tiene ventas, valoraciones ni productos.
async function abrirEstadisticas(email) {
  await abrirSesionPorApi(email)
  renderApp('/profile')
  return screen.findByRole('region', { name: 'Estadísticas de ventas' })
}

const cifra = (bloque, nombre) => within(bloque).getByRole('group', { name: nombre })

const filasDe = (tabla) =>
  within(tabla)
    .getAllByRole('row')
    .slice(1)
    .map((fila) => within(fila).getAllByRole('cell').map((celda) => celda.textContent))

describe('SellerStatistics (HU17): ventas', () => {
  it('HU17-CP14: el vendedor ve sus pedidos, unidades e ingresos y los más vendidos en orden', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const ventas = await screen.findByRole('region', { name: 'Ventas' })
    expect(cifra(ventas, 'Pedidos')).toHaveTextContent('6')
    expect(cifra(ventas, 'Unidades vendidas')).toHaveTextContent('16')
    expect(cifra(ventas, 'Ingresos')).toHaveTextContent('1095,83')

    const masVendidos = within(ventas).getByRole('table', { name: 'Los más vendidos' })
    const filas = filasDe(masVendidos)
    expect(filas.map(([producto, unidades]) => [producto, unidades])).toEqual([
      ['Teclado mecánico', '5'],
      ['Auriculares inalámbricos', '4'],
      ['Cargador USB-C 65 W', '3'],
      ['Memoria USB 128 GB', '2'],
      ['Monitor 27 pulgadas', '1'],
    ])
    expect(filas[0][2]).toMatch(/^447,50\s€$/)
  })
})
