import PropTypes from 'prop-types'
import { useEffect, useState } from 'react'
import {
  Button,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import {
  ArrowExit20Regular,
  BoxMultiple20Regular,
  ChartMultiple20Regular,
  ChatWarning20Regular,
  Crown20Regular,
  Grid20Regular,
  Heart20Regular,
  Person20Regular,
  PersonAdd20Regular,
  Receipt20Regular,
} from '@fluentui/react-icons'
import CartWidget from '../features/cart/CartWidget'
import { getProfile } from '../api/profile'
import { useApiQuery } from '../hooks/useApiQuery'
import { PREMIUM_STATUS_CHANGED } from '../features/premium/premiumEvents'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ROLES, getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import { PATHS } from '../routes/paths'
import LinkButton from './LinkButton'

// Tres tamaños. En el medio se compactan los accesos con icono; en móvil, el logotipo queda arriba y debajo aparece
// una barra de accesos que se desliza en horizontal.
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
    position: 'sticky',
    top: 0,
    zIndex: 1100,
    width: '100%',
    maxWidth: 'none',
    backgroundColor: '#FFF8F1',
    borderBottom: '1px solid #F3D2BA',
    boxShadow: '0 2px 8px rgba(124, 45, 18, 0.08)',
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
    },
    '&::-webkit-scrollbar': {
      display: 'none',
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
  navAction: {
    minHeight: '40px',
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusCircular,
    color: '#713512',
    backgroundColor: 'transparent',
    border: '1px solid transparent',
    transitionProperty: 'background-color, color, border-color',
    transitionDuration: tokens.durationFaster,
    ':hover': {
      color: '#B54708',
      backgroundColor: '#FCE8D5',
    },
    ':active': {
      color: '#963B08',
      backgroundColor: '#F8D7B8',
    },
  },
  navActionActive: {
    color: '#A83F08',
    backgroundColor: '#FFF1E3',
    borderColor: '#F4D1B3',
    ':hover': {
      color: '#963B08',
      backgroundColor: '#FCE4CC',
      borderColor: '#EFAE79',
    },
  },
  profileTrigger: {
    minHeight: '40px',
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusCircular,
    color: '#713512',
    backgroundColor: 'transparent',
    border: '1px solid transparent',
    ':hover': {
      color: '#A83F08',
      backgroundColor: '#FCE4CC',
    },
  },
  profileTriggerActive: {
    color: '#A83F08',
    backgroundColor: '#FFF1E3',
    borderColor: '#F4D1B3',
    ':hover': {
      backgroundColor: '#FCE4CC',
      borderColor: '#EFAE79',
    },
  },
  premiumPersonIcon: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  premiumCrown: {
    width: '20px',
    height: '20px',
    color: '#B54708',
    strokeWidth: '1.5px',
  },
  accountMenu: {
    minWidth: '230px',
    padding: tokens.spacingVerticalXS,
    backgroundColor: '#FFFCF8',
    border: '1px solid #F1D5BF',
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: '0 10px 28px rgba(124, 45, 18, 0.16)',
  },
  accountMenuList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '3px',
  },
  accountMenuItem: {
    minHeight: '40px',
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusMedium,
    color: '#65432F',
    ':hover': {
      color: '#9A4514',
      backgroundColor: '#FCEBDD',
    },
    ':focus': {
      backgroundColor: '#FCEBDD',
    },
  },
  signOutMenuItem: {
    marginTop: tokens.spacingVerticalXS,
    borderTop: '1px solid #F1D5BF',
    borderRadius: tokens.borderRadiusMedium,
    color: '#B42318',
    ':hover': {
      color: '#912018',
      backgroundColor: '#FEE4E2',
    },
    ':focus': {
      color: '#912018',
      backgroundColor: '#FEE4E2',
    },
    '& svg': {
      color: '#B42318',
    },
  },
})

