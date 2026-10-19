import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserStatusButton from './UserStatusButton'

// Lucía Torres Vega es una clienta bloqueada y Carlos Moreno Gil un cliente activo del mock de usuarios.
const BLOQUEADA = { id: 'usr-4', nombre: 'Lucía', apellidos: 'Torres Vega' }
const ACTIVO = { id: 'usr-3', nombre: 'Carlos', apellidos: 'Moreno Gil' }
const ESPERA = { timeout: 3000 }

async function renderBoton(user, accion, props = {}) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onDone: vi.fn(), onError: vi.fn() }
  renderWithProviders(<UserStatusButton user={user} accion={accion} {...handlers} {...props} />)
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

describe('UserStatusButton: desbloquear (HU24)', () => {
  it('userStatusButton_desbloquear_muestraUnBotonConElNombreDelUsuario', async () => {
    await renderBoton(BLOQUEADA, 'desbloquear')

    expect(screen.getByRole('button', { name: 'Desbloquear a Lucía Torres Vega' })).toBeInTheDocument()
  })

  it('userStatusButton_desbloquear_pideConfirmacionSinDesbloquearTodavia', async () => {
    const user = userEvent.setup()
    const { onDone } = await renderBoton(BLOQUEADA, 'desbloquear')

    const confirmacion = await abrirConfirmacion(
      user,
      'Desbloquear a Lucía Torres Vega',
      /seguro que quieres desbloquear a lucía torres vega\? podrá volver a usar la plataforma/i,
    )

    expect(confirmacion).toHaveTextContent('Desbloquear usuario')
    expect(onDone).not.toHaveBeenCalled()
  })

  it('userStatusButton_desbloquear_alCancelar_noCambiaNada', async () => {
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(BLOQUEADA, 'desbloquear')

    const confirmacion = await abrirConfirmacion(user, 'Desbloquear a Lucía Torres Vega', /seguro que quieres desbloquear a lucía/i)
    await user.click(botonDe(confirmacion, 'Cancelar'))

    await waitFor(() => expect(screen.queryByText(/seguro que quieres desbloquear/i)).not.toBeInTheDocument(), ESPERA)
    expect(onDone).not.toHaveBeenCalled()
    expect(onError).not.toHaveBeenCalled()
  })

  it('userStatusButton_desbloquear_alConfirmar_enviaActivoYAvisaConElNombre', async () => {
    const peticion = capturarCambioDeEstado()
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(BLOQUEADA, 'desbloquear')

    const confirmacion = await abrirConfirmacion(user, 'Desbloquear a Lucía Torres Vega', /seguro que quieres desbloquear a lucía/i)
    await user.click(botonDe(confirmacion, 'Desbloquear'))

    await waitFor(() => expect(onDone).toHaveBeenCalledWith('Lucía Torres Vega'), ESPERA)
    expect(peticion).toEqual({ id: 'usr-4', cuerpo: { estado: 'ACTIVO' } })
    expect(onError).not.toHaveBeenCalled()
  })

  it('userStatusButton_desbloquear_siFalla_avisaDelError', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(BLOQUEADA, 'desbloquear')

    const confirmacion = await abrirConfirmacion(user, 'Desbloquear a Lucía Torres Vega', /seguro que quieres desbloquear a lucía/i)
    await user.click(botonDe(confirmacion, 'Desbloquear'))

    await waitFor(() => expect(onError).toHaveBeenCalledWith('Lucía Torres Vega', expect.anything()), ESPERA)
    expect(onDone).not.toHaveBeenCalled()
  })
})

describe('UserStatusButton: bloquear (HU23)', () => {
  it('userStatusButton_bloquear_muestraUnBotonConElNombreDelUsuario', async () => {
    await renderBoton(ACTIVO, 'bloquear')

    expect(screen.getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /desactivar/i })).not.toBeInTheDocument()
  })

  it('userStatusButton_bloquear_pideConfirmacionAvisandoDeQueNoPodraIniciarSesion', async () => {
    const user = userEvent.setup()
    const { onDone } = await renderBoton(ACTIVO, 'bloquear')

    const confirmacion = await abrirConfirmacion(
      user,
      'Bloquear a Carlos Moreno Gil',
      /seguro que quieres bloquear a carlos moreno gil\? no podrá iniciar sesión/i,
    )

    expect(confirmacion).toHaveTextContent('Bloquear usuario')
    expect(onDone).not.toHaveBeenCalled()
  })

  it('userStatusButton_bloquear_alConfirmar_enviaBloqueadoYAvisaConElNombre', async () => {
    const peticion = capturarCambioDeEstado()
    const user = userEvent.setup()
    const { onDone } = await renderBoton(ACTIVO, 'bloquear')

    const confirmacion = await abrirConfirmacion(user, 'Bloquear a Carlos Moreno Gil', /seguro que quieres bloquear/i)
    await user.click(botonDe(confirmacion, 'Bloquear'))

    await waitFor(() => expect(onDone).toHaveBeenCalledWith('Carlos Moreno Gil'), ESPERA)
    expect(peticion).toEqual({ id: 'usr-3', cuerpo: { estado: 'BLOQUEADO' } })
  })

  it('userStatusButton_bloquear_conConflicto_pasaElErrorConSuCodigo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    const { onDone, onError } = await renderBoton(ACTIVO, 'bloquear')

    const confirmacion = await abrirConfirmacion(user, 'Bloquear a Carlos Moreno Gil', /seguro que quieres bloquear/i)
    await user.click(botonDe(confirmacion, 'Bloquear'))

    await waitFor(
      () => expect(onError).toHaveBeenCalledWith('Carlos Moreno Gil', expect.objectContaining({ code: 'LAST_ACTIVE_ADMIN' })),
      ESPERA,
    )
    expect(onDone).not.toHaveBeenCalled()
  })

  it('userStatusButton_bloquear_deshabilitado_noSePuedePulsarYExplicaElMotivo', async () => {
    const user = userEvent.setup()
    const { onDone } = await renderBoton(ACTIVO, 'bloquear', { disabled: true, reason: 'No puedes bloquear tu propia cuenta.' })

    const boton = screen.getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })
    await user.click(boton)

    expect(boton).toBeDisabled()
    expect(boton).toHaveAttribute('title', 'No puedes bloquear tu propia cuenta.')
    expect(screen.queryByText(/seguro que quieres bloquear/i)).not.toBeInTheDocument()
    expect(onDone).not.toHaveBeenCalled()
  })
})
