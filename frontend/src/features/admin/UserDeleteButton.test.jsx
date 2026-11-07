import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserDeleteButton from './UserDeleteButton'

// Del mock de usuarios: Ana López García (usr-1) y Diego Delgado Iglesias (usr-11) son administradores, y Ada
// Admin (u-1) es el administrador con el que se inicia sesión.
const ANA = { id: 'usr-1', nombre: 'Ana', apellidos: 'López García' }
const DIEGO = { id: 'usr-11', nombre: 'Diego', apellidos: 'Delgado Iglesias' }
const ADA = { id: 'u-1', nombre: 'Ada', apellidos: 'Admin' }
const ESPERA = { timeout: 3000 }

async function renderBoton(usuario, props = {}) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onResult: vi.fn(), onChanged: vi.fn(), onDeleted: vi.fn() }
  renderWithProviders(<UserDeleteButton user={usuario} currentUserId="u-1" {...handlers} {...props} />)
  return handlers
}

const boton = (nombre) => screen.getByRole('button', { name: `Eliminar a ${nombre}` })

// La confirmación se busca por texto: mientras se abre o se cierra, Fluent marca partes de la página
// con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function pedirConfirmacion(user, nombre) {
  await user.click(boton(nombre))
  return (await screen.findByText(/seguro que quieres eliminar a/i)).closest('[role="alertdialog"]')
}

async function confirmarEliminacion(user, nombre) {
  const confirmacion = await pedirConfirmacion(user, nombre)
  await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))
}

function respondeElBorrado(respuesta) {
  const peticion = { id: null, llamadas: 0 }
  server.use(
    http.delete('/api/admin/usuarios/:id', ({ params }) => {
      peticion.llamadas += 1
      peticion.id = params.id
      return respuesta()
    }),
  )
  return peticion
}

describe('UserDeleteButton (HU37): confirmar y eliminar', () => {
  it('HU37-CP12: al pulsar la papelera pide confirmación con el nombre y avisa de que no se puede deshacer', async () => {
    const peticion = respondeElBorrado(() => new HttpResponse(null, { status: 204 }))
    const user = userEvent.setup()
    await renderBoton(ANA)

    const confirmacion = await pedirConfirmacion(user, 'Ana López García')

    expect(confirmacion).toHaveTextContent('Eliminar usuario')
    expect(confirmacion).toHaveTextContent('¿Seguro que quieres eliminar a Ana López García? Esta acción no se puede deshacer.')
    expect(peticion.llamadas).toBe(0)
  })

  it('HU37-CP12: al confirmar elimina a ese administrador, avisa y pide actualizar la lista', async () => {
    const peticion = respondeElBorrado(() => new HttpResponse(null, { status: 204 }))
    const user = userEvent.setup()
    const { onResult, onChanged, onDeleted } = await renderBoton(ANA)

    await confirmarEliminacion(user, 'Ana López García')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Ana López García se ha eliminado.' }),
      ESPERA,
    )
    expect(peticion).toEqual({ id: 'usr-1', llamadas: 1 })
    expect(onChanged).toHaveBeenCalledTimes(1)
    expect(onDeleted).toHaveBeenCalledTimes(1)
    await waitFor(() => expect(screen.queryByText(/seguro que quieres eliminar/i)).not.toBeInTheDocument(), ESPERA)
  })

  it('HU37-CP12: con un nombre indicado (label), la confirmación y el aviso lo usan', async () => {
    respondeElBorrado(() => new HttpResponse(null, { status: 204 }))
    const user = userEvent.setup()
    const { onResult } = await renderBoton(ANA, { label: 'Ana (soporte)' })

    await confirmarEliminacion(user, 'Ana (soporte)')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Ana (soporte) se ha eliminado.' }),
      ESPERA,
    )
  })

  it('HU37-CP13: al cancelar no borra nada, no avisa ni actualiza la lista', async () => {
    const peticion = respondeElBorrado(() => new HttpResponse(null, { status: 204 }))
    const user = userEvent.setup()
    const { onResult, onChanged, onDeleted } = await renderBoton(ANA)
    const confirmacion = await pedirConfirmacion(user, 'Ana López García')

    await user.click(within(confirmacion).getByText('Cancelar', { selector: 'button' }))

    await waitFor(() => expect(screen.queryByText(/seguro que quieres eliminar/i)).not.toBeInTheDocument(), ESPERA)
    expect(peticion.llamadas).toBe(0)
    expect(onResult).not.toHaveBeenCalled()
    expect(onChanged).not.toHaveBeenCalled()
    expect(onDeleted).not.toHaveBeenCalled()
  })

  it('HU37-CP12: con withText el botón muestra el texto Eliminar además del icono', async () => {
    await renderBoton(ANA, { withText: true })

    expect(boton('Ana López García')).toHaveTextContent('Eliminar')
  })

  it('HU37-CP12: sin withText el botón es solo un icono', async () => {
    await renderBoton(ANA)

    expect(boton('Ana López García')).not.toHaveTextContent('Eliminar')
    expect(boton('Ana López García')).toHaveAttribute('title', 'Eliminar')
  })
})