// Un acceso de la cabecera: icono y texto. Con `compact`, en el tamaño medio solo se ve el icono y el texto pasa a
// ser su tooltip; el nombre accesible no cambia.
function NavLink({ to, icon, compact = false, children }) {
  const styles = useStyles()
  const { pathname } = useLocation()
  const active = pathname === to
  return (
    <LinkButton
      to={to}
      appearance="subtle"
      icon={icon}
      title={compact ? children : undefined}
      className={mergeClasses(styles.item, styles.navAction, active && styles.navActionActive, compact && styles.compactItem)}
      aria-current={active ? 'page' : undefined}
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

function PremiumAwarePersonIcon() {
  const { data: profile, retry } = useApiQuery(getProfile)
  const styles = useStyles()

  useEffect(() => {
    const actualizarPerfil = () => retry()
    window.addEventListener(PREMIUM_STATUS_CHANGED, actualizarPerfil)
    return () => window.removeEventListener(PREMIUM_STATUS_CHANGED, actualizarPerfil)
  }, [retry])

  return (
    <span className={styles.premiumPersonIcon}>
      <Person20Regular />
      {profile?.tipoCliente === 'PREMIUM' && <Crown20Regular className={styles.premiumCrown} aria-hidden="true" data-testid="premium-crown" />}
    </span>
  )
}

function ClientMenu({ compact, onSignOut, disabled }) {
  const styles = useStyles()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const profileSectionActive = [PATHS.PROFILE, PATHS.ORDERS, PATHS.INCIDENTS].includes(pathname)
  const enlaces = [
    [PATHS.ORDERS, <Receipt20Regular />, 'Mis pedidos'],
    [PATHS.INCIDENTS, <ChatWarning20Regular />, 'Incidencias'],
    [PATHS.PROFILE, <Person20Regular />, 'Mi perfil'],
  ]

  return (
    <Menu positioning="below-end">
      <MenuTrigger>
        <Button
          appearance="subtle"
          icon={<PremiumAwarePersonIcon />}
          title={compact ? 'Mi perfil' : undefined}
          className={mergeClasses(styles.item, styles.profileTrigger, profileSectionActive && styles.profileTriggerActive, compact && styles.compactItem)}
          aria-current={profileSectionActive ? 'page' : undefined}
        >
          <span className={compact ? styles.compactLabel : undefined}>Mi perfil</span>
        </Button>
      </MenuTrigger>
      <MenuPopover className={styles.accountMenu}>
        <MenuList className={styles.accountMenuList}>
          {enlaces.map(([to, icon, label]) => (
            <MenuItem key={to} className={styles.accountMenuItem} icon={icon} onClick={() => navigate(to)}>
              {label}
            </MenuItem>
          ))}
          <MenuItem className={mergeClasses(styles.accountMenuItem, styles.signOutMenuItem)} icon={<ArrowExit20Regular />} disabled={disabled} onClick={onSignOut}>
            Cerrar sesión
          </MenuItem>
        </MenuList>
      </MenuPopover>
    </Menu>
  )
}

ClientMenu.propTypes = {
  compact: PropTypes.bool.isRequired,
  onSignOut: PropTypes.func.isRequired,
  disabled: PropTypes.bool.isRequired,
}

function UserActions({ rol, onSignOut, disabled }) {
  const esCliente = rol === ROLES.CLIENTE
  // El cliente conserva Favoritos, Carrito y Mi perfil en la cabecera.
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
        <>
          <NavLink to={PATHS.SELLER_PRODUCTS} icon={<BoxMultiple20Regular />}>
            Mis productos
          </NavLink>
          <NavLink to={PATHS.SELLER_ORDERS} icon={<Receipt20Regular />}>
            Mis pedidos
          </NavLink>
          <NavLink to={PATHS.SELLER_STATISTICS} icon={<ChartMultiple20Regular />}>
            Mis estadísticas
          </NavLink>
        </>
      )}
      {/* La lista de deseos (HU14) permanece visible en la cabecera */}
      {esCliente && (
        <NavLink to={PATHS.WISHLIST} icon={<Heart20Regular />} compact={compact}>
          Favoritos
        </NavLink>
      )}
      {/* Las incidencias (HU44) son del cliente y del vendedor */}
      {rol === ROLES.VENDEDOR && (
        <NavLink to={PATHS.INCIDENTS} icon={<ChatWarning20Regular />} compact={compact}>
          Incidencias
        </NavLink>
      )}
      {/* El perfil propio (HU4) es del cliente y del vendedor */}
      {rol === ROLES.VENDEDOR && (
        <NavLink to={PATHS.PROFILE} icon={<Person20Regular />} compact={compact}>
          Mi perfil
        </NavLink>
      )}
      {esCliente && <CartWidget />}
      {esCliente && <ClientMenu compact={compact} onSignOut={onSignOut} disabled={disabled} />}
      {!esCliente && (
        <Button appearance="primary" icon={<ArrowExit20Regular />} onClick={onSignOut} disabled={disabled}>
          Cerrar sesión
        </Button>
      )}
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