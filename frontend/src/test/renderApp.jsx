import { render } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { login, verifyAuthenticatorCode, verifyEmailCode } from '../api/auth'
import AuthProvider from '../auth/AuthProvider'
import AppRoutes from '../routes/AppRoutes'
import LocationProbe from './LocationProbe'

// Con un router de datos, como la aplicación (HU45): hace falta para el aviso de cambios sin guardar (useBlocker)
export function renderApp(route = '/') {
  const router = createMemoryRouter(
    [
      {
        path: '*',
        element: (
          <>
            <AppRoutes />
            <LocationProbe />
          </>
        ),
      },
    ],
    { initialEntries: [route] },
  )
  return render(
    <FluentProvider theme={webLightTheme}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </FluentProvider>,
  )
}

export async function abrirSesionPorApi(email) {
  await login({ email, password: 'Mock-Clave-1!' })
  await verifyEmailCode('654321')
  await verifyAuthenticatorCode('123456')
}
