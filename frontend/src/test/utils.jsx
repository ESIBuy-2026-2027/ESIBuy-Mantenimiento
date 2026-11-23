import { render, screen, waitFor, within } from '@testing-library/react'
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

// Elige una categoría en el desplegable (CategoryCombobox). Las opciones se abren en un portal,
// así que se buscan en toda la pantalla aunque el desplegable esté dentro de un diálogo.
export async function elegirCategoria(user, nombre, { campo = /categoría/i, dentro } = {}) {
  const zona = dentro ? within(dentro) : screen
  // El listado puede terminar de cargar después de que la página ya haya mostrado su cabecera.
  // Esperamos a que aparezca el filtro, tanto si Fluent UI lo expone como combobox como si es botón.
  const selector = await waitFor(() => {
    const combobox = zona.queryByRole('combobox', { name: campo })
    const dropdown = zona.queryByRole('button', { name: campo })
    if (!combobox && !dropdown) throw new Error('No se ha renderizado el selector de categoría')
    return combobox ?? dropdown
  })
  await user.click(selector)
  await user.click(await screen.findByRole('option', { name: nombre }))
}
