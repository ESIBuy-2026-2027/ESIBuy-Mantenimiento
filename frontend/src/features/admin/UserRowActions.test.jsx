import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserRowActions from './UserRowActions'

// Del mock de usuarios: Carlos Moreno Gil (usr-3) es un cliente activo, Lucía Torres Vega (usr-4) una clienta
// bloqueada, Gonzalo Ibáñez Roca (usr-25) un cliente pendiente de verificación (HU1) y Ada Admin (u-1) el
// administrador con el que se inicia sesión.
const CARLOS = { id: 'usr-3', nombre: 'Carlos', apellidos: 'Moreno Gil', estado: 'ACTIVO' }
const LUCIA = { id: 'usr-4', nombre: 'Lucía', apellidos: 'Torres Vega', estado: 'BLOQUEADO' }
const GONZALO = { id: 'usr-25', nombre: 'Gonzalo', apellidos: 'Ibáñez Roca', estado: 'PENDIENTE' }
const ADA = { id: 'u-1', nombre: 'Ada', apellidos: 'Admin', estado: 'ACTIVO' }
const ESPERA = { timeout: 3000 }

async function renderAcciones(usuario, props = {}) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onResult: vi.fn(), onChanged: vi.fn() }
  renderWithProviders(
    <table>
      <tbody>
        <tr>
          <td>
            <UserRowActions user={usuario} currentUserId="u-1" {...handlers} {...props} />
          </td>
        </tr>
      </tbody>
    </table>,
  )
  return handlers
}

// La confirmación se busca por texto: mientras se abre o se cierra, Fluent marca partes de la página
// con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function confirmar(user, boton, pregunta, textoDelBotonDeConfirmar) {
  await user.click(screen.getByRole('button', { name: boton }))
  const confirmacion = (await screen.findByText(pregunta)).closest('[role="alertdialog"]')
  await user.click(within(confirmacion).getByText(textoDelBotonDeConfirmar, { selector: 'button' }))
}

describe('UserRowActions (HU19): qué acciones ofrece', () => {
  it('userRowActions_deUnaCuentaActiva_ofreceVerYBloquear', async () => {
    await renderAcciones(CARLOS)

    expect(screen.getByRole('button', { name: 'Ver información de Carlos Moreno Gil' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^desbloquear/i })).not.toBeInTheDocument()
  })

  it('userRowActions_deUnaCuentaBloqueada_ofreceDesbloquearYNoBloquear', async () => {
    await renderAcciones(LUCIA)

    expect(screen.getByRole('button', { name: 'Desbloquear a Lucía Torres Vega' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^bloquear/i })).not.toBeInTheDocument()
  })

  it('userRowActions_deUnaCuentaPendiente_ofreceDesbloquearParaAceptarla', async () => {
    await renderAcciones(GONZALO)

    expect(screen.getByRole('button', { name: 'Desbloquear a Gonzalo Ibáñez Roca' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^bloquear/i })).not.toBeInTheDocument()
  })

  it('userRowActions_nuncaOfreceDesactivarNiActivar', async () => {
    await renderAcciones(CARLOS)

    expect(screen.queryByRole('button', { name: /desactivar/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^activar/i })).not.toBeInTheDocument()
  })

  it('userRowActions_deLaPropiaCuenta_dejaElBloqueoDeshabilitadoYExplicaElMotivo', async () => {
    await renderAcciones(ADA)

    const boton = screen.getByRole('button', { name: 'Bloquear a Ada Admin' })
    expect(boton).toBeDisabled()
    expect(boton).toHaveAttribute('title', 'No puedes bloquear tu propia cuenta.')
  })
})

