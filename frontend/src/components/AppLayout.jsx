import { makeStyles, tokens } from '@fluentui/react-components'
import { Outlet, useLocation } from 'react-router-dom'
import CartProvider from '../features/cart/CartProvider'
import { PATHS } from '../routes/paths'
import AppHeader from './AppHeader'

const useStyles = makeStyles({
  page: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    backgroundImage: `linear-gradient(180deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 55%)`,
  },
})

export default function AppLayout() {
  const styles = useStyles()
  const { pathname } = useLocation()
  const isRegisterPage = pathname === PATHS.REGISTER
  const isHomePage = pathname === PATHS.HOME

  return (
    <CartProvider>
      <div className={styles.page}>
        {!isRegisterPage && !isHomePage && <AppHeader />}
        <Outlet />
      </div>
    </CartProvider>
  )
}
