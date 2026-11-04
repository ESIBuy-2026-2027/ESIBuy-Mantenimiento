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
