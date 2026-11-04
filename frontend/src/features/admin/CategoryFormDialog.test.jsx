import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import CategoryFormDialog from './CategoryFormDialog'

async function renderFormulario(props = {}) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onClose: vi.fn(), onSaved: vi.fn() }
  renderWithProviders(<CategoryFormDialog {...handlers} {...props} />)
  await screen.findByRole('dialog')
  return handlers
}

// Apunta cada POST que llega al backend, para comprobar que cancelar no crea nada.
function capturarAltas() {
  const altas = []
  server.use(
    http.post('/api/admin/categorias', async ({ request }) => {
      altas.push(await request.json())
      return HttpResponse.json({ id: 'cat-11', numeroProductos: 0, ...altas.at(-1) }, { status: 201 })
    }),
  )
  return altas
}

describe('CategoryFormDialog (HU40): formulario de creación', () => {
  it('HU40-CP11: pide el nombre, la descripción y el estado, con la categoría activa por defecto', async () => {
    await renderFormulario()

    expect(screen.getByRole('dialog', { name: 'Nueva categoría' })).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /nombre/i })).toHaveValue('')
    expect(screen.getByRole('textbox', { name: /descripción/i })).toHaveValue('')
    expect(screen.getByRole('radio', { name: 'Activa' })).toBeChecked()
    expect(screen.getByRole('radio', { name: 'Inactiva' })).not.toBeChecked()
    expect(screen.getByRole('button', { name: 'Crear' })).toBeInTheDocument()
  })

  it('HU40-CP11: al cancelar se cierra sin crear nada', async () => {
    const user = userEvent.setup()
    const altas = capturarAltas()
    const { onClose, onSaved } = await renderFormulario()

    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Jardín')
    await user.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(onSaved).not.toHaveBeenCalled()
    expect(altas).toEqual([])
  })
})
