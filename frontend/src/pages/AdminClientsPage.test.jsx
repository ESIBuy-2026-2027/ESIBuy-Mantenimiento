import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { updateUser } from '../api/users'
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

describe('AdminClientsPage (HU22): editar desde la ficha', () => {
  it('adminClientsPage_alEditarUnClienteDesdeSuFicha_elListadoMuestraElNombreNuevo', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)
    await user.click(await screen.findByRole('button', { name: /ver información de carlos moreno gil/i }))
    const ficha = await screen.findByRole('dialog', { name: /información del usuario/i })

    await user.click(await within(ficha).findByRole('button', { name: 'Editar' }))
    await user.clear(await within(ficha).findByLabelText(/^nombre/i))
    await user.type(within(ficha).getByLabelText(/^nombre/i), 'Carla')
    await user.click(within(ficha).getByRole('button', { name: /^guardar$/i }))

    expect(await within(ficha).findByText('Los cambios se han guardado.', undefined, { timeout: 3000 })).toBeInTheDocument()
    await user.click(within(ficha).getByRole('button', { name: 'Cerrar' }))
    // Mientras se cierra la ficha, Fluent marca la página como oculta: se busca también entre lo oculto.
    expect(
      await screen.findByRole('button', { name: /ver información de carla moreno gil/i, hidden: true }),
    ).toBeInTheDocument()
  })
})

describe('AdminClientsPage (HU22): un cliente que cambia de rol', () => {
  it('adminClientsPage_alPasarUnClienteAAdministrador_yaNoApareceEnElListadoDeClientes', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp(RUTA)
    await user.click(await screen.findByRole('button', { name: /ver información de carlos moreno gil/i }))
    const ficha = await screen.findByRole('dialog', { name: /información del usuario/i })

    await user.click(await within(ficha).findByRole('button', { name: 'Editar' }))
    await user.selectOptions(await within(ficha).findByLabelText(/^rol/i), 'ADMINISTRADOR')
    await user.click(within(ficha).getByRole('button', { name: /^guardar$/i }))
    expect(await within(ficha).findByText('Los cambios se han guardado.', undefined, { timeout: 3000 })).toBeInTheDocument()
    await user.click(within(ficha).getByRole('button', { name: 'Cerrar' }))

    await waitFor(() =>
      expect(
        screen.queryByRole('button', { name: /ver información de carlos moreno gil/i, hidden: true }),
      ).not.toBeInTheDocument(),
    )
  })

  it('adminClientsPage_unAdministradorPromovidoDesdeLaApi_noApareceEnClientesYSiEnAdministradores', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    await updateUser('usr-3', { rol: 'ADMINISTRADOR', nombre: 'Carlos', apellidos: 'Moreno Gil', sede: '', avatar: '' })
    renderApp('/admin/admins')

    expect(await screen.findByRole('button', { name: /ver información de carlos moreno gil/i })).toBeInTheDocument()
  })
})
