import { render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'

export function renderWithProviders(ui, { route = '/' } = {}) {
  return render(
    <FluentProvider theme={webLightTheme}>
      <MemoryRouter initialEntries={[route]}>
        {ui}
      </MemoryRouter>
    </FluentProvider>
  )
}

// Elige una categoría en el desplegable con búsqueda (CategoryCombobox). Las opciones se abren en un portal,
// así que se buscan en toda la pantalla aunque el desplegable esté dentro de un diálogo.
export async function elegirCategoria(user, nombre, { campo = /categoría/i, dentro } = {}) {
  const zona = dentro ? within(dentro) : screen
  await user.click(await zona.findByRole('combobox', { name: campo }))
  await user.click(await screen.findByRole('option', { name: nombre }))
}
