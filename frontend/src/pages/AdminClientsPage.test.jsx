import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const RUTA = '/admin/clients'

describe('AdminClientsPage (HU24)', () => {
  it('adminClientsPage_conSesionDeAdmin_muestraElTituloYLosClientes', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)

    expect(await screen.findByRole('heading', { level: 1, name: /^clientes$/i })).toBeInTheDocument()
    expect(await screen.findByText('carlos.moreno@esibuy.test')).toBeInTheDocument()
    expect(screen.queryByText('ana.lopez@esibuy.test')).not.toBeInTheDocument()
  })

  it('adminClientsPage_alPulsarVolverAlPanel_vuelveAlPanelDeControl', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)

    await user.click(await screen.findByRole('link', { name: /volver al panel/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
  })

  it('adminClientsPage_sinSesion_redirigeAlLogin', async () => {
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('adminClientsPage_conSesionDeVendedor_noMuestraElListado', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
    expect(screen.queryByRole('heading', { name: /^clientes$/i })).not.toBeInTheDocument()
  })
})
