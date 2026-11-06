import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import DeleteAccountDialog from './DeleteAccountDialog'

// Diálogo para que el vendedor elimine su propia cuenta (HU47): explica las consecuencias y pide la contraseña actual.

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

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), 'Mock-Clave-1!')

    expect(eliminar).toBeEnabled()
  })
})
