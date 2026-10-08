import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

describe('AdminPage (HU18)', () => {
  it('adminPage_conSesionDeAdmin_muestraElTituloPanelDeControl', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    expect(await screen.findByRole('heading', { level: 1, name: /panel de control/i })).toBeInTheDocument()
  })

  it('adminPage_conSesionDeAdmin_muestraLosTresBotonesDeGestion', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    expect(await screen.findByRole('button', { name: /clientes/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /vendedores/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /administradores/i })).toBeInTheDocument()
  })

  it('adminPage_botonesDeGestion_recibenFocoConTeclado', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    const boton = await screen.findByRole('button', { name: /clientes/i })
    boton.focus()

    expect(boton).toHaveFocus()
  })

  it('adminPage_alPulsarClientes_abreElListadoDeClientes', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    await user.click(await screen.findByRole('button', { name: /clientes/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin\/clients$/))
  })

  it('adminPage_alPulsarAdministradores_abreElListadoDeAdministradores', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin')

    await user.click(await screen.findByRole('button', { name: /administradores/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin\/admins$/))
  })

  it('adminPage_sinSesion_redirigeAlLogin', async () => {
    renderApp('/admin')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('adminPage_conSesionDeVendedor_noMuestraElPanel', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/admin')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
    expect(screen.queryByRole('heading', { name: /panel de control/i })).not.toBeInTheDocument()
  })
})
