import PropTypes from 'prop-types'
import { useEffect, useRef } from 'react'
import { Button, Subtitle1, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import {
  ArrowLeft20Regular,
  CheckmarkCircle24Filled,
  Clock48Regular,
  Hourglass24Regular,
  ShieldCheckmark24Regular,
} from '@fluentui/react-icons'

const NO_MOTION = '@media (prefers-reduced-motion: reduce)'

const useStyles = makeStyles({
  screen: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalL,
    textAlign: 'center',
    animationName: { from: { opacity: 0, transform: 'translateY(12px)' }, to: { opacity: 1, transform: 'translateY(0)' } },
    animationDuration: '450ms',
    animationTimingFunction: 'ease-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  badge: {
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '96px',
    height: '96px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
    '::after': {
      content: '""',
      position: 'absolute',
      inset: 0,
      borderRadius: tokens.borderRadiusCircular,
      border: `2px solid ${tokens.colorBrandStroke1}`,
      animationName: { from: { opacity: 0.7, transform: 'scale(1)' }, to: { opacity: 0, transform: 'scale(1.6)' } },
      animationDuration: '2s',
      animationIterationCount: 'infinite',
      animationTimingFunction: 'ease-out',
      [NO_MOTION]: { animationName: 'none' },
    },
  },
  title: {
    margin: 0,
  },
  email: {
    overflowWrap: 'anywhere',
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorNeutralForeground1,
  },
  description: {
    color: tokens.colorNeutralForeground3,
  },
  steps: {
    width: '100%',
    boxSizing: 'border-box',
    listStyle: 'none',
    margin: 0,
    padding: tokens.spacingVerticalM,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    textAlign: 'left',
    backgroundColor: tokens.colorNeutralBackground2,
    borderRadius: tokens.borderRadiusLarge,
  },
  step: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    animationName: { from: { opacity: 0, transform: 'translateX(-12px)' }, to: { opacity: 1, transform: 'translateX(0)' } },
    animationDuration: '400ms',
    animationFillMode: 'both',
    animationTimingFunction: 'ease-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  delay1: { animationDelay: '350ms' },
  delay2: { animationDelay: '700ms' },
  delay3: { animationDelay: '1050ms' },
  stepIcon: {
    display: 'flex',
    flexShrink: 0,
  },
  done: {
    color: tokens.colorPaletteGreenForeground1,
  },
  current: {
    color: tokens.colorBrandForeground1,
    animationName: { from: { transform: 'rotate(0deg)' }, to: { transform: 'rotate(180deg)' } },
    animationDuration: '2.4s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    animationDirection: 'alternate',
    [NO_MOTION]: { animationName: 'none' },
  },
  upcoming: {
    color: tokens.colorNeutralForeground4,
  },
  stepText: {
    display: 'flex',
    flexDirection: 'column',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Pantalla que sustituye al formulario cuando el backend indica que la cuenta existe y la contraseña es correcta
// pero aún está pendiente de que un administrador la active (HU2, task AB#236). Los tres pasos van apareciendo uno
// a uno y el segundo (el que está en curso) se mueve para dejar claro dónde está la cuenta.
export default function PendingAccountScreen({ email, onBack }) {
  const styles = useStyles()
  const titleRef = useRef(null)

  useEffect(() => {
    titleRef.current?.focus()
  }, [])

  return (
    <section className={styles.screen} aria-live="polite">
      <span className={styles.badge} aria-hidden="true">
        <Clock48Regular />
      </span>

      <Subtitle1 as="h2" ref={titleRef} tabIndex={-1} className={styles.title}>
        Tu cuenta está pendiente de activación
      </Subtitle1>

      <Text className={styles.description}>
        {email ? (
          <>
            La cuenta <span className={styles.email}>{email}</span> existe, pero todavía no puede iniciar sesión.
          </>
        ) : (
          'Tu cuenta existe, pero todavía no puede iniciar sesión.'
        )}{' '}
        Un administrador debe verificarla antes de que puedas entrar.
      </Text>

      <ol className={styles.steps}>
        <li className={mergeClasses(styles.step, styles.delay1)}>
          <span className={mergeClasses(styles.stepIcon, styles.done)} aria-hidden="true">
            <CheckmarkCircle24Filled />
          </span>
          <span className={styles.stepText}>
            <Text weight="semibold">Cuenta creada</Text>
            <Text size={200} className={styles.muted}>Tus datos ya están registrados.</Text>
          </span>
        </li>
        <li className={mergeClasses(styles.step, styles.delay2)} aria-current="step">
          <span className={mergeClasses(styles.stepIcon, styles.current)} aria-hidden="true">
            <Hourglass24Regular />
          </span>
          <span className={styles.stepText}>
            <Text weight="semibold">Verificación por un administrador</Text>
            <Text size={200} className={styles.muted}>En curso: es el paso en el que estás ahora.</Text>
          </span>
        </li>
        <li className={mergeClasses(styles.step, styles.delay3)}>
          <span className={mergeClasses(styles.stepIcon, styles.upcoming)} aria-hidden="true">
            <ShieldCheckmark24Regular />
          </span>
          <span className={styles.stepText}>
            <Text weight="semibold">Acceso a ESIBuy</Text>
            <Text size={200} className={styles.muted}>Podrás iniciar sesión cuando la activen.</Text>
          </span>
        </li>
      </ol>

      <Button appearance="primary" icon={<ArrowLeft20Regular />} onClick={onBack}>
        Volver al inicio de sesión
      </Button>
    </section>
  )
}

PendingAccountScreen.propTypes = {
  email: PropTypes.string,
  onBack: PropTypes.func.isRequired,
}