describe('UserRowActions (HU19): la ficha', () => {
  it('userRowActions_alPulsarLaLupa_abreLaFichaDeEseUsuario', async () => {
    const user = userEvent.setup()
    await renderAcciones(LUCIA)

    await user.click(screen.getByRole('button', { name: 'Ver información de Lucía Torres Vega' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    expect(await within(dialogo).findByLabelText('Email')).toHaveValue('lucia.torres@esibuy.test')
  })

  it('userRowActions_alCerrarLaFicha_vuelveALaFila', async () => {
    const user = userEvent.setup()
    await renderAcciones(LUCIA)
    await user.click(screen.getByRole('button', { name: 'Ver información de Lucía Torres Vega' }))
    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    await within(dialogo).findByLabelText('Nombre')

    await user.click(within(dialogo).getByRole('button', { name: 'Cerrar' }))

    await waitFor(() => expect(screen.queryByRole('dialog', { name: /información del usuario/i })).not.toBeInTheDocument(), ESPERA)
  })
})

describe('UserRowActions (HU23 y HU24): bloquear y desbloquear', () => {
  it('userRowActions_alBloquear_avisaConElMensajeYActualizaElListado', async () => {
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)

    await confirmar(user, 'Bloquear a Carlos Moreno Gil', /seguro que quieres bloquear a carlos moreno gil/i, 'Bloquear')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'success',
          texto: 'Carlos Moreno Gil está bloqueado y ya no puede iniciar sesión.',
        }),
      ESPERA,
    )
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('userRowActions_alDesbloquear_avisaConElMensajeYActualizaElListado', async () => {
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(LUCIA)

    await confirmar(user, 'Desbloquear a Lucía Torres Vega', /seguro que quieres desbloquear a lucía torres vega/i, 'Desbloquear')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'success',
          texto: 'Lucía Torres Vega ya está activo y puede usar la plataforma.',
        }),
      ESPERA,
    )
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('userRowActions_alDesbloquearUnaCuentaPendiente_laAcepta', async () => {
    const user = userEvent.setup()
    const { onResult } = await renderAcciones(GONZALO)

    await confirmar(user, 'Desbloquear a Gonzalo Ibáñez Roca', /seguro que quieres desbloquear a gonzalo/i, 'Desbloquear')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'success',
          texto: 'Gonzalo Ibáñez Roca ya está activo y puede usar la plataforma.',
        }),
      ESPERA,
    )
  })

  it('userRowActions_siEsElUltimoAdministradorActivo_muestraElMotivoYNoActualiza', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)

    await confirmar(user, 'Bloquear a Carlos Moreno Gil', /seguro que quieres bloquear/i, 'Bloquear')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'No se puede bloquear al último administrador activo.',
        }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
  })

  it('userRowActions_siElBackendDiceQueEsLaPropiaCuenta_muestraElMotivo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'CANNOT_BLOCK_SELF' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    const { onResult } = await renderAcciones(CARLOS)

    await confirmar(user, 'Bloquear a Carlos Moreno Gil', /seguro que quieres bloquear/i, 'Bloquear')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto: 'No puedes bloquear tu propia cuenta.' }),
      ESPERA,
    )
  })

  it('userRowActions_siFallaElCambio_muestraUnErrorGenericoYNoActualiza', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(LUCIA)

    await confirmar(user, 'Desbloquear a Lucía Torres Vega', /seguro que quieres desbloquear/i, 'Desbloquear')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'No se ha podido desbloquear a Lucía Torres Vega. Inténtalo de nuevo.',
        }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
  })
})

