import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import CategoryRowActions from './CategoryRowActions'

// Del mock de categorías: Mascotas (cat-9) es una categoría activa sin productos.
const MASCOTAS = {
  id: 'cat-9',
  nombre: 'Mascotas',
  descripcion: 'Alimento y accesorios para perros, gatos y otras mascotas.',
  estado: 'ACTIVA',
  numeroProductos: 0,
}
// Tecnología (cat-1) tiene 18 productos: el backend no deja eliminarla.
const TECNOLOGIA = {
  id: 'cat-1',
  nombre: 'Tecnología',
  descripcion: 'Ordenadores, móviles y accesorios.',
  estado: 'ACTIVA',
  numeroProductos: 18,
}
// Moda (cat-10) es una categoría inactiva sin productos.
const MODA = { id: 'cat-10', nombre: 'Moda', descripcion: '', estado: 'INACTIVA', numeroProductos: 0 }
const CON_PRODUCTOS = 'No se puede eliminar una categoría que tiene productos.'
const PREGUNTA = '¿Seguro que quieres eliminar la categoría Mascotas? Esta acción no se puede deshacer.'
const ESPERA = { timeout: 3000 }

async function renderAcciones(categoria) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onResult: vi.fn(), onChanged: vi.fn() }
  renderWithProviders(
    <table>
      <tbody>
        <tr>
          <td>
            <CategoryRowActions category={categoria} {...handlers} />
          </td>
        </tr>
      </tbody>
    </table>,
  )
  return handlers
}

// Apunta cada DELETE que llega al backend, para comprobar que cancelar no borra nada.
function capturarBorrados() {
  const borrados = []
  server.use(
    http.delete('/api/admin/categorias/:id', ({ params }) => {
      borrados.push(params.id)
      return new HttpResponse(null, { status: 204 })
    }),
  )
  return borrados
}

// Apunta cada cambio de estado que llega al backend, para comprobar que cancelar no cambia nada.
function capturarCambiosDeEstado() {
  const cambios = []
  server.use(
    http.patch('/api/admin/categorias/:id/status', async ({ params, request }) => {
      cambios.push({ id: params.id, ...(await request.json()) })
      return HttpResponse.json({ ...MASCOTAS, estado: 'INACTIVA' })
    }),
  )
  return cambios
}

const DESACTIVAR_MASCOTAS =
  '¿Seguro que quieres desactivar la categoría Mascotas? Dejará de mostrarse a los clientes y a los vendedores.'
const ACTIVAR_MODA = '¿Seguro que quieres activar la categoría Moda? Volverá a mostrarse a los clientes y a los vendedores.'

