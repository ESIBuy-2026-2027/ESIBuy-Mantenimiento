import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../../test/utils'
import ReadOnlyFields from './ReadOnlyFields'

const CAMPOS = [
  ['Nombre', 'Lucía'],
  ['Email', 'lucia.torres@esibuy.test'],
]

describe('ReadOnlyFields', () => {
  it('readOnlyFields_muestraCadaCampoConSuEtiquetaYSuValor', () => {
    renderWithProviders(<ReadOnlyFields fields={CAMPOS} />)

    expect(screen.getByLabelText('Nombre')).toHaveValue('Lucía')
    expect(screen.getByLabelText('Email')).toHaveValue('lucia.torres@esibuy.test')
  })

  it('readOnlyFields_losCamposSonDeSoloLecturaYNoCambianAlEscribir', async () => {
    const user = userEvent.setup()
    renderWithProviders(<ReadOnlyFields fields={CAMPOS} />)
    const nombre = screen.getByLabelText('Nombre')

    await user.type(nombre, 'cambio')

    expect(nombre).toHaveAttribute('readonly')
    expect(nombre).toHaveValue('Lucía')
  })
})
