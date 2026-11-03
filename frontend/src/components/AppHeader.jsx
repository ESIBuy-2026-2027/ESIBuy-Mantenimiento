import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import {
  ArrowExit20Regular,
  Location20Regular,
  Payment20Regular,
  Person20Regular,
  PersonAdd20Regular,
  Receipt20Regular,
} from '@fluentui/react-icons'
import { Link, useNavigate } from 'react-router-dom'
import { ROLES } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import { PATHS } from '../routes/paths'
import LinkButton from './LinkButton'

const useStyles = makeStyles({
  header: {
    width: '100%',
    maxWidth: '1200px',
    margin: '0 auto',
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
  },
  logo: {
    color: tokens.colorNeutralForeground1,
    textDecorationLine: 'none',
    fontSize: tokens.fontSizeBase500,
    fontWeight: tokens.fontWeightBold,
    ':hover': { color: tokens.colorBrandForeground1 },
  },
  logoAccent: {
    color: tokens.colorBrandForeground1,
  },
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
})

function GuestActions() {
  return (
    <>
      <LinkButton to={PATHS.LOGIN} appearance="subtle">
        Iniciar sesión
      </LinkButton>
      <LinkButton to={PATHS.REGISTER} appearance="primary" icon={<PersonAdd20Regular />}>
        Registrarse
      </LinkButton>
    </>
  )
}

function UserActions({ rol, onSignOut, disabled }) {
  return (
    <>
      {rol === ROLES.ADMIN && (
        <LinkButton to={PATHS.ADMIN} appearance="subtle">
          Panel de control
        </LinkButton>
      )}
      {/* Fallos de la demo del Sprint 1: el vendedor no tenía acceso a sus productos desde el menú */}
      {rol === ROLES.VENDEDOR && (
        <LinkButton to={PATHS.SELLER_PRODUCTS} appearance="subtle">
          Mis productos
        </LinkButton>
      )}
      {/* El historial de pedidos (HU11) es solo del cliente */}
      {rol === ROLES.CLIENTE && (
        <LinkButton to={PATHS.ORDERS} appearance="subtle" icon={<Receipt20Regular />}>
          Mis pedidos
        </LinkButton>
      )}
      {/* Los métodos de pago (HU6) son solo del cliente */}
      {rol === ROLES.CLIENTE && (
        <LinkButton to={PATHS.PAYMENT_METHODS} appearance="subtle" icon={<Payment20Regular />}>
          Métodos de pago
        </LinkButton>
      )}
      {/* Las direcciones de envío (HU5) son solo del cliente */}
      {rol === ROLES.CLIENTE && (
        <LinkButton to={PATHS.ADDRESSES} appearance="subtle" icon={<Location20Regular />}>
          Direcciones
        </LinkButton>
      )}
      {/* El perfil propio (HU4) es del cliente y del vendedor */}
      {(rol === ROLES.CLIENTE || rol === ROLES.VENDEDOR) && (
        <LinkButton to={PATHS.PROFILE} appearance="subtle" icon={<Person20Regular />}>
          Mi perfil
        </LinkButton>
      )}
      <Button appearance="primary" icon={<ArrowExit20Regular />} onClick={onSignOut} disabled={disabled}>
        Cerrar sesión
      </Button>
    </>
  )
}

export default function AppHeader() {
  const styles = useStyles()
  const navigate = useNavigate()
  const { user, loading, signOut } = useAuth()
  const [signingOut, setSigningOut] = useState(false)

  // Tras cerrar sesión se vuelve al login, que avisa de que se ha cerrado (HU43).
  const handleSignOut = async () => {
    setSigningOut(true)
    await signOut()
    setSigningOut(false)
    navigate(PATHS.LOGIN)
  }

  return (
    <header className={styles.header}>
      <Link to={PATHS.HOME} className={styles.logo}>
        ESI<span className={styles.logoAccent}>Buy</span>
      </Link>
      <nav className={styles.actions} aria-label="Cuenta">
        {!loading && (user ? <UserActions rol={user.rol} onSignOut={handleSignOut} disabled={signingOut} /> : <GuestActions />)}
      </nav>
    </header>
  )
}

UserActions.propTypes = {
  rol: PropTypes.string.isRequired,
  onSignOut: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
}