describe('UserDeleteButton (HU37): cuentas protegidas', () => {
  it('HU37-CP15: la propia cuenta queda deshabilitada y explica el motivo', async () => {
    const user = userEvent.setup()
    await renderBoton(ADA)

    expect(boton('Ada Admin')).toBeDisabled()
    expect(boton('Ada Admin')).toHaveAttribute('title', 'No puedes eliminar tu propia cuenta.')
    await user.click(boton('Ada Admin'))
    expect(screen.queryByText(/seguro que quieres eliminar/i)).not.toBeInTheDocument()
  })

  it('HU37-CP15: el último administrador activo queda deshabilitado y explica el motivo', async () => {
    await renderBoton(ANA, { lastAdminId: 'usr-1' })

    expect(boton('Ana López García')).toBeDisabled()
    expect(boton('Ana López García')).toHaveAttribute('title', 'No se puede eliminar al último administrador activo.')
  })

  it('HU37-CP15: si el último administrador es otro, o no se sabe, queda disponible', async () => {
    await renderBoton(ANA, { lastAdminId: 'usr-99' })

    expect(boton('Ana López García')).toBeEnabled()
  })
})

describe('UserDeleteButton (HU37): errores', () => {
  it.each([
    ['CANNOT_DELETE_SELF', 'No puedes eliminar tu propia cuenta.'],
    ['LAST_ACTIVE_ADMIN', 'No se puede eliminar al último administrador activo.'],
  ])('HU37-CP16: un 409 %s muestra su motivo y no actualiza la lista', async (code, texto) => {
    respondeElBorrado(() => HttpResponse.json({ code }, { status: 409 }))
    const user = userEvent.setup()
    const { onResult, onChanged, onDeleted } = await renderBoton(ANA)

    await confirmarEliminacion(user, 'Ana López García')

    await waitFor(() => expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto }), ESPERA)
    expect(onChanged).not.toHaveBeenCalled()
    expect(onDeleted).not.toHaveBeenCalled()
  })

  it('HU37-CP17: un 404 (alguien lo eliminó antes) avisa con su mensaje propio y actualiza la lista', async () => {
    respondeElBorrado(() => new HttpResponse(null, { status: 404 }))
    const user = userEvent.setup()
    const { onResult, onChanged, onDeleted } = await renderBoton(DIEGO)

    await confirmarEliminacion(user, 'Diego Delgado Iglesias')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'Diego Delgado Iglesias ya no existe. Se ha actualizado la lista.',
        }),
      ESPERA,
    )
    expect(onChanged).toHaveBeenCalledTimes(1)
    expect(onDeleted).toHaveBeenCalledTimes(1)
  })

  it('HU37-CP16: cualquier otro fallo muestra un error genérico y no actualiza la lista', async () => {
    respondeElBorrado(() => new HttpResponse(null, { status: 500 }))
    const user = userEvent.setup()
    const { onResult, onChanged, onDeleted } = await renderBoton(ANA)

    await confirmarEliminacion(user, 'Ana López García')

    await waitFor(
      () =>
        expect(onResult).toHaveBeenCalledWith({
          intent: 'error',
          texto: 'No se ha podido eliminar a Ana López García. Inténtalo de nuevo.',
        }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
    expect(onDeleted).not.toHaveBeenCalled()
  })

  it('HU37-CP16: con el mock real, eliminarse a sí mismo da el motivo y no borra la cuenta', async () => {
    const user = userEvent.setup()
    // El botón de la propia cuenta está deshabilitado: se simula una ficha sin saber quién está en sesión.
    const { onResult, onChanged } = await renderBoton(ADA, { currentUserId: undefined })

    await confirmarEliminacion(user, 'Ada Admin')

    await waitFor(
      () => expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto: 'No puedes eliminar tu propia cuenta.' }),
      ESPERA,
    )
    expect(onChanged).not.toHaveBeenCalled()
  })
})
