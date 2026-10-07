import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { login, verifyAuthenticatorCode, verifyEmailCode } from '../api/auth'
import AuthProvider from '../auth/AuthProvider'
import AppRoutes from '../routes/AppRoutes'
import LocationProbe from './LocationProbe'

export function renderApp(route = '/') {
  return render(
    <FluentProvider theme={webLightTheme}>
      <AuthProvider>
        <MemoryRouter initialEntries={[route]}>
          <AppRoutes />
          <LocationProbe />
        </MemoryRouter>
      </AuthProvider>
    </FluentProvider>,
  )
}

export async function abrirSesionPorApi(email) {
  await login({ email, password: 'Mock-Clave-1!' })
  await verifyEmailCode('654321')
  await verifyAuthenticatorCode('123456')
}
