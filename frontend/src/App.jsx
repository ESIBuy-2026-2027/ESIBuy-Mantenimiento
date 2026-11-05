import { FluentProvider } from '@fluentui/react-components'
import { BrowserRouter } from 'react-router-dom'
import AuthProvider from './auth/AuthProvider'
import AppRoutes from './routes/AppRoutes'
import { esiBuyTheme } from './theme/theme'

export default function App() {
  return (
    <FluentProvider theme={esiBuyTheme}>
      <AuthProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </AuthProvider>
    </FluentProvider>
  )
}
