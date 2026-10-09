import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const RUTA = '/admin/admins'

async function abrirListado() {
  await abrirSesionPorApi('admin@esibuy.test')
  renderApp(RUTA)
  await screen.findByRole('heading', { level: 1, name: /^administradores$/i })
}

describe('AdminAdminsPage (HU21)', () => {
  it('adminAdminsPage_alAbrirse_muestraSoloLosAdministradores', async () => {
    await abrirListado()

    const tabla = await screen.findByRole('table')
    const filas = within(tabla).getAllByRole('row').slice(1)

    expect(filas).toHaveLength(3)
    expect(within(tabla).getByText('ana.lopez@esibuy.test')).toBeInTheDocument()
    expect(within(tabla).getByText('diego.delgado@esibuy.test')).toBeInTheDocument()
    expect(within(tabla).getByText('admin@esibuy.test')).toBeInTheDocument()
    filas.forEach((fila) => expect(within(fila).getByText('Administrador')).toBeInTheDocument())
  })

  it('adminAdminsPage_pideAlBackendSoloElRolAdministrador', async () => {
    let rolPedido = null
    server.use(
      http.get('/api/admin/usuarios', ({ request }) => {
        rolPedido = new URL(request.url).searchParams.get('role')
        return HttpResponse.json({ items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 })
      }),
    )

    await abrirListado()

    await waitFor(() => expect(rolPedido).toBe('ADMINISTRADOR'))
  })

  it('adminAdminsPage_alPulsarCrearAdministrador_abreElFormularioDeAlta', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('link', { name: /crear administrador/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /nuevo administrador/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin\/admins\/new$/)
  })

  it('adminAdminsPage_alPulsarVolverAlPanel_vuelveAlPanelDeControl', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('link', { name: /volver al panel/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
  })

  it('adminAdminsPage_conErrorDeServidor_muestraMensajeYPermiteReintentar', async () => {
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirListado()

    expect(await screen.findByText(/no se ha podido cargar el listado de administradores/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByText('ana.lopez@esibuy.test')).toBeInTheDocument()
  })

  it('adminAdminsPage_sinSesion_redirigeAlLogin', async () => {
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('adminAdminsPage_conSesionDeCliente_noMuestraElListado', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/))
    expect(screen.queryByRole('heading', { name: /^administradores$/i })).not.toBeInTheDocument()
  })

  it('adminAdminsPage_alActivarUnAdministradorDesactivado_loMuestraActivo', async () => {
    const user = userEvent.setup()
    await abrirListado()
    const fila = () => screen.getByText('diego.delgado@esibuy.test').closest('tr')
    await screen.findByText('diego.delgado@esibuy.test')

    await user.click(screen.getByRole('button', { name: 'Activar a Diego Delgado Iglesias' }))
    const pregunta = await screen.findByText(/seguro que quieres activar a diego delgado iglesias/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Activar', { selector: 'button' }))

    expect(await screen.findByText(/diego delgado iglesias ya está activo/i, {}, { timeout: 3000 })).toBeInTheDocument()
    await waitFor(() => expect(within(fila()).getByText('Activo')).toBeInTheDocument(), { timeout: 3000 })
  })

  it('adminAdminsPage_enLaFilaDelAdministradorEnSesion_noOfreceDesactivar', async () => {
    await abrirListado()
    await screen.findByText('admin@esibuy.test')

    const filaPropia = screen.getByText('admin@esibuy.test').closest('tr')
    const filaDeOtro = screen.getByText('ana.lopez@esibuy.test').closest('tr')

    expect(within(filaPropia).queryByText('Desactivar', { selector: 'button' })).not.toBeInTheDocument()
    expect(within(filaDeOtro).getByText('Desactivar', { selector: 'button' })).toBeInTheDocument()
  })

  it('adminAdminsPage_alDesactivarOtroAdministrador_loMuestraDesactivado', async () => {
    const user = userEvent.setup()
    await abrirListado()
    const fila = () => screen.getByText('ana.lopez@esibuy.test').closest('tr')
    await screen.findByText('ana.lopez@esibuy.test')

    await user.click(screen.getByRole('button', { name: 'Desactivar a Ana López García' }))
    const pregunta = await screen.findByText(/seguro que quieres desactivar a ana lópez garcía/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Desactivar', { selector: 'button' }))

    expect(await screen.findByText(/ana lópez garcía está desactivado/i, {}, { timeout: 3000 })).toBeInTheDocument()
    await waitFor(() => expect(within(fila()).getByText('Desactivado')).toBeInTheDocument(), { timeout: 3000 })
  })

  it('adminAdminsPage_siEsElUltimoAdministradorActivo_muestraElMotivo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    await abrirListado()
    await screen.findByText('ana.lopez@esibuy.test')

    await user.click(screen.getByRole('button', { name: 'Desactivar a Ana López García' }))
    const pregunta = await screen.findByText(/seguro que quieres desactivar a ana lópez garcía/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Desactivar', { selector: 'button' }))

    expect(
      await screen.findByText('No se puede desactivar al último administrador activo.', {}, { timeout: 3000 }),
    ).toBeInTheDocument()
  })
})
