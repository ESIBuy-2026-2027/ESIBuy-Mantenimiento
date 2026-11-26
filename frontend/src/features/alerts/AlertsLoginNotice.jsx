import { useState } from 'react'
import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Alert20Filled, Dismiss16Regular } from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { textoSinLeer } from './alertsText'
import { useAlerts } from './useAlerts'

// Colores de la cabecera y del menú de la cuenta, para que la notificación parezca salir de él
const FONDO = '#FFFCF8'
const BORDE = '#F1D5BF'

const useStyles = makeStyles({
  // Va dentro de la cabecera (sticky), no de la barra de accesos: en el móvil esa barra se desliza y la recortaría
  notification: {
    position: 'absolute',
    top: 'calc(100% + 10px)',
    right: tokens.spacingHorizontalL,
    zIndex: 1,
    width: 'min(340px, calc(100vw - 24px))',
    boxSizing: 'border-box',
    display: 'grid',
    gridTemplateColumns: 'auto 1fr auto',
    columnGap: tokens.spacingHorizontalM,
    alignItems: 'start',
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    backgroundColor: FONDO,
    border: `1px solid ${BORDE}`,
    borderRadius: '18px',
    boxShadow: '0 14px 36px rgba(124, 45, 18, 0.18), 0 2px 6px rgba(124, 45, 18, 0.08)',
    animationName: {
      from: { opacity: 0, transform: 'translateY(-10px) scale(0.96)' },
      to: { opacity: 1, transform: 'translateY(0) scale(1)' },
    },
    animationDuration: '320ms',
    animationTimingFunction: 'cubic-bezier(0.2, 0.9, 0.3, 1.2)',
    transformOrigin: 'top right',
    '@media (prefers-reduced-motion: reduce)': {
      animationDuration: '0.01ms',
    },
    // La flecha que señala el botón de la cuenta, el último de la cabecera
    '::before': {
      content: '""',
      position: 'absolute',
      top: '-7px',
      right: '28px',
      width: '12px',
      height: '12px',
      backgroundColor: FONDO,
      borderTop: `1px solid ${BORDE}`,
      borderLeft: `1px solid ${BORDE}`,
      transform: 'rotate(45deg)',
    },
  },
  icon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '36px',
    height: '36px',
    borderRadius: '12px',
    color: '#FFFFFF',
    backgroundImage: 'linear-gradient(135deg, #F79009 0%, #B54708 100%)',
    boxShadow: '0 4px 10px rgba(181, 71, 8, 0.3)',
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px',
    minWidth: 0,
  },
  app: {
    color: tokens.colorNeutralForeground3,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  title: {
    color: '#65432F',
  },
  text: {
    color: tokens.colorNeutralForeground2,
  },
  action: {
    alignSelf: 'flex-start',
    marginTop: tokens.spacingVerticalXS,
    marginLeft: `calc(-1 * ${tokens.spacingHorizontalS})`,
    color: '#B54708',
    fontWeight: tokens.fontWeightSemibold,
  },
  close: {
    minWidth: 'auto',
    color: tokens.colorNeutralForeground3,
  },
})

// Notificación de alertas sin leer justo al iniciar sesión (AB#529), como las de un móvil: sale del botón de la
// cuenta. El login lleva al cliente a su inicio con `state.trasIniciarSesion`, que se pierde al navegar, así que solo
// aparece en esa primera página; después sigue avisando el punto del botón de la cuenta.
export default function AlertsLoginNotice() {
  const styles = useStyles()
  const { state } = useLocation()
  const { sinLeer } = useAlerts()
  const [cerrado, setCerrado] = useState(false)

  if (!state?.trasIniciarSesion || cerrado || sinLeer === 0) return null

  return (
    <div role="status" aria-label="Notificación de alertas" className={styles.notification}>
      <span className={styles.icon} aria-hidden="true">
        <Alert20Filled />
      </span>
      <div className={styles.body}>
        <Text size={100} weight="semibold" className={styles.app}>
          ESIBuy · ahora
        </Text>
        <Text weight="semibold" className={styles.title}>
          Novedades en tus favoritos
        </Text>
        <Text size={200} className={styles.text}>{`${textoSinLeer(sinLeer)} de tus productos favoritos.`}</Text>
        <LinkButton to={PATHS.ALERTS} appearance="transparent" size="small" className={styles.action}>
          Ver alertas
        </LinkButton>
      </div>
      <Button
        appearance="transparent"
        size="small"
        icon={<Dismiss16Regular />}
        aria-label="Cerrar el aviso de alertas"
        className={styles.close}
        onClick={() => setCerrado(true)}
      />
    </div>
  )
}
