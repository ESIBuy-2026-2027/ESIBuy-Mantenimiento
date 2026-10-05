import { render } from '@testing-library/react'
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
