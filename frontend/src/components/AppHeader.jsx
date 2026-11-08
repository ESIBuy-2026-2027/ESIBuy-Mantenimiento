import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import {
  ArrowExit20Regular,
  BoxMultiple20Regular,
  ChatWarning20Regular,
  Diamond20Regular,
  Grid20Regular,
  Heart20Regular,
  Location20Regular,
  Payment20Regular,
  Person20Regular,
  PersonAdd20Regular,
  Receipt20Regular,
} from '@fluentui/react-icons'
import { Link, useNavigate } from 'react-router-dom'
import { ROLES, getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import { PATHS } from '../routes/paths'
import LinkButton from './LinkButton'

// Tres tamaños. Ancho: iconos con texto. Medio: con muchos accesos (el cliente tiene nueve) solo se ven los iconos;
// el texto sigue ahí para los lectores de pantalla y como tooltip. Móvil: el logotipo arriba y, debajo, una barra de
// accesos con su texto que se desliza en horizontal. Todo es CSS: los enlaces están siempre en la página.
const MOVIL = '@media (max-width: 767px)'
const MEDIO = '@media (min-width: 768px) and (max-width: 1279px)'

// Oculto a la vista, pero no a los lectores de pantalla
const visuallyHidden = {
  position: 'absolute',
  width: '1px',
  height: '1px',
  margin: '-1px',
  padding: 0,
  overflow: 'hidden',
  clip: 'rect(0 0 0 0)',
  whiteSpace: 'nowrap',
  border: 0,
}

const useStyles = makeStyles({
  header: {
    width: '100%',
    maxWidth: '1280px',
    margin: '0 auto',
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
    [MOVIL]: {
      flexWrap: 'wrap',
      rowGap: tokens.spacingVerticalS,
      paddingTop: tokens.spacingVerticalM,
      paddingBottom: tokens.spacingVerticalS,
      paddingLeft: tokens.spacingHorizontalM,
      paddingRight: tokens.spacingHorizontalM,
    },
  },
  logo: {
    flexShrink: 0,
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
    minWidth: 0,
    [MEDIO]: { gap: tokens.spacingHorizontalXS },
  },
  // Con sesión, en el móvil la barra ocupa una fila propia bajo el logotipo y se desliza
  userActions: {
    [MOVIL]: {
      order: 1,
      flexBasis: '100%',
      overflowX: 'auto',
      scrollbarWidth: 'none',
      paddingBottom: tokens.spacingVerticalXXS,
      maskImage: 'linear-gradient(to right, black 85%, transparent)',
      WebkitMaskImage: 'linear-gradient(to right, black 85%, transparent)',
      '::-webkit-scrollbar': { display: 'none' },
    },
  },
  item: {
    flexShrink: 0,
    whiteSpace: 'nowrap',
  },
  // Solo el texto de los accesos con icono se esconde en el tamaño medio
  compactLabel: {
    [MEDIO]: visuallyHidden,
  },
  compactItem: {
    [MEDIO]: { minWidth: 'auto', paddingLeft: tokens.spacingHorizontalS, paddingRight: tokens.spacingHorizontalS },
  },
})

// Un acceso de la cabecera: icono y texto. Con `compact`, en el tamaño medio solo se ve el icono y el texto pasa a
// ser su tooltip; el nombre accesible no cambia.
function NavLink({ to, icon, compact = false, children }) {
  const styles = useStyles()
  return (
    <LinkButton
      to={to}
      appearance="subtle"
      icon={icon}
      title={compact ? children : undefined}
      className={mergeClasses(styles.item, compact && styles.compactItem)}
    >
      <span className={compact ? styles.compactLabel : undefined}>{children}</span>
    </LinkButton>
  )
}

NavLink.propTypes = {
  to: PropTypes.string.isRequired,
  icon: PropTypes.element,
  compact: PropTypes.bool,
  children: PropTypes.string.isRequired,
}

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
  const styles = useStyles()
  const esCliente = rol === ROLES.CLIENTE
  // El cliente es quien tiene muchos accesos: solo los suyos se compactan en el tamaño medio
  const compact = esCliente

  return (
    <>
      {rol === ROLES.ADMIN && (
        <NavLink to={PATHS.ADMIN} icon={<Grid20Regular />}>
          Panel de control
        </NavLink>
      )}
      {/* Fallos de la demo del Sprint 1: el vendedor no tenía acceso a sus productos desde el menú */}
      {rol === ROLES.VENDEDOR && (
        <NavLink to={PATHS.SELLER_PRODUCTS} icon={<BoxMultiple20Regular />}>
          Mis productos
        </NavLink>
      )}
      {/* Las ventajas de ESIBuy Premium (HU45) son solo del cliente */}
      {esCliente && (
        <NavLink to={PATHS.PREMIUM} icon={<Diamond20Regular />} compact={compact}>
          Premium
        </NavLink>
      )}
      {/* El historial de pedidos (HU11) es solo del cliente */}
      {esCliente && (
        <NavLink to={PATHS.ORDERS} icon={<Receipt20Regular />} compact={compact}>
          Mis pedidos
        </NavLink>
      )}
      {/* La lista de deseos (HU14) es solo del cliente */}
      {esCliente && (
        <NavLink to={PATHS.WISHLIST} icon={<Heart20Regular />} compact={compact}>
          Favoritos
        </NavLink>
      )}
      {/* Los métodos de pago (HU6) son solo del cliente */}
      {esCliente && (
        <NavLink to={PATHS.PAYMENT_METHODS} icon={<Payment20Regular />} compact={compact}>
          Métodos de pago
        </NavLink>
      )}
      {/* Las direcciones de envío (HU5) son solo del cliente */}
      {esCliente && (
        <NavLink to={PATHS.ADDRESSES} icon={<Location20Regular />} compact={compact}>
          Direcciones
        </NavLink>
      )}
      {/* Las incidencias (HU44) son del cliente y del vendedor */}
      {(esCliente || rol === ROLES.VENDEDOR) && (
        <NavLink to={PATHS.INCIDENTS} icon={<ChatWarning20Regular />} compact={compact}>
          Incidencias
        </NavLink>
      )}
      {/* El perfil propio (HU4) es del cliente y del vendedor */}
      {(esCliente || rol === ROLES.VENDEDOR) && (
        <NavLink to={PATHS.PROFILE} icon={<Person20Regular />} compact={compact}>
          Mi perfil
        </NavLink>
      )}
      <Button
        appearance="primary"
        icon={<ArrowExit20Regular />}
        onClick={onSignOut}
        disabled={disabled}
        title={compact ? 'Cerrar sesión' : undefined}
        className={mergeClasses(styles.item, compact && styles.compactItem)}
      >
        <span className={compact ? styles.compactLabel : undefined}>Cerrar sesión</span>
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
      {/* Con sesión, el logotipo lleva al inicio del rol (el mismo que tras el login); sin sesión, a la home (HU30) */}
      <Link to={user ? getHomePath(user.rol) : PATHS.HOME} className={styles.logo}>
        ESI<span className={styles.logoAccent}>Buy</span>
      </Link>
      <nav className={mergeClasses(styles.actions, user && styles.userActions)} aria-label="Cuenta">
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
