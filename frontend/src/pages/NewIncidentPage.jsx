import { useState } from 'react'
import { Text, Title1, makeStyles, mergeClasses } from '@fluentui/react-components'
import { useNavigate } from 'react-router-dom'
import { createIncident } from '../api/incidents'
import { usePageStyles } from '../components/pageStyles'
import IncidentForm from '../features/incidents/IncidentForm'
import { PATHS } from '../routes/paths'

const NOTICE = 'Incidencia enviada. Un administrador la revisará y te responderá desde aquí.'
const ERROR_VALIDATION = 'No se ha podido crear la incidencia. Revisa el asunto y la descripción e inténtalo de nuevo.'
const ERROR_OTHER = 'No se ha podido crear la incidencia. Comprueba tu conexión e inténtalo de nuevo.'
const BAD_REQUEST = 400

const useStyles = makeStyles({
  // Columna más estrecha que la de los listados; sigue centrada en la pantalla (margin auto).
  page: {
    maxWidth: '720px',
  },
})

// Alta de una incidencia del cliente o del vendedor en sesión (HU44). Al crearla vuelve a la lista con un aviso.
export default function NewIncidentPage() {
  const styles = usePageStyles()
  const narrow = useStyles()
  const navigate = useNavigate()
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState(null)

  const handleSubmit = async (payload) => {
    setSaving(true)
    setSaveError(null)
    try {
      await createIncident(payload)
      navigate(PATHS.INCIDENTS, { state: { notice: NOTICE } })
    } catch (error) {
      // El formulario sigue montado, así que lo escrito no se pierde. El backend responde 400 con un mensaje genérico.
      setSaveError(error?.status === BAD_REQUEST ? ERROR_VALIDATION : ERROR_OTHER)
      setSaving(false)
    }
  }

  return (
    <main className={mergeClasses(styles.page, narrow.page)}>
      <div className={styles.titleBlock}>
        <Title1 as="h1" className={styles.title}>
          Nueva incidencia
        </Title1>
        <Text className={styles.subtitle}>Los campos marcados con * son obligatorios.</Text>
      </div>
      <IncidentForm saving={saving} saveError={saveError} onSubmit={handleSubmit} />
    </main>
  )
}
