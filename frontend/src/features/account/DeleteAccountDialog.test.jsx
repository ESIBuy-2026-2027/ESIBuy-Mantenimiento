import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import DeleteAccountDialog from './DeleteAccountDialog'

// Diálogo para que el vendedor elimine su propia cuenta (HU47): explica las consecuencias y pide la contraseña actual.

const CONTRASENA = 'Contraseña-de-prueba-1!'

// Guarda los cuerpos que llegan al backend y responde con el estado indicado
function registrarEliminaciones(respuesta = () => new HttpResponse(null, { status: 204 })) {
  const cuerpos = []
  server.use(
    http.delete('/api/perfil', async ({ request }) => {
      cuerpos.push(await request.json())
      return respuesta()
    }),
  )
  return cuerpos
}

function abrirDialogo() {
  const onClose = vi.fn()
  const onDeleted = vi.fn()
  renderWithProviders(<DeleteAccountDialog onClose={onClose} onDeleted={onDeleted} />)
  return { onClose, onDeleted }
}

describe('DeleteAccountDialog (HU47): confirmación', () => {
  it('HU47-CP09: explica qué se borra y qué se conserva, y sin la contraseña no deja eliminar', async () => {
    const usuario = userEvent.setup()
    abrirDialogo()

    const dialogo = screen.getByRole('alertdialog', { name: /eliminar mi cuenta/i })
    expect(dialogo).toHaveTextContent(/no se puede deshacer/i)
    expect(dialogo).toHaveTextContent(/productos que no tienen pedidos se eliminan/i)
    expect(dialogo).toHaveTextContent(/pedidos y las valoraciones se conservan/i)
    expect(dialogo).toHaveTextContent(/se cerrará la sesión/i)
    const eliminar = screen.getByRole('button', { name: /^eliminar mi cuenta$/i })
    expect(eliminar).toBeDisabled()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)

    expect(eliminar).toBeEnabled()
  })
})

describe('DeleteAccountDialog (HU47): eliminar', () => {
  it('HU47-CP10: con la contraseña escrita elimina la cuenta enviando solo la contraseña', async () => {
    const cuerpos = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(cuerpos).toEqual([{ password: CONTRASENA }])
  })
})

describe('DeleteAccountDialog (HU47): errores', () => {
  it('HU47-CP11: con la contraseña incorrecta lo avisa, no elimina y deja corregirla', async () => {
    registrarEliminaciones(() => HttpResponse.json({ code: 'WRONG_PASSWORD' }, { status: 400 }))
    const usuario = userEvent.setup()
    const { onClose, onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    expect(await screen.findByText('La contraseña actual no es correcta.')).toBeInTheDocument()
    expect(screen.getByRole('alertdialog')).not.toHaveTextContent(CONTRASENA)
    expect(screen.getByRole('button', { name: /^eliminar mi cuenta$/i })).toBeEnabled()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('HU47-CP11: si falla por otro motivo da un aviso genérico y no elimina', async () => {
    registrarEliminaciones(() => new HttpResponse(null, { status: 500 }))
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    expect(await screen.findByText('No se ha podido eliminar la cuenta. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(onDeleted).not.toHaveBeenCalled()
  })
})
