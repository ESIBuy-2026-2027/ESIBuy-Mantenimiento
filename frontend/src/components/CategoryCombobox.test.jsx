import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Field } from '@fluentui/react-components'
import CategoryCombobox from './CategoryCombobox'
import { renderWithProviders } from '../test/utils'

const CATEGORIAS = [
  { id: 'cat-1', nombre: 'Tecnología' },
  { id: 'cat-2', nombre: 'Hogar' },
  { id: 'cat-3', nombre: 'Jardín' },
]

function renderizar(props = {}) {
  const onChange = vi.fn()
  renderWithProviders(
    <Field label="Categoría">
      <CategoryCombobox value="" categories={CATEGORIAS} onChange={onChange} {...props} />
    </Field>,
  )
  return { onChange, combobox: screen.getByRole('combobox', { name: /categoría/i }) }
}

const nombresDeLasOpciones = () => screen.getAllByRole('option').map((opcion) => opcion.textContent)

describe('CategoryCombobox (HU31)', () => {
  it('HU31-CP26: al abrirlo muestra todas las categorías en el orden en que llegan', async () => {
    const user = userEvent.setup()
    const { combobox } = renderizar()

    await user.click(combobox)

    expect(nombresDeLasOpciones()).toEqual(['Tecnología', 'Hogar', 'Jardín'])
  })

  it('HU31-CP26: al escribir filtra las categorías sin distinguir mayúsculas ni tildes', async () => {
    const user = userEvent.setup()
    const { combobox } = renderizar()

    await user.type(combobox, 'JARDIN')

    expect(nombresDeLasOpciones()).toEqual(['Jardín'])
  })

  it('HU31-CP26: si ninguna categoría coincide lo indica', async () => {
    const user = userEvent.setup()
    const { combobox } = renderizar()

    await user.type(combobox, 'zzz')

    expect(screen.getByText(/no hay categorías que coincidan/i)).toBeInTheDocument()
  })

  it('HU31-CP26: al elegir una categoría avisa con su identificador', async () => {
    const user = userEvent.setup()
    const { combobox, onChange } = renderizar()

    await user.click(combobox)
    await user.click(screen.getByRole('option', { name: 'Hogar' }))

    expect(onChange).toHaveBeenCalledWith('cat-2')
  })

  it('HU31-CP26: muestra el nombre de la categoría elegida', () => {
    const { combobox } = renderizar({ value: 'cat-1' })

    expect(combobox).toHaveValue('Tecnología')
  })

  it('HU31-CP26: con una categoría elegida, al abrirlo sigue ofreciendo todas', async () => {
    const user = userEvent.setup()
    const { combobox } = renderizar({ value: 'cat-1' })

    await user.click(combobox)

    expect(nombresDeLasOpciones()).toEqual(['Tecnología', 'Hogar', 'Jardín'])
  })

  it('HU31-CP26: si se escribe y se sale sin elegir, vuelve a mostrar la categoría elegida', async () => {
    const user = userEvent.setup()
    const { combobox, onChange } = renderizar({ value: 'cat-1' })

    await user.clear(combobox)
    await user.type(combobox, 'Hog')
    await user.keyboard('{Escape}')
    await user.tab()

    expect(combobox).toHaveValue('Tecnología')
    expect(onChange).not.toHaveBeenCalled()
  })

  it('HU31-CP26: con una opción para todas, la ofrece la primera y elegirla avisa con un valor vacío', async () => {
    const user = userEvent.setup()
    const { combobox, onChange } = renderizar({ value: 'cat-2', allLabel: 'Todas las categorías' })

    await user.click(combobox)
    expect(nombresDeLasOpciones()).toEqual(['Todas las categorías', 'Tecnología', 'Hogar', 'Jardín'])
    await user.click(screen.getByRole('option', { name: 'Todas las categorías' }))

    expect(onChange).toHaveBeenCalledWith('')
  })

  it('HU31-CP26: con una opción para todas y sin categoría elegida, muestra esa opción', () => {
    const { combobox } = renderizar({ allLabel: 'Todas las categorías' })

    expect(combobox).toHaveValue('Todas las categorías')
  })
})
