import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Card,
  Field,
  Input,
  Subtitle1,
  Text,
  ToggleButton,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  Calendar20Regular,
  Globe20Regular,
  LockClosed16Regular,
  LockClosed20Regular,
  Payment20Regular,
  Phone20Regular,
  Wallet20Regular,
} from '@fluentui/react-icons'

// Los tres tipos que admite el backend, en el orden de los botones del selector
const TIPOS = [
  { valor: 'TARJETA', etiqueta: 'Tarjeta', Icono: Payment20Regular },
  { valor: 'PAYPAL', etiqueta: 'PayPal', Icono: Wallet20Regular },
  { valor: 'BIZUM', etiqueta: 'Bizum', Icono: Phone20Regular },
]

const CAMPOS_VACIOS = {
  numeroTarjeta: '',
  caducidad: '',
  cvv: '',
  titular: '',
  urlPayPal: '',
  telefonoBizum: '',
}

// Solo formatos básicos, con los mismos límites que el backend, para avisar antes de enviar. Lo demás (Luhn, que la
// tarjeta no esté caducada) lo comprueba el backend.
const FORMATO_NUMERO = /^\d{13,19}$/
const FORMATO_CADUCIDAD = /^(0[1-9]|1[0-2])\/\d{2}$/
const FORMATO_CVV = /^\d{3,4}$/
const FORMATO_TITULAR = /^[\p{L} '-]{1,26}$/u
const FORMATO_TELEFONO = /^\d{9}$/

// El número se puede escribir con espacios, como en la tarjeta; se envía sin ellos
const sinEspacios = (texto) => texto.replaceAll(/\s/g, '')

function erroresDeTarjeta({ numeroTarjeta, caducidad, cvv, titular }) {
  const errores = {}
  if (!FORMATO_NUMERO.test(sinEspacios(numeroTarjeta))) {
    errores.numeroTarjeta = 'El número de tarjeta tiene entre 13 y 19 dígitos.'
  }
  if (!FORMATO_CADUCIDAD.test(caducidad.trim())) errores.caducidad = 'Escribe la caducidad como MM/AA.'
  if (!FORMATO_CVV.test(cvv)) errores.cvv = 'El CVV tiene 3 o 4 dígitos.'
  if (!FORMATO_TITULAR.test(titular.trim())) {
    errores.titular = 'El titular solo puede tener letras, espacios, guiones y apóstrofos (hasta 26).'
  }
  return errores
}

// Como el backend: una URL con esquema y servidor
function esUrlCompleta(texto) {
  try {
    return new URL(texto.trim()).hostname !== ''
  } catch {
    return false
  }
}

// Errores de formato del método, según su tipo
function erroresDe(tipo, campos) {
  if (tipo === 'TARJETA') return erroresDeTarjeta(campos)
  if (tipo === 'PAYPAL' && !esUrlCompleta(campos.urlPayPal)) {
    return { urlPayPal: 'Escribe una URL completa, como https://paypal.me/tu-cuenta.' }
  }
  if (tipo === 'BIZUM' && !FORMATO_TELEFONO.test(campos.telefonoBizum.trim())) {
    return { telefonoBizum: 'El teléfono tiene 9 dígitos.' }
  }
  return {}
}

// Composición inspirada en la tarjeta «Payment Details», adaptada a DESIGN.md: superficie blanca con la sombra del
// panel protagonista (sin efecto cristal), el naranja como único acento y una entrada corta que no se reproduce
// con movimiento reducido.
const useStyles = makeStyles({
  panel: {
    width: '100%',
    maxWidth: '440px',
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
  header: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  title: {
    margin: 0,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  types: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: tokens.spacingHorizontalS,
  },
  type: {
    height: '48px',
  },
  fields: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  twoColumns: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, 1fr)',
    gap: tokens.spacingHorizontalM,
  },
  actions: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
  secure: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: tokens.spacingHorizontalXS,
    color: tokens.colorNeutralForeground3,
  },
})

