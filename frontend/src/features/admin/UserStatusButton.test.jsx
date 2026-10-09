import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserStatusButton from './UserStatusButton'

// Lucía Torres Vega es una clienta desactivada y Carlos Moreno Gil un cliente activo del mock de usuarios.
const DESACTIVADA = { id: 'usr-4', nombre: 'Lucía', apellidos: 'Torres Vega' }
const ACTIVO = { id: 'usr-3', nombre: 'Carlos', apellidos: 'Moreno Gil' }
const ESPERA = { timeout: 3000 }

async function renderBoton(user, accion) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onDone: vi.fn(), onError: vi.fn() }
  renderWithProviders(<UserStatusButton user={user} accion={accion} {...handlers} />)
  return handlers
}

// Se busca por texto y no por rol: mientras la confirmación se abre o se cierra, Fluent marca
// partes de la página con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function abrirConfirmacion(usuarioDelTest, boton, pregunta) {
  await usuarioDelTest.click(screen.getByRole('button', { name: boton }))
  return (await screen.findByText(pregunta)).closest('[role="alertdialog"]')
}

const botonDe = (confirmacion, texto) => within(confirmacion).getByText(texto, { selector: 'button' })

function capturarCambioDeEstado() {
  const peticion = {}
  server.use(
    http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
      peticion.id = params.id
      peticion.cuerpo = await request.json()
      return HttpResponse.json({ id: params.id, ...peticion.cuerpo })
    }),
  )
  return peticion
}

describe('UserStatusButton: activar (HU24)', () => {
  it('userStatusButton_activar_muestraElBotonConElNombreDelUsuario', async () => {
    await renderBoton(DESACTIVADA, 'activar')

    expect(screen.getByRole('button', { name: 'Activar a Lucía Torres Vega' })).toHaveTextContent('Activar')
  })

  it('userStatusButton_activar_pideConfirmacionSinActivarTodavia', async () => {
    const user = userEvent.setup()
    const { onDone } = await renderBoton(DESACTIVADA, 'activar')

    const confirmacion = await abrirConfirmacion(user, 'Activar a Lucía Torres Vega', /seguro que quieres activar a lucía/i)

    expect(confirmacion).toHaveTextContent('Activar usuario')
    expect(onDone).not.toHaveBeenCalled()
  })

  it('userStatusButton_activar_alCancelar_noCambiaNada', async () => {
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(DESACTIVADA, 'activar')

    const confirmacion = await abrirConfirmacion(user, 'Activar a Lucía Torres Vega', /seguro que quieres activar a lucía/i)
    await user.click(botonDe(confirmacion, 'Cancelar'))

    await waitFor(() => expect(screen.queryByText(/seguro que quieres activar/i)).not.toBeInTheDocument(), ESPERA)
    expect(onDone).not.toHaveBeenCalled()
    expect(onError).not.toHaveBeenCalled()
  })

  it('userStatusButton_activar_alConfirmar_enviaActivoYAvisaConElNombre', async () => {
    const peticion = capturarCambioDeEstado()
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(DESACTIVADA, 'activar')

    const confirmacion = await abrirConfirmacion(user, 'Activar a Lucía Torres Vega', /seguro que quieres activar a lucía/i)
    await user.click(botonDe(confirmacion, 'Activar'))

    await waitFor(() => expect(onDone).toHaveBeenCalledWith('Lucía Torres Vega'), ESPERA)
    expect(peticion).toEqual({ id: 'usr-4', cuerpo: { estado: 'ACTIVO' } })
    expect(onError).not.toHaveBeenCalled()
  })

  it('userStatusButton_activar_siFalla_avisaDelError', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(DESACTIVADA, 'activar')

    const confirmacion = await abrirConfirmacion(user, 'Activar a Lucía Torres Vega', /seguro que quieres activar a lucía/i)
    await user.click(botonDe(confirmacion, 'Activar'))

    await waitFor(() => expect(onError).toHaveBeenCalledWith('Lucía Torres Vega', expect.anything()), ESPERA)
    expect(onDone).not.toHaveBeenCalled()
  })
})

describe('UserStatusButton: desactivar (HU23)', () => {
  it('userStatusButton_desactivar_muestraElBotonDesactivarConElNombre', async () => {
    await renderBoton(ACTIVO, 'desactivar')

    expect(screen.getByRole('button', { name: 'Desactivar a Carlos Moreno Gil' })).toHaveTextContent('Desactivar')
  })

  it('userStatusButton_desactivar_pideConfirmacionAvisandoDeQueNoPodraIniciarSesion', async () => {
    const user = userEvent.setup()
    const { onDone } = await renderBoton(ACTIVO, 'desactivar')

    const confirmacion = await abrirConfirmacion(
      user,
      'Desactivar a Carlos Moreno Gil',
      /seguro que quieres desactivar a carlos moreno gil\? no podrá iniciar sesión/i,
    )

    expect(confirmacion).toHaveTextContent('Desactivar usuario')
    expect(onDone).not.toHaveBeenCalled()
  })

  it('userStatusButton_desactivar_alConfirmar_enviaDesactivadoYAvisaConElNombre', async () => {
    const peticion = capturarCambioDeEstado()
    const user = userEvent.setup()
    const { onDone } = await renderBoton(ACTIVO, 'desactivar')

    const confirmacion = await abrirConfirmacion(user, 'Desactivar a Carlos Moreno Gil', /seguro que quieres desactivar/i)
    await user.click(botonDe(confirmacion, 'Desactivar'))

    await waitFor(() => expect(onDone).toHaveBeenCalledWith('Carlos Moreno Gil'), ESPERA)
    expect(peticion).toEqual({ id: 'usr-3', cuerpo: { estado: 'DESACTIVADO' } })
  })

  it('userStatusButton_desactivar_conConflicto_pasaElErrorConSuCodigo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(ACTIVO, 'desactivar')

    const confirmacion = await abrirConfirmacion(user, 'Desactivar a Carlos Moreno Gil', /seguro que quieres desactivar/i)
    await user.click(botonDe(confirmacion, 'Desactivar'))

    await waitFor(
      () => expect(onError).toHaveBeenCalledWith('Carlos Moreno Gil', expect.objectContaining({ code: 'LAST_ACTIVE_ADMIN' })),
      ESPERA,
    )
    expect(onDone).not.toHaveBeenCalled()
  })
})
