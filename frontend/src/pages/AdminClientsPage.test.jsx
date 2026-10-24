import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
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

describe('AdminClientsPage (HU22): editar desde la tabla', () => {
  it('adminClientsPage_alEditarUnClienteDesdeLaTabla_elListadoMuestraElNombreNuevo', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)
    await user.click(await screen.findByRole('button', { name: 'Editar a Carlos Moreno Gil' }))
    const edicion = await screen.findByRole('dialog', { name: /editar usuario/i })

    await user.clear(await within(edicion).findByLabelText(/^nombre/i))
    await user.type(within(edicion).getByLabelText(/^nombre/i), 'Carla')
    await user.click(within(edicion).getByRole('button', { name: /^guardar$/i }))

    expect(await screen.findByText('Los cambios se han guardado.', undefined, { timeout: 3000 })).toBeInTheDocument()
    // Mientras se cierra el formulario, Fluent marca la página como oculta: se busca también entre lo oculto.
    expect(
      await screen.findByRole('button', { name: 'Editar a Carla Moreno Gil', hidden: true }, { timeout: 3000 }),
    ).toBeInTheDocument()
  })

  it('adminClientsPage_laFichaNoTieneEditar', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)

    await user.click(await screen.findByRole('button', { name: /ver información de carlos moreno gil/i }))

    const ficha = await screen.findByRole('dialog', { name: /información del usuario/i })
    await within(ficha).findByLabelText('Email')
    expect(within(ficha).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
  })
})
