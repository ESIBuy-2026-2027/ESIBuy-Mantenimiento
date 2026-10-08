import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import ActivateUserButton from './ActivateUserButton'

// Lucía Torres Vega es una clienta desactivada del mock de usuarios.
const USUARIO = { id: 'usr-4', nombre: 'Lucía', apellidos: 'Torres Vega' }
const ESPERA = { timeout: 3000 }

async function renderBoton() {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onActivated: vi.fn(), onError: vi.fn() }
  renderWithProviders(<ActivateUserButton user={USUARIO} {...handlers} />)
  return handlers
}

// Se busca por texto y no por rol: mientras la confirmación se abre o se cierra, Fluent marca
// partes de la página con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function abrirConfirmacion(user) {
  await user.click(screen.getByRole('button', { name: 'Activar a Lucía Torres Vega' }))
  const pregunta = await screen.findByText(/seguro que quieres activar a lucía torres vega/i)
  return pregunta.closest('[role="alertdialog"]')
}

const botonDe = (confirmacion, texto) => within(confirmacion).getByText(texto, { selector: 'button' })

describe('ActivateUserButton', () => {
  it('activateUserButton_muestraElBotonActivarConElNombreDelUsuario', async () => {
    await renderBoton()

    expect(screen.getByRole('button', { name: 'Activar a Lucía Torres Vega' })).toHaveTextContent('Activar')
  })

  it('activateUserButton_alPulsar_pideConfirmacionSinActivarTodavia', async () => {
    const user = userEvent.setup()
    const { onActivated } = await renderBoton()

    const confirmacion = await abrirConfirmacion(user)

    expect(confirmacion).toHaveTextContent('Activar usuario')
    expect(onActivated).not.toHaveBeenCalled()
  })

  it('activateUserButton_alCancelar_noActivaNada', async () => {
    const user = userEvent.setup()
    const { onActivated, onError } = await renderBoton()

    const confirmacion = await abrirConfirmacion(user)
    await user.click(botonDe(confirmacion, 'Cancelar'))

    await waitFor(() => expect(screen.queryByText(/seguro que quieres activar/i)).not.toBeInTheDocument(), ESPERA)
    expect(onActivated).not.toHaveBeenCalled()
    expect(onError).not.toHaveBeenCalled()
  })

  it('activateUserButton_alConfirmar_activaLaCuentaYAvisaConElNombre', async () => {
    let cuerpo = null
    server.use(
      http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
        cuerpo = { id: params.id, ...(await request.json()) }
        return HttpResponse.json({ id: params.id, estado: 'ACTIVO' })
      }),
    )
    const user = userEvent.setup()
    const { onActivated, onError } = await renderBoton()

    const confirmacion = await abrirConfirmacion(user)
    await user.click(botonDe(confirmacion, 'Activar'))

    await waitFor(() => expect(onActivated).toHaveBeenCalledWith('Lucía Torres Vega'), ESPERA)
    expect(cuerpo).toEqual({ id: 'usr-4', estado: 'ACTIVO' })
    expect(onError).not.toHaveBeenCalled()
  })

  it('activateUserButton_siFallaLaActivacion_avisaDelError', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { onActivated, onError } = await renderBoton()

    const confirmacion = await abrirConfirmacion(user)
    await user.click(botonDe(confirmacion, 'Activar'))

    await waitFor(() => expect(onError).toHaveBeenCalledWith('Lucía Torres Vega'), ESPERA)
    expect(onActivated).not.toHaveBeenCalled()
  })
})
