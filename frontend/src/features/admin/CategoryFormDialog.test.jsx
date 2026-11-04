import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
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

describe('CategoryFormDialog (HU40): validación', () => {
  it.each([
    ['vacío', ''],
    ['con solo espacios', '   '],
  ])('HU40-CP12: con el nombre %s avisa de que es obligatorio y no envía nada', async (_caso, nombre) => {
    const user = userEvent.setup()
    const altas = capturarAltas()
    const { onSaved } = await renderFormulario()

    if (nombre) await user.type(screen.getByRole('textbox', { name: /nombre/i }), nombre)
    await user.click(screen.getByRole('button', { name: 'Crear' }))

    expect(await screen.findByText('Este campo es obligatorio')).toBeInTheDocument()
    expect(altas).toEqual([])
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('HU40-CP12: el nombre admite como mucho 50 caracteres y la descripción 200, como el backend', async () => {
    await renderFormulario()

    expect(screen.getByRole('textbox', { name: /nombre/i })).toHaveAttribute('maxlength', '50')
    expect(screen.getByRole('textbox', { name: /descripción/i })).toHaveAttribute('maxlength', '200')
  })
})

describe('CategoryFormDialog (HU40): crear', () => {
  it('HU40-CP13: envía el nombre, la descripción y el estado elegidos y avisa con la categoría creada', async () => {
    const user = userEvent.setup()
    const altas = capturarAltas()
    const { onSaved } = await renderFormulario()

    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Jardín')
    await user.type(screen.getByRole('textbox', { name: /descripción/i }), 'Plantas y herramientas')
    await user.click(screen.getByRole('radio', { name: 'Inactiva' }))
    await user.click(screen.getByRole('button', { name: 'Crear' }))

    await waitFor(() => expect(onSaved).toHaveBeenCalledTimes(1))
    expect(altas).toEqual([{ nombre: 'Jardín', descripcion: 'Plantas y herramientas', estado: 'INACTIVA' }])
    expect(onSaved).toHaveBeenCalledWith(expect.objectContaining({ id: 'cat-11', nombre: 'Jardín', estado: 'INACTIVA' }))
  })

  it('HU40-CP13: mientras se crea, los botones quedan deshabilitados', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/admin/categorias', async () => {
        await delay('infinite')
        return new HttpResponse(null, { status: 201 })
      }),
    )
    await renderFormulario()

    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Jardín')
    await user.click(screen.getByRole('button', { name: 'Crear' }))

    await waitFor(() => expect(screen.getByRole('button', { name: 'Crear' })).toBeDisabled())
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()
  })
})
