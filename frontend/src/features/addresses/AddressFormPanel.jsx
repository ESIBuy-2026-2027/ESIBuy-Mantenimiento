import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Card,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Subtitle1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { addAddress, updateAddress } from '../../api/addresses'
import { direccionPropType } from './direccionPropType'

const CAMPOS_VACIOS = {
  destinatario: '',
  calle: '',
  detalles: '',
  codigoPostal: '',
  ciudad: '',
  provincia: '',
  telefono: '',
}

// Los mismos límites que el backend, para avisar antes de enviar
const LONGITUD_MAXIMA = 100
const LONGITUD_MAXIMA_CALLE = 150
const FORMATO_CODIGO_POSTAL = /^\d{5}$/
const FORMATO_TELEFONO = /^\d{9}$/

// Texto obligatorio con longitud máxima: devuelve el error o nada
function errorDeTexto(valor, longitudMaxima, vacio, largo) {
  if (!valor) return vacio
  if (valor.length > longitudMaxima) return largo
  return undefined
}

function erroresDe(campos) {
  const errores = {
    destinatario: errorDeTexto(
      campos.destinatario,
      LONGITUD_MAXIMA,
      'Escribe el nombre de quien recibe el pedido.',
      `El destinatario tiene como máximo ${LONGITUD_MAXIMA} caracteres.`,
    ),
    calle: errorDeTexto(
      campos.calle,
      LONGITUD_MAXIMA_CALLE,
      'Escribe la calle y el número.',
      `La calle tiene como máximo ${LONGITUD_MAXIMA_CALLE} caracteres.`,
    ),
    ciudad: errorDeTexto(
      campos.ciudad,
      LONGITUD_MAXIMA,
      'Escribe la ciudad.',
      `La ciudad tiene como máximo ${LONGITUD_MAXIMA} caracteres.`,
    ),
    provincia: errorDeTexto(
      campos.provincia,
      LONGITUD_MAXIMA,
      'Escribe la provincia.',
      `La provincia tiene como máximo ${LONGITUD_MAXIMA} caracteres.`,
    ),
  }
  if (campos.detalles.length > LONGITUD_MAXIMA) {
    errores.detalles = `Los detalles tienen como máximo ${LONGITUD_MAXIMA} caracteres.`
  }
  if (!FORMATO_CODIGO_POSTAL.test(campos.codigoPostal)) errores.codigoPostal = 'El código postal tiene 5 dígitos.'
  if (!FORMATO_TELEFONO.test(campos.telefono)) errores.telefono = 'El teléfono tiene 9 dígitos.'
  return Object.fromEntries(Object.entries(errores).filter(([, mensaje]) => mensaje))
}

// Sin espacios sobrantes; unos detalles vacíos se envían como ausentes, como los guarda el backend
function limpios(campos) {
  const recortados = Object.fromEntries(Object.entries(campos).map(([nombre, valor]) => [nombre, valor.trim()]))
  return { ...recortados, detalles: recortados.detalles || null }
}

// Los campos de una dirección guardada, para empezar a editarla (la principal no se edita aquí)
const camposDe = (direccion) =>
  direccion
    ? Object.fromEntries(Object.keys(CAMPOS_VACIOS).map((nombre) => [nombre, direccion[nombre] ?? '']))
    : CAMPOS_VACIOS

// Mensajes propios según el código del backend: nunca su texto ni los datos enviados
const MENSAJES = {
  ADDRESS_LIMIT_REACHED: 'Ya tienes 5 direcciones. Elimina una para añadir otra.',
  ADDRESS_NOT_FOUND: 'Esa dirección ya no existe. Cierra el panel para actualizar la lista.',
}
const MENSAJE_RECHAZO = 'No se ha podido guardar la dirección. Revisa los datos e inténtalo de nuevo.'
const mensajeDeFallo = (error) => MENSAJES[error?.code] ?? MENSAJE_RECHAZO

