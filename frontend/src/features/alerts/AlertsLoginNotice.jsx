import { useState } from 'react'
import { Button, MessageBar, MessageBarActions, MessageBarBody, makeStyles, tokens } from '@fluentui/react-components'
import { Dismiss20Regular } from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { textoSinLeer } from './alertsText'
import { useAlerts } from './useAlerts'

const useStyles = makeStyles({
  // Mismo ancho que el contenido de las páginas
  notice: {
    width: '100%',
    maxWidth: '1000px',
    margin: '0 auto',
    boxSizing: 'border-box',
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL} 0`,
  },
})

// Aviso de alertas sin leer justo al iniciar sesión (AB#529): el login lleva al cliente a su inicio con
// `state.trasIniciarSesion`, que se pierde al navegar, así que el aviso solo sale en esa primera página. Después, el
// indicador del botón de la cuenta sigue avisando.
export default function AlertsLoginNotice() {
  const styles = useStyles()
  const { state } = useLocation()
  const { sinLeer } = useAlerts()
  const [cerrado, setCerrado] = useState(false)

  if (!state?.trasIniciarSesion || cerrado || sinLeer === 0) return null

  return (
    <div className={styles.notice}>
      <MessageBar intent="info">
        <MessageBarBody>{`${textoSinLeer(sinLeer)} de tus productos favoritos.`}</MessageBarBody>
        <MessageBarActions
          containerAction={
            <Button
              appearance="transparent"
              icon={<Dismiss20Regular />}
              aria-label="Cerrar el aviso de alertas"
              onClick={() => setCerrado(true)}
            />
          }
        >
          <LinkButton to={PATHS.ALERTS}>Ver alertas</LinkButton>
        </MessageBarActions>
      </MessageBar>
    </div>
  )
}
