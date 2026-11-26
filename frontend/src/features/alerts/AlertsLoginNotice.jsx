import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Popover, PopoverSurface, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Alert20Filled, Dismiss16Regular } from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { textoSinLeer } from './alertsText'
import { useAlerts } from './useAlerts'
import { useAvisoTrasIniciarSesion } from './useAvisoTrasIniciarSesion'

// Colores de la cabecera, para que la notificación parezca salir de ella
const FONDO = '#FFFCF8'
const BORDE = '#F1D5BF'

const useStyles = makeStyles({
  // La flecha de Fluent hereda el fondo; por encima de la cabecera (sticky, z-index 1100) y por debajo del carrito
  surface: {
    zIndex: 1150,
    width: 'min(340px, calc(100vw - 24px))',
    boxSizing: 'border-box',
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    backgroundColor: FONDO,
    border: `1px solid ${BORDE}`,
    borderRadius: '18px',
    boxShadow: '0 14px 36px rgba(124, 45, 18, 0.18), 0 2px 6px rgba(124, 45, 18, 0.08)',
  },
  notification: {
    display: 'grid',
    gridTemplateColumns: 'auto 1fr auto',
    columnGap: tokens.spacingHorizontalM,
    alignItems: 'start',
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

// Notificación de alertas sin leer justo al iniciar sesión (AB#529), como las de un móvil. Desde AB#531 sale de la
// campanita (`target`), con una flecha que la señala en cualquier tamaño de pantalla, mientras la campanita se
// balancea. Va en un portal: en el móvil la barra de accesos se desliza y la recortaría. Se cierra con la ✕ o pulsando
// fuera; después sigue avisando el número de la campanita.
export default function AlertsLoginNotice({ target }) {
  const styles = useStyles()
  const { sinLeer } = useAlerts()
  const toca = useAvisoTrasIniciarSesion()
  const [cerrado, setCerrado] = useState(false)

  if (!toca || cerrado || !target) return null

  return (
    <Popover
      open
      withArrow
      positioning={{ target, position: 'below', align: 'end' }}
      onOpenChange={(_, data) => {
        if (!data.open) setCerrado(true)
      }}
    >
      <PopoverSurface className={styles.surface}>
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
      </PopoverSurface>
    </Popover>
  )
}

AlertsLoginNotice.propTypes = {
  // El botón de la campanita, del que sale la notificación
  target: PropTypes.instanceOf(Element),
}
