import { describe, it, expect, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import SortControls from './SortControls'

const OPCIONES = [
  { value: 'name', label: 'Nombre' },
  { value: 'status', label: 'Estado' },
]

function renderOrden(props = {}) {
  const handlers = { onSortChange: vi.fn(), onOrderChange: vi.fn() }
  renderWithProviders(<SortControls options={OPCIONES} sort="name" order="asc" {...handlers} {...props} />)
  return handlers
}

describe('SortControls', () => {
  it('sortControls_muestraLasOpcionesRecibidasYLaElegida', () => {
    renderOrden()

    const criterio = screen.getByRole('combobox', { name: 'Ordenar por' })
    const opciones = within(criterio).getAllByRole('option').map((opcion) => opcion.textContent)

    expect(opciones).toEqual(['Nombre', 'Estado'])
    expect(criterio).toHaveValue('name')
  })

  it('sortControls_alElegirOtroCriterio_avisaConSuValor', async () => {
    const user = userEvent.setup()
    const { onSortChange } = renderOrden()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Ordenar por' }), 'Estado')

    expect(onSortChange).toHaveBeenCalledWith('status')
  })

  it('sortControls_alPulsarElSentido_loAlterna', async () => {
    const user = userEvent.setup()
    const { onOrderChange } = renderOrden({ order: 'desc' })

    await user.click(screen.getByRole('button', { name: 'Orden descendente' }))

    expect(onOrderChange).toHaveBeenCalledWith('asc')
  })
})