describe('UserRowActions (HU19): editar', () => {
  it('userRowActions_ofreceEditarYEliminarAunqueLaCuentaEsteBloqueada', async () => {
    await renderAcciones(LUCIA)

    expect(screen.getByRole('button', { name: 'Editar a Lucía Torres Vega' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Eliminar a Lucía Torres Vega' })).toBeInTheDocument()
  })

  it('userRowActions_alPulsarEditar_abreElFormularioDeEdicionConSusDatos', async () => {
    const user = userEvent.setup()
    await renderAcciones(CARLOS)

    await user.click(screen.getByRole('button', { name: 'Editar a Carlos Moreno Gil' }))

    const dialogo = await screen.findByRole('dialog', { name: /editar usuario/i })
    expect(await within(dialogo).findByLabelText(/^nombre/i)).toHaveValue('Carlos')
    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
  })

  it('userRowActions_alGuardarLaEdicion_avisaActualizaElListadoYCierraElFormulario', async () => {
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)
    await user.click(screen.getByRole('button', { name: 'Editar a Carlos Moreno Gil' }))
    const dialogo = await screen.findByRole('dialog', { name: /editar usuario/i })

    await user.clear(await within(dialogo).findByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Carla')
    await user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

    await waitFor(() => expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Los cambios se han guardado.' }), ESPERA)
    expect(onChanged).toHaveBeenCalledTimes(1)
    await waitFor(() => expect(screen.queryByRole('dialog', { name: /editar usuario/i })).not.toBeInTheDocument(), ESPERA)
  })

  it('userRowActions_alCancelarLaEdicion_cierraElFormularioSinAvisar', async () => {
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)
    await user.click(screen.getByRole('button', { name: 'Editar a Carlos Moreno Gil' }))
    const dialogo = await screen.findByRole('dialog', { name: /editar usuario/i })
    await within(dialogo).findByLabelText(/^nombre/i)

    await user.click(within(dialogo).getByRole('button', { name: /^cancelar$/i }))

    await waitFor(() => expect(screen.queryByRole('dialog', { name: /editar usuario/i })).not.toBeInTheDocument(), ESPERA)
    expect(onResult).not.toHaveBeenCalled()
    expect(onChanged).not.toHaveBeenCalled()
  })
})

describe('UserRowActions (HU19): eliminar', () => {
  function capturarBorrado() {
    const peticion = { id: null, llamadas: 0 }
    server.use(
      http.delete('/api/admin/usuarios/:id', ({ params }) => {
        peticion.llamadas += 1
        peticion.id = params.id
        return new HttpResponse(null, { status: 204 })
      }),
    )
    return peticion
  }

  it('userRowActions_alPulsarEliminar_pideConfirmacionAvisandoDeQueNoSePuedeDeshacer', async () => {
    const peticion = capturarBorrado()
    const user = userEvent.setup()
    await renderAcciones(CARLOS)

    await user.click(screen.getByRole('button', { name: 'Eliminar a Carlos Moreno Gil' }))

    const confirmacion = (
      await screen.findByText('¿Seguro que quieres eliminar a Carlos Moreno Gil? Esta acción no se puede deshacer.')
    ).closest('[role="alertdialog"]')
    expect(confirmacion).toHaveTextContent('Eliminar usuario')
    expect(peticion.llamadas).toBe(0)
  })

  it('userRowActions_alCancelarLaEliminacion_noBorraNada', async () => {
    const peticion = capturarBorrado()
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)
    await user.click(screen.getByRole('button', { name: 'Eliminar a Carlos Moreno Gil' }))
    const confirmacion = (await screen.findByText(/seguro que quieres eliminar a carlos/i)).closest('[role="alertdialog"]')

    await user.click(within(confirmacion).getByText('Cancelar', { selector: 'button' }))

    await waitFor(() => expect(screen.queryByText(/seguro que quieres eliminar/i)).not.toBeInTheDocument(), ESPERA)
    expect(peticion.llamadas).toBe(0)
    expect(onResult).not.toHaveBeenCalled()
    expect(onChanged).not.toHaveBeenCalled()
  })

  it('userRowActions_alConfirmarLaEliminacion_borraAvisaYActualizaElListado', async () => {
    const peticion = capturarBorrado()
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)

    await confirmar(user, 'Eliminar a Carlos Moreno Gil', /seguro que quieres eliminar a carlos moreno gil/i, 'Eliminar')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Carlos Moreno Gil se ha eliminado.' }),
      ESPERA,
    )
    expect(peticion).toEqual({ id: 'usr-3', llamadas: 1 })
    expect(onChanged).toHaveBeenCalledTimes(1)
  })

  it('userRowActions_siEsElUltimoAdministradorActivo_muestraElMotivoDeLaEliminacion', async () => {
    server.use(
      http.delete('/api/admin/usuarios/:id', () => HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 })),
    )
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)

    await confirmar(user, 'Eliminar a Carlos Moreno Gil', /seguro que quieres eliminar/i, 'Eliminar')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'No se puede eliminar al último administrador activo.',
        }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
  })

  it('userRowActions_siElBackendDiceQueEsLaPropiaCuenta_muestraElMotivoDeLaEliminacion', async () => {
    server.use(
      http.delete('/api/admin/usuarios/:id', () => HttpResponse.json({ code: 'CANNOT_DELETE_SELF' }, { status: 409 })),
    )
    const user = userEvent.setup()
    const { onResult } = await renderAcciones(CARLOS)

    await confirmar(user, 'Eliminar a Carlos Moreno Gil', /seguro que quieres eliminar/i, 'Eliminar')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto: 'No puedes eliminar tu propia cuenta.' }),
      ESPERA,
    )
  })

  it('userRowActions_siFallaLaEliminacion_muestraUnErrorGenericoYNoActualiza', async () => {
    server.use(http.delete('/api/admin/usuarios/:id', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(CARLOS)

    await confirmar(user, 'Eliminar a Carlos Moreno Gil', /seguro que quieres eliminar/i, 'Eliminar')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'No se ha podido eliminar a Carlos Moreno Gil. Inténtalo de nuevo.',
        }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
  })
})