// Misma superficie que el panel de alta de métodos de pago (HU6), siguiendo DESIGN.md
const useStyles = makeStyles({
  panel: {
    width: '100%',
    maxWidth: '520px',
    alignSelf: 'center',
    boxSizing: 'border-box',
    padding: tokens.spacingVerticalXL,
    gap: tokens.spacingVerticalL,
    boxShadow: tokens.shadow28,
    animationName: {
      from: { opacity: 0, transform: 'translateY(8px)' },
      to: { opacity: 1, transform: 'translateY(0)' },
    },
    animationDuration: tokens.durationNormal,
    animationTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  title: {
    margin: 0,
  },
  fields: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  // minmax(0, 1fr) y minWidth 0 en el input: sin ellos, el ancho propio de cada input desborda el panel
  twoColumns: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    alignItems: 'start',
    gap: tokens.spacingHorizontalM,
  },
  shrinkable: {
    minWidth: 0,
  },
  actions: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
})

// Alta o edición de una dirección de envío (HU5): sin `direccion` es una nueva; con ella, se edita esa.
export default function AddressFormPanel({ direccion, onSaved, onCancel }) {
  const styles = useStyles()
  const [campos, setCampos] = useState(() => camposDe(direccion))
  const [errores, setErrores] = useState({})
  const [fallo, setFallo] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const editando = Boolean(direccion)

  // Props comunes de cada campo: su valor, su cambio y su mensaje de error
  const campo = (nombre) => ({
    value: campos[nombre],
    onChange: (_event, data) => setCampos((actuales) => ({ ...actuales, [nombre]: data.value })),
  })
  const error = (nombre) =>
    errores[nombre] ? { validationState: 'error', validationMessage: errores[nombre] } : {}

  const guardar = async () => {
    const datos = limpios(campos)
    const encontrados = erroresDe({ ...datos, detalles: datos.detalles ?? '' })
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return
    setFallo(null)
    setGuardando(true)
    try {
      onSaved(editando ? await updateAddress(direccion.id, datos) : await addAddress(datos))
    } catch (error) {
      setFallo(mensajeDeFallo(error))
      setGuardando(false)
    }
  }

  return (
    <Card className={styles.panel} aria-labelledby="titulo-direccion" role="region">
      <Subtitle1 as="h2" id="titulo-direccion" className={styles.title}>
        {editando ? 'Editar dirección' : 'Nueva dirección'}
      </Subtitle1>

      {fallo && (
        <MessageBar intent="error">
          <MessageBarBody>{fallo}</MessageBarBody>
        </MessageBar>
      )}

      <div className={styles.fields}>
        <Field label="Destinatario" {...error('destinatario')}>
          <Input {...campo('destinatario')} placeholder="Nombre y apellidos" autoComplete="name" />
        </Field>
        <Field label="Calle y número" {...error('calle')}>
          <Input {...campo('calle')} placeholder="Calle Mayor 1" autoComplete="address-line1" />
        </Field>
        <Field label="Piso, puerta… (opcional)" {...error('detalles')}>
          <Input {...campo('detalles')} placeholder="2º B" autoComplete="address-line2" />
        </Field>
        <div className={styles.twoColumns}>
          <Field label="Código postal" {...error('codigoPostal')}>
            <Input
              {...campo('codigoPostal')}
              className={styles.shrinkable}
              placeholder="13071"
              inputMode="numeric"
              autoComplete="postal-code"
            />
          </Field>
          <Field label="Ciudad" {...error('ciudad')}>
            <Input {...campo('ciudad')} className={styles.shrinkable} autoComplete="address-level2" />
          </Field>
        </div>
        <div className={styles.twoColumns}>
          <Field label="Provincia" {...error('provincia')}>
            <Input {...campo('provincia')} className={styles.shrinkable} autoComplete="address-level1" />
          </Field>
          <Field label="Teléfono" {...error('telefono')}>
            <Input
              {...campo('telefono')}
              className={styles.shrinkable}
              type="tel"
              placeholder="600000000"
              autoComplete="tel-national"
            />
          </Field>
        </div>
      </div>

      <div className={styles.actions}>
        <Button appearance="primary" onClick={guardar} disabled={guardando}>
          Guardar dirección
        </Button>
        <Button appearance="subtle" onClick={onCancel} disabled={guardando}>
          Cancelar
        </Button>
      </div>
    </Card>
  )
}

AddressFormPanel.propTypes = {
  direccion: direccionPropType,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}
