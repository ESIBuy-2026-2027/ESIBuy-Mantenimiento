import { FluentProvider } from '@fluentui/react-components'
import { BrowserRouter } from 'react-router-dom'
import AppRoutes from './routes/AppRoutes'
import { esiBuyTheme } from './theme/theme'

export default function App() {
  return (
    <FluentProvider theme={esiBuyTheme}>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </FluentProvider>
  )
}
