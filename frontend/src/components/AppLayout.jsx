import { makeStyles, tokens } from '@fluentui/react-components'
import { Outlet } from 'react-router-dom'
import CartProvider from '../features/cart/CartProvider'
import CartWidget from '../features/cart/CartWidget'
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

  return (
    <CartProvider>
      <div className={styles.page}>
        <AppHeader />
        <Outlet />
        <CartWidget />
      </div>
    </CartProvider>
  )
}
