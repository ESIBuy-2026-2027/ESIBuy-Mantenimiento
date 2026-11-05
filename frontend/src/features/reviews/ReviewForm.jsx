import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  Field,
  MessageBar,
  MessageBarBody,
  Rating,
  Subtitle2,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { ApiError } from '../../api/client'
import { createProductReview } from '../../api/products'

const MAXIMO_COMENTARIO = 500

// Errores que el backend explica con su code (HU13); el resto, un mensaje general
const MENSAJES_POR_CODIGO = {
  PRODUCT_NOT_PURCHASED: 'Solo puedes valorar productos que has comprado.',
  REVIEW_ALREADY_EXISTS: 'Ya has valorado este producto.',
}
const MENSAJE_DATOS_NO_VALIDOS = 'Revisa la puntuación y el comentario.'
const MENSAJE_GENERAL = 'No se ha podido enviar tu valoración. Inténtalo de nuevo.'

function mensajeDeError(error) {
  if (!(error instanceof ApiError)) return MENSAJE_GENERAL
  if (MENSAJES_POR_CODIGO[error.code]) return MENSAJES_POR_CODIGO[error.code]
  return error.status === 400 ? MENSAJE_DATOS_NO_VALIDOS : MENSAJE_GENERAL
}

const etiquetaDeEstrellas = (estrellas) => (estrellas === 1 ? '1 estrella' : `${estrellas} estrellas`)

const useStyles = makeStyles({
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
  },
  title: {
    margin: 0,
  },
  submit: {
    alignSelf: 'flex-start',
  },
})

// Formulario para que el cliente valore el producto (HU13, T367): de 1 a 5 estrellas y un comentario opcional. Si el
// cliente no lo ha comprado o ya lo valoró lo dice el backend al enviar. Tras valorar se da las gracias y se avisa
// (onCreated) para recargar las valoraciones.
export default function ReviewForm({ productId, onCreated }) {
  const styles = useStyles()
  const [puntuacion, setPuntuacion] = useState(0)
  const [comentario, setComentario] = useState('')
  const [faltaPuntuacion, setFaltaPuntuacion] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState(null)
  const [enviada, setEnviada] = useState(false)

  if (enviada) {
    return (
      <MessageBar intent="success">
        <MessageBarBody>Gracias por tu valoración.</MessageBarBody>
      </MessageBar>
    )
  }

  async function enviar(event) {
    event.preventDefault()
    if (puntuacion === 0) {
      setFaltaPuntuacion(true)
      return
    }
    setEnviando(true)
    setError(null)
    try {
      await createProductReview(productId, { puntuacion, comentario: comentario.trim() })
      setEnviada(true)
      onCreated()
    } catch (fallo) {
      setError(mensajeDeError(fallo))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={styles.form} aria-labelledby="valorar-producto" onSubmit={enviar} noValidate>
      <Subtitle2 as="h3" id="valorar-producto" className={styles.title}>
        Valora este producto
      </Subtitle2>
      <Field
        label="Puntuación"
        required
        validationState={faltaPuntuacion ? 'error' : 'none'}
        validationMessage={faltaPuntuacion ? 'Elige una puntuación de 1 a 5 estrellas.' : undefined}
      >
        <Rating
          value={puntuacion}
          color="marigold"
          itemLabel={etiquetaDeEstrellas}
          onChange={(_event, data) => {
            setPuntuacion(data.value)
            setFaltaPuntuacion(false)
          }}
        />
      </Field>
      <Field label="Comentario (opcional)" hint={`${comentario.length}/${MAXIMO_COMENTARIO} caracteres`}>
        <Textarea
          value={comentario}
          maxLength={MAXIMO_COMENTARIO}
          resize="vertical"
          onChange={(_event, data) => setComentario(data.value)}
        />
      </Field>
      {error && (
        <MessageBar intent="error">
          <MessageBarBody>{error}</MessageBarBody>
        </MessageBar>
      )}
      <Button type="submit" appearance="primary" disabled={enviando} className={styles.submit}>
        {enviando ? 'Enviando…' : 'Enviar valoración'}
      </Button>
    </form>
  )
}

ReviewForm.propTypes = {
  productId: PropTypes.string.isRequired,
  onCreated: PropTypes.func.isRequired,
}