// Alta de un método de pago (HU6). Los datos solo viven en este panel mientras se rellena.
export default function AddPaymentMethodPanel({ onCancel }) {
  const styles = useStyles()
  const [tipo, setTipo] = useState('TARJETA')
  const [campos, setCampos] = useState(CAMPOS_VACIOS)
  const [errores, setErrores] = useState({})

  // Props comunes de cada campo: su valor, su cambio y su mensaje de error
  const campo = (nombre) => ({
    value: campos[nombre],
    onChange: (_event, data) => setCampos((actuales) => ({ ...actuales, [nombre]: data.value })),
  })
  const error = (nombre) =>
    errores[nombre] ? { validationState: 'error', validationMessage: errores[nombre] } : {}

  const guardar = () => {
    setErrores(erroresDe(tipo, campos))
  }

  return (
    <Card className={styles.panel} aria-labelledby="titulo-nuevo-metodo" role="region">
      <div className={styles.header}>
        <Subtitle1 as="h2" id="titulo-nuevo-metodo" className={styles.title}>
          Datos del método de pago
        </Subtitle1>
        <Text className={styles.muted}>Se guardan cifrados.</Text>
      </div>

      <div className={styles.types} role="group" aria-label="Tipo de método de pago">
        {TIPOS.map(({ valor, etiqueta, Icono }) => (
          <ToggleButton
            key={valor}
            className={styles.type}
            icon={<Icono />}
            checked={tipo === valor}
            onClick={() => setTipo(valor)}
          >
            {etiqueta}
          </ToggleButton>
        ))}
      </div>

      {tipo === 'TARJETA' && (
        <div className={styles.fields}>
          <Field label="Número de tarjeta" {...error('numeroTarjeta')}>
            <Input
              {...campo('numeroTarjeta')}
              contentBefore={<Payment20Regular />}
              placeholder="0000 0000 0000 0000"
              inputMode="numeric"
              autoComplete="cc-number"
            />
          </Field>
          <div className={styles.twoColumns}>
            <Field label="Caducidad" {...error('caducidad')}>
              <Input {...campo('caducidad')} contentBefore={<Calendar20Regular />} placeholder="MM/AA" autoComplete="cc-exp" />
            </Field>
            <Field label="CVV" {...error('cvv')}>
              <Input
                {...campo('cvv')}
                contentBefore={<LockClosed20Regular />}
                type="password"
                placeholder="123"
                inputMode="numeric"
                autoComplete="cc-csc"
              />
            </Field>
          </div>
          <Field label="Titular" {...error('titular')}>
            <Input {...campo('titular')} placeholder="Nombre y apellidos" autoComplete="cc-name" />
          </Field>
        </div>
      )}

      {tipo === 'PAYPAL' && (
        <Field label="URL de PayPal" {...error('urlPayPal')}>
          <Input {...campo('urlPayPal')} contentBefore={<Globe20Regular />} type="url" placeholder="https://paypal.me/tu-cuenta" />
        </Field>
      )}

      {tipo === 'BIZUM' && (
        <Field label="Teléfono de Bizum" {...error('telefonoBizum')}>
          <Input {...campo('telefonoBizum')} contentBefore={<Phone20Regular />} type="tel" placeholder="600000000" autoComplete="tel-national" />
        </Field>
      )}

      <div className={styles.actions}>
        <Button appearance="primary" onClick={guardar}>
          Guardar método de pago
        </Button>
        <Button appearance="subtle" onClick={onCancel}>
          Cancelar
        </Button>
      </div>

      <Text size={200} className={styles.secure}>
        <LockClosed16Regular aria-hidden />
        Tus datos de pago se guardan cifrados. El CVV no se guarda.
      </Text>
    </Card>
  )
}

AddPaymentMethodPanel.propTypes = {
  onCancel: PropTypes.func.isRequired,
}
