import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'
import { server } from '../mocks/server'

async function abrirEstadisticasAdmin() {
  await abrirSesionPorApi('admin@esibuy.test')
  renderApp('/admin/statistics')
}

describe('AdminStatisticsPage', () => {
  it('HU546-CP03: el administrador ve los tres rankings globales', async () => {
    await abrirEstadisticasAdmin()

    expect(await screen.findByRole('heading', { level: 1, name: /estadísticas de la plataforma/i }))
      .toBeInTheDocument()
    expect(await screen.findByRole('heading', { level: 3, name: /productos más vendidos/i })).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: /productos más vendidos/i })).getByText('Auriculares inalámbricos'),
    ).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 3, name: /productos mejor valorados/i })).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: /productos mejor valorados/i })).getByText('Teclado mecánico'),
    ).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 3, name: /vendedores mejor valorados/i })).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: /vendedores mejor valorados/i })).getByText('TecnoVera'),
    ).toBeInTheDocument()
  })

  it('HU519-CP03: el Top 10 de ventas muestra el nombre y las unidades del producto', async () => {
    await abrirEstadisticasAdmin()

    expect(await screen.findByRole('heading', { level: 3, name: /productos más vendidos/i })).toBeInTheDocument()
    expect(screen.getByText(/12 unidades/)).toBeInTheDocument()
  })

  it('HU520-CP03: el Top 10 de productos muestra la media y el número de valoraciones', async () => {
    await abrirEstadisticasAdmin()

    expect(await screen.findByRole('heading', { level: 3, name: /productos mejor valorados/i })).toBeInTheDocument()
    expect(
      within(screen.getByRole('table', { name: /productos mejor valorados/i })).getByText('4.8 / 5'),
    ).toBeInTheDocument()
    expect(screen.getByText('6 valoraciones')).toBeInTheDocument()
  })

  it('HU521-CP03: el Top 10 de vendedores muestra el nombre comercial y su media', async () => {
    await abrirEstadisticasAdmin()

    expect(await screen.findByRole('heading', { level: 3, name: /vendedores mejor valorados/i })).toBeInTheDocument()
    const table = screen.getByRole('table', { name: /vendedores mejor valorados/i })
    expect(within(table).getByText('TecnoVera')).toBeInTheDocument()
    expect(within(table).getByText('4.6 / 5')).toBeInTheDocument()
  })

  it('HU546-CP04: sin datos muestra un estado vacío en cada ranking', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    server.use(
      http.get('/api/admin/statistics', () =>
        HttpResponse.json({
          productosMasVendidos: [],
          productosMejorValorados: [],
          vendedoresMejorValorados: [],
        }),
      ),
    )
    renderApp('/admin/statistics')

    expect(await screen.findByRole('heading', { level: 4, name: 'Sin datos de ventas' })).toBeInTheDocument()
    expect(screen.getAllByRole('heading', { level: 4, name: 'Sin valoraciones' })).toHaveLength(2)
  })
})
