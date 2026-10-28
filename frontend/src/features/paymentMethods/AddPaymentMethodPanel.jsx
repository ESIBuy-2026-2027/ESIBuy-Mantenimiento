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
          <Field label="Número de tarjeta">
            <Input
              contentBefore={<Payment20Regular />}
              placeholder="0000 0000 0000 0000"
              inputMode="numeric"
              autoComplete="cc-number"
            />
          </Field>
          <div className={styles.twoColumns}>
            <Field label="Caducidad">
              <Input contentBefore={<Calendar20Regular />} placeholder="MM/AA" autoComplete="cc-exp" />
            </Field>
            <Field label="CVV">
              <Input
                contentBefore={<LockClosed20Regular />}
                type="password"
                placeholder="123"
                inputMode="numeric"
                autoComplete="cc-csc"
              />
            </Field>
          </div>
          <Field label="Titular">
            <Input placeholder="Nombre y apellidos" autoComplete="cc-name" />
          </Field>
        </div>
      )}

      <div className={styles.actions}>
        <Button appearance="primary">Guardar método de pago</Button>
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