describe('CategoryRowActions (HU41): activar y desactivar', () => {
  it('HU41-CP28: una categoría activa ofrece desactivarla', async () => {
    await renderAcciones(MASCOTAS)
    expect(screen.getByRole('button', { name: 'Desactivar Mascotas' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Activar Mascotas' })).not.toBeInTheDocument()
  })

  it('HU41-CP28: una categoría inactiva ofrece activarla', async () => {
    await renderAcciones(MODA)

    expect(screen.getByRole('button', { name: 'Activar Moda' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Desactivar Moda' })).not.toBeInTheDocument()
  })

  it('HU41-CP28: al pulsar Desactivar pide confirmación avisando de que dejará de mostrarse', async () => {
    const user = userEvent.setup()
    await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Desactivar Mascotas' }))

    const confirmacion = (await screen.findByText(DESACTIVAR_MASCOTAS)).closest('[role="alertdialog"]')
    expect(confirmacion).toHaveTextContent('Desactivar categoría')
    expect(within(confirmacion).getByText('Desactivar', { selector: 'button' })).toBeInTheDocument()
  })

  it('HU41-CP28: al pulsar Activar pide confirmación avisando de que volverá a mostrarse', async () => {
    const user = userEvent.setup()
    await renderAcciones(MODA)

    await user.click(screen.getByRole('button', { name: 'Activar Moda' }))

    const confirmacion = (await screen.findByText(ACTIVAR_MODA)).closest('[role="alertdialog"]')
    expect(confirmacion).toHaveTextContent('Activar categoría')
    expect(within(confirmacion).getByText('Activar', { selector: 'button' })).toBeInTheDocument()
  })

  it('HU41-CP28: al cancelar se cierra la confirmación y no se cambia nada', async () => {
    const user = userEvent.setup()
    const cambios = capturarCambiosDeEstado()
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Desactivar Mascotas' }))
    const confirmacion = (await screen.findByText(DESACTIVAR_MASCOTAS)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Cancelar', { selector: 'button' }))

    await waitFor(() => expect(screen.queryByText(DESACTIVAR_MASCOTAS)).not.toBeInTheDocument(), ESPERA)
    expect(cambios).toEqual([])
    expect(onResult).not.toHaveBeenCalled()
    expect(onChanged).not.toHaveBeenCalled()
  })
})

describe('CategoryRowActions (HU41): cambiar el estado', () => {
  it('HU41-CP29: al confirmar desactiva la categoría, avisa y pide actualizar la lista', async () => {
    const user = userEvent.setup()
    const cambios = capturarCambiosDeEstado()
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Desactivar Mascotas' }))
    const confirmacion = (await screen.findByText(DESACTIVAR_MASCOTAS)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Desactivar', { selector: 'button' }))

    await waitFor(() => expect(onChanged).toHaveBeenCalledTimes(1), ESPERA)
    expect(cambios).toEqual([{ id: 'cat-9', estado: 'INACTIVA' }])
    expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Mascotas se ha desactivado.' })
    await waitFor(() => expect(screen.queryByText(DESACTIVAR_MASCOTAS)).not.toBeInTheDocument(), ESPERA)
  })

  it('HU41-CP29: al confirmar activa una categoría inactiva y avisa', async () => {
    const user = userEvent.setup()
    const cambios = capturarCambiosDeEstado()
    const { onResult, onChanged } = await renderAcciones(MODA)

    await user.click(screen.getByRole('button', { name: 'Activar Moda' }))
    const confirmacion = (await screen.findByText(ACTIVAR_MODA)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Activar', { selector: 'button' }))

    await waitFor(() => expect(onChanged).toHaveBeenCalledTimes(1), ESPERA)
    expect(cambios).toEqual([{ id: 'cat-10', estado: 'ACTIVA' }])
    expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Moda se ha activado.' })
  })

  it('HU41-CP29: mientras se cambia el estado, los botones de la confirmación quedan deshabilitados', async () => {
    const user = userEvent.setup()
    server.use(
      http.patch('/api/admin/categorias/:id/status', async () => {
        await delay('infinite')
      }),
    )
    await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Desactivar Mascotas' }))
    const confirmacion = (await screen.findByText(DESACTIVAR_MASCOTAS)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Desactivar', { selector: 'button' }))

    await waitFor(() => expect(within(confirmacion).getByText('Desactivar', { selector: 'button' })).toBeDisabled())
    expect(within(confirmacion).getByText('Cancelar', { selector: 'button' })).toBeDisabled()
  })
})

describe('CategoryRowActions (HU42): confirmación', () => {
  it('HU42-CP17: ofrece la lupa para ver la ficha y la papelera para eliminar la categoría', async () => {
    await renderAcciones(MASCOTAS)

    expect(screen.getByRole('button', { name: 'Ver información de Mascotas' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Eliminar Mascotas' })).toBeEnabled()
  })

  it('HU42-CP17: al pulsar la papelera pide confirmación avisando de que no se puede deshacer', async () => {
    const user = userEvent.setup()
    await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))

    const confirmacion = (await screen.findByText(PREGUNTA)).closest('[role="alertdialog"]')
    expect(confirmacion).toHaveTextContent('Eliminar categoría')
    expect(within(confirmacion).getByText('Eliminar', { selector: 'button' })).toBeInTheDocument()
  })

  it('HU42-CP17: al cancelar se cierra la confirmación y no se borra nada', async () => {
    const user = userEvent.setup()
    const borrados = capturarBorrados()
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
    const confirmacion = (await screen.findByText(PREGUNTA)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Cancelar', { selector: 'button' }))

    await waitFor(() => expect(screen.queryByText(PREGUNTA)).not.toBeInTheDocument(), ESPERA)
    expect(borrados).toEqual([])
    expect(onResult).not.toHaveBeenCalled()
    expect(onChanged).not.toHaveBeenCalled()
  })
})

describe('CategoryRowActions (HU41): editar', () => {
  it('HU41-CP16: ofrece el lápiz para editar la categoría, que abre su formulario con sus datos', async () => {
    const user = userEvent.setup()
    await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Editar Mascotas' }))

    const formulario = await screen.findByRole('dialog', { name: 'Editar categoría' })
    expect(within(formulario).getByRole('textbox', { name: /nombre/i })).toHaveValue('Mascotas')
  })

  it('HU41-CP17: al guardar, avisa de que se ha actualizado y pide actualizar la lista', async () => {
    const user = userEvent.setup()
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Editar Mascotas' }))
    const formulario = await screen.findByRole('dialog', { name: 'Editar categoría' })
    const nombre = within(formulario).getByRole('textbox', { name: /nombre/i })
    await user.clear(nombre)
    await user.type(nombre, 'Animales')
    await user.click(within(formulario).getByRole('button', { name: 'Guardar' }))

    await waitFor(() => expect(onChanged).toHaveBeenCalledTimes(1), ESPERA)
    expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Animales se ha actualizado.' })
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument(), ESPERA)
  })
})

describe('CategoryRowActions (HU41): la categoría ya no existe', () => {
  it('HU41-CP18: al guardar una categoría que ya no existe, cierra el formulario, avisa y pide actualizar la lista', async () => {
    const user = userEvent.setup()
    server.use(
      http.put('/api/admin/categorias/:id', () => HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })),
    )
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Editar Mascotas' }))
    const formulario = await screen.findByRole('dialog', { name: 'Editar categoría' })
    await user.click(within(formulario).getByRole('button', { name: 'Guardar' }))

    await waitFor(() => expect(onChanged).toHaveBeenCalledTimes(1), ESPERA)
    expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto: 'Mascotas ya no existe.' })
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument(), ESPERA)
  })
})

describe('CategoryRowActions (HU42): eliminar', () => {
  it('HU42-CP18: al confirmar borra la categoría, avisa de que se ha eliminado y pide actualizar la lista', async () => {
    const user = userEvent.setup()
    const borrados = capturarBorrados()
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
    const confirmacion = (await screen.findByText(PREGUNTA)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))

    await waitFor(() => expect(onChanged).toHaveBeenCalledTimes(1), ESPERA)
    expect(borrados).toEqual(['cat-9'])
    expect(onResult).toHaveBeenCalledWith({ intent: 'success', texto: 'Mascotas se ha eliminado.' })
    await waitFor(() => expect(screen.queryByText(PREGUNTA)).not.toBeInTheDocument(), ESPERA)
  })

  it('HU42-CP18: mientras se elimina, los botones de la confirmación quedan deshabilitados', async () => {
    const user = userEvent.setup()
    server.use(
      http.delete('/api/admin/categorias/:id', async () => {
        await delay('infinite')
        return new HttpResponse(null, { status: 204 })
      }),
    )
    await renderAcciones(MASCOTAS)

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
    const confirmacion = (await screen.findByText(PREGUNTA)).closest('[role="alertdialog"]')
    await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))

    await waitFor(() => expect(within(confirmacion).getByText('Eliminar', { selector: 'button' })).toBeDisabled())
    expect(within(confirmacion).getByText('Cancelar', { selector: 'button' })).toBeDisabled()
  })
})

describe('CategoryRowActions (HU42): categoría con productos', () => {
  it('HU42-CP19: si la categoría tiene productos, la papelera queda deshabilitada y explica por qué', async () => {
    await renderAcciones(TECNOLOGIA)

    const papelera = screen.getByRole('button', { name: 'Eliminar Tecnología' })
    expect(papelera).toBeDisabled()
    expect(papelera).toHaveAttribute('title', CON_PRODUCTOS)
    expect(screen.getByRole('button', { name: 'Ver información de Tecnología' })).toBeEnabled()
  })

  it('HU42-CP19: una categoría sin productos tiene la papelera habilitada con el título Eliminar', async () => {
    await renderAcciones(MASCOTAS)

    expect(screen.getByRole('button', { name: 'Eliminar Mascotas' })).toHaveAttribute('title', 'Eliminar')
  })
})

// El backend responde con este error al DELETE de Mascotas.
function responderAlBorrarCon(status, code) {
  server.use(
    http.delete('/api/admin/categorias/:id', () =>
      code ? HttpResponse.json({ code }, { status }) : new HttpResponse(null, { status }),
    ),
  )
}

async function confirmarEliminacion(user) {
  await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
  const confirmacion = (await screen.findByText(PREGUNTA)).closest('[role="alertdialog"]')
  await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))
  await waitFor(() => expect(screen.queryByText(PREGUNTA)).not.toBeInTheDocument(), ESPERA)
}

describe('CategoryRowActions (HU42): errores al eliminar', () => {
  // Con productos o ya eliminada, la lista que ve el administrador está desfasada y se pide actualizarla.
  it.each([
    {
      caso: 'si ya tiene productos avisa de ello y pide actualizar la lista',
      status: 409,
      code: 'CATEGORY_HAS_PRODUCTS',
      texto: 'No se puede eliminar Mascotas porque tiene productos.',
      actualiza: true,
    },
    {
      caso: 'si es la categoría principal de algún vendedor avisa de ello',
      status: 409,
      code: 'CATEGORY_HAS_SELLERS',
      texto: 'No se puede eliminar Mascotas porque es la categoría principal de algún vendedor.',
      actualiza: false,
    },
    {
      caso: 'si ya no existe avisa de ello y pide actualizar la lista',
      status: 404,
      code: 'CATEGORY_NOT_FOUND',
      texto: 'Mascotas ya no existe.',
      actualiza: true,
    },
    {
      caso: 'cualquier otro error muestra un mensaje genérico',
      status: 500,
      code: null,
      texto: 'No se ha podido eliminar Mascotas. Inténtalo de nuevo.',
      actualiza: false,
    },
  ])('HU42-CP20: $caso', async ({ status, code, texto, actualiza }) => {
    const user = userEvent.setup()
    responderAlBorrarCon(status, code)
    const { onResult, onChanged } = await renderAcciones(MASCOTAS)

    await confirmarEliminacion(user)

    expect(onResult).toHaveBeenCalledWith({ intent: 'error', texto })
    expect(onChanged).toHaveBeenCalledTimes(actualiza ? 1 : 0)
  })
})
