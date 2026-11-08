import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import UnsavedChangesDialog from './UnsavedChangesDialog'

function renderizar() {
  const props = { onStay: vi.fn(), onLeave: vi.fn() }
  renderWithProviders(<UnsavedChangesDialog {...props} />)
  return props
}

describe('UnsavedChangesDialog (HU45): salir con cambios sin guardar', () => {
  it('HU45-CP16: avisa de que hay cambios sin guardar y de que se perderán si se sale', () => {
    renderizar()

    const aviso = screen.getByRole('alertdialog', { name: '¿Salir sin guardar?' })
    expect(aviso).toHaveTextContent('Tienes cambios sin guardar. Si sales, se perderán.')
  })

  it('HU45-CP16: el foco empieza en la opción segura, seguir editando', () => {
    renderizar()

    expect(screen.getByRole('button', { name: 'Seguir editando' })).toHaveFocus()
  })

  it('HU45-CP16: seguir editando avisa de que se quiere seguir en la pantalla', async () => {
    const user = userEvent.setup()
    const { onStay, onLeave } = renderizar()

    await user.click(screen.getByRole('button', { name: 'Seguir editando' }))

    expect(onStay).toHaveBeenCalledTimes(1)
    expect(onLeave).not.toHaveBeenCalled()
  })

  it('HU45-CP16: salir sin guardar avisa de que se descartan los cambios', async () => {
    const user = userEvent.setup()
    const { onStay, onLeave } = renderizar()

    await user.click(screen.getByRole('button', { name: 'Salir sin guardar' }))

    expect(onLeave).toHaveBeenCalledTimes(1)
    expect(onStay).not.toHaveBeenCalled()
  })

  it('HU45-CP16: con la tecla Escape se sigue editando, no se descarta nada', async () => {
    const user = userEvent.setup()
    const { onStay, onLeave } = renderizar()

    await user.keyboard('{Escape}')

    expect(onStay).toHaveBeenCalledTimes(1)
    expect(onLeave).not.toHaveBeenCalled()
  })
})
