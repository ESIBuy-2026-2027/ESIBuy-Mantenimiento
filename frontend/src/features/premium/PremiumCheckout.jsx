import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { ArrowLeft20Regular, Diamond24Regular, LockClosed20Regular } from '@fluentui/react-icons'
import { usePageStyles } from '../../components/pageStyles'
import { PREMIUM_BENEFITS } from './premiumBenefits'

const useStyles = makeStyles({
  summary: {
    width: '100%',
    maxWidth: '520px',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalXL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow8,
  },
  summaryTitle: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    fontSize: tokens.fontSizeBase500,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
  },
  benefits: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    margin: 0,
    paddingLeft: tokens.spacingHorizontalXL,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

// Pasarela de pago simulada de ESIBuy Premium (HU45), con el mismo aspecto que la del carrito (HU10). No pide datos de
// tarjeta ni muestra ningún precio: «Simular pago» llama a `onPay` y, mientras termina, muestra «Procesando pago…».
// La usan el registro como cliente Premium y el paso a Premium de un cliente Estándar.
export default function PremiumCheckout({ description, cancelLabel, onPay, onCancel, error = null }) {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const [paying, setPaying] = useState(false)

  const pay = async () => {
    setPaying(true)
    try {
      await onPay()
    } finally {
      setPaying(false)
    }
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Pasarela de pago
        </Title1>
        <Text className={pageStyles.subtitle}>{description}</Text>
      </div>

      <section aria-label="Resumen del pago" className={styles.summary}>
        <h2 className={styles.summaryTitle}>
          <Diamond24Regular aria-hidden="true" />
          ESIBuy Premium
        </h2>
        <ul className={styles.benefits}>
          {PREMIUM_BENEFITS.map(({ id, titulo }) => (
            <li key={id}>{titulo}</li>
          ))}
        </ul>

        {error && (
          <MessageBar intent="error">
            <MessageBarBody>{error}</MessageBarBody>
          </MessageBar>
        )}

        <div className={styles.actions}>
          <Button
            appearance="primary"
            size="large"
            icon={paying ? <Spinner size="tiny" /> : <LockClosed20Regular />}
            disabled={paying}
            onClick={pay}
          >
            {paying ? 'Procesando pago…' : 'Simular pago'}
          </Button>
          <Button size="large" icon={<ArrowLeft20Regular />} disabled={paying} onClick={onCancel}>
            {cancelLabel}
          </Button>
        </div>
        <Text size={200} className={styles.muted}>
          Es una simulación: no se realiza ningún cobro real.
        </Text>
      </section>
    </main>
  )
}

PremiumCheckout.propTypes = {
  description: PropTypes.string.isRequired,
  cancelLabel: PropTypes.string.isRequired,
  onPay: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  error: PropTypes.string,
}
