import { describe, it, expect, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import CategoryDetailDialog from './CategoryDetailDialog'

const DESCRIPCION_LARGA =
  'Muebles, decoración y menaje para dar vida a cualquier estancia. Incluye iluminación, textil del hogar y almacenamiento.'
const HOGAR = { id: 'c-1', nombre: 'Hogar', descripcion: DESCRIPCION_LARGA, estado: 'ACTIVA', numeroProductos: 14 }
const MODA = { id: 'c-2', nombre: 'Moda', descripcion: '', estado: 'INACTIVA', numeroProductos: 0 }

async function abrirFicha(category) {
  const onClose = vi.fn()
  renderWithProviders(<CategoryDetailDialog category={category} onClose={onClose} />)
  const dialogo = await screen.findByRole('dialog', { name: /información de la categoría/i })
  return { dialogo, onClose }
}

describe('CategoryDetailDialog', () => {
  it('HU39-CP11: muestra el nombre, el estado, el número de productos y la descripción completa', async () => {
    const { dialogo } = await abrirFicha(HOGAR)

    expect(within(dialogo).getByLabelText('Nombre')).toHaveValue('Hogar')
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activa')
    expect(within(dialogo).getByLabelText('Productos')).toHaveValue('14')
    expect(within(dialogo).getByLabelText('Descripción')).toHaveValue(DESCRIPCION_LARGA)
  })

  it('HU39-CP11: una categoría inactiva sin descripción ni productos muestra Inactiva, 0 y un guion', async () => {
    const { dialogo } = await abrirFicha(MODA)

    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Inactiva')
    expect(within(dialogo).getByLabelText('Productos')).toHaveValue('0')
    expect(within(dialogo).getByLabelText('Descripción')).toHaveValue('—')
  })

  it('HU39-CP11: todos los campos son de solo lectura y la ficha avisa de ello', async () => {
    const { dialogo } = await abrirFicha(HOGAR)

    for (const etiqueta of ['Nombre', 'Estado', 'Productos', 'Descripción']) {
      expect(within(dialogo).getByLabelText(etiqueta)).toHaveAttribute('readonly')
    }
    expect(within(dialogo).getByText('Modo solo lectura')).toBeInTheDocument()
  })

  it('HU39-CP11: la ficha no ofrece editar, crear ni eliminar', async () => {
    const { dialogo } = await abrirFicha(HOGAR)

    expect(within(dialogo).queryByRole('button', { name: /editar|crear|eliminar|guardar/i })).not.toBeInTheDocument()
  })

  it('HU39-CP11: se cierra con la X', async () => {
    const user = userEvent.setup()
    const { dialogo, onClose } = await abrirFicha(HOGAR)

    await user.click(within(dialogo).getByRole('button', { name: 'Cerrar' }))

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('HU39-CP11: se cierra con Escape', async () => {
    const user = userEvent.setup()
    const { onClose } = await abrirFicha(HOGAR)

    await user.keyboard('{Escape}')

    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
