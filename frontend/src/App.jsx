import { FluentProvider } from '@fluentui/react-components'
import { RouterProvider, createBrowserRouter } from 'react-router-dom'
import AuthProvider from './auth/AuthProvider'
import AppRoutes from './routes/AppRoutes'
import { esiBuyTheme } from './theme/theme'

// Router de datos: es el que permite avisar antes de salir de una pantalla con cambios sin guardar (useBlocker, HU45).
// Una sola ruta comodín monta todas las rutas de AppRoutes, que siguen declaradas allí.
const router = createBrowserRouter([{ path: '*', element: <AppRoutes /> }])

export default function App() {
  return (
    <FluentProvider theme={esiBuyTheme}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </FluentProvider>
  )
}
