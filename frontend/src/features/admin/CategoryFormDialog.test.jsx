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

const REPETIDO = 'Ya existe una categoría con ese nombre.'

describe('CategoryFormDialog (HU40): errores al crear', () => {
  it('HU40-CP14: si el nombre ya existe lo marca en el campo, no cierra y al cambiarlo desaparece el aviso', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/admin/categorias', () =>
        HttpResponse.json({ code: 'CATEGORY_NAME_ALREADY_EXISTS' }, { status: 409 }),
      ),
    )
    const { onSaved, onClose } = await renderFormulario()
    const nombre = screen.getByRole('textbox', { name: /nombre/i })

    await user.type(nombre, 'hogar')
    await user.click(screen.getByRole('button', { name: 'Crear' }))

    expect(await screen.findByText(REPETIDO)).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Nueva categoría' })).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
    expect(onClose).not.toHaveBeenCalled()

    await user.type(nombre, ' y jardín')
    expect(screen.queryByText(REPETIDO)).not.toBeInTheDocument()
  })

  it('HU40-CP14: cualquier otro error avisa con un mensaje genérico y deja el formulario abierto', async () => {
    const user = userEvent.setup()
    server.use(http.post('/api/admin/categorias', () => new HttpResponse(null, { status: 500 })))
    const { onSaved } = await renderFormulario()

    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Jardín')
    await user.click(screen.getByRole('button', { name: 'Crear' }))

    expect(await screen.findByText('No se ha podido crear la categoría. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /nombre/i })).toHaveValue('Jardín')
    expect(screen.getByRole('button', { name: 'Crear' })).toBeEnabled()
    expect(onSaved).not.toHaveBeenCalled()
  })
})

// Hogar (cat-2) del mock de categorías, con su descripción y 14 productos.
const HOGAR = {
  id: 'cat-2',
  nombre: 'Hogar',
  descripcion: 'Muebles y decoración.',
  estado: 'ACTIVA',
  numeroProductos: 14,
}

// Apunta cada PUT que llega al backend, para comprobar qué se envía y que cancelar no edita nada.
function capturarEdiciones() {
  const ediciones = []
  server.use(
    http.put('/api/admin/categorias/:id', async ({ request, params }) => {
      const cuerpo = await request.json()
      ediciones.push({ id: params.id, ...cuerpo })
      return HttpResponse.json({ ...HOGAR, ...cuerpo })
    }),
  )
  return ediciones
}

describe('CategoryFormDialog (HU41): formulario de edición', () => {
  it('HU41-CP16: con una categoría, muestra su nombre y su descripción para editarlos, sin el estado', async () => {
    await renderFormulario({ category: HOGAR })

    expect(screen.getByRole('dialog', { name: 'Editar categoría' })).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /nombre/i })).toHaveValue('Hogar')
    expect(screen.getByRole('textbox', { name: /descripción/i })).toHaveValue('Muebles y decoración.')
    expect(screen.queryByRole('radio')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Guardar' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Crear' })).not.toBeInTheDocument()
  })

  it('HU41-CP16: al cancelar se cierra sin editar nada', async () => {
    const user = userEvent.setup()
    const ediciones = capturarEdiciones()
    const { onClose, onSaved } = await renderFormulario({ category: HOGAR })

    await user.type(screen.getByRole('textbox', { name: /nombre/i }), ' y jardín')
    await user.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(onSaved).not.toHaveBeenCalled()
    expect(ediciones).toEqual([])
  })
})

describe('CategoryFormDialog (HU41): guardar', () => {
  it('HU41-CP17: envía el nombre y la descripción nuevos y avisa con la categoría editada', async () => {
    const user = userEvent.setup()
    const ediciones = capturarEdiciones()
    const { onSaved } = await renderFormulario({ category: HOGAR })

    const nombre = screen.getByRole('textbox', { name: /nombre/i })
    await user.clear(nombre)
    await user.type(nombre, 'Casa')
    const descripcion = screen.getByRole('textbox', { name: /descripción/i })
    await user.clear(descripcion)
    await user.type(descripcion, 'Todo para la casa.')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => expect(onSaved).toHaveBeenCalledTimes(1))
    expect(ediciones).toEqual([{ id: 'cat-2', nombre: 'Casa', descripcion: 'Todo para la casa.' }])
    expect(onSaved).toHaveBeenCalledWith(expect.objectContaining({ id: 'cat-2', nombre: 'Casa', estado: 'ACTIVA' }))
  })

  it('HU41-CP17: al editar también se valida que el nombre no quede vacío', async () => {
    const user = userEvent.setup()
    const ediciones = capturarEdiciones()
    await renderFormulario({ category: HOGAR })

    await user.clear(screen.getByRole('textbox', { name: /nombre/i }))
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText('Este campo es obligatorio')).toBeInTheDocument()
    expect(ediciones).toEqual([])
  })
})

describe('CategoryFormDialog (HU41): errores al guardar', () => {
  it('HU41-CP18: si el nombre nuevo es el de otra categoría lo marca en el campo y no cierra', async () => {
    const user = userEvent.setup()
    server.use(
      http.put('/api/admin/categorias/:id', () =>
        HttpResponse.json({ code: 'CATEGORY_NAME_ALREADY_EXISTS' }, { status: 409 }),
      ),
    )
    const { onSaved } = await renderFormulario({ category: HOGAR })

    const nombre = screen.getByRole('textbox', { name: /nombre/i })
    await user.clear(nombre)
    await user.type(nombre, 'Deportes')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText(REPETIDO)).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Editar categoría' })).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('HU41-CP18: si la categoría ya no existe avisa a quien abrió el formulario', async () => {
    const user = userEvent.setup()
    server.use(
      http.put('/api/admin/categorias/:id', () => HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })),
    )
    const onNotFound = vi.fn()
    const { onSaved } = await renderFormulario({ category: HOGAR, onNotFound })

    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    await waitFor(() => expect(onNotFound).toHaveBeenCalledTimes(1))
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('HU41-CP18: cualquier otro error avisa con un mensaje genérico y deja el formulario abierto', async () => {
    const user = userEvent.setup()
    server.use(http.put('/api/admin/categorias/:id', () => new HttpResponse(null, { status: 500 })))
    await renderFormulario({ category: HOGAR })

    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText('No se han podido guardar los cambios. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /nombre/i })).toHaveValue('Hogar')
  })
})
