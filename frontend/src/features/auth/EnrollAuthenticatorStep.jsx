import PropTypes from 'prop-types'
import { Body1, Subtitle1 } from '@fluentui/react-components'
import CodeStep from './CodeStep'
import { useFormStyles } from './authFormStyles'

// Alta de Microsoft Authenticator en el primer login (HU2, T20): el usuario escanea el QR con la app
// y confirma el alta con el primer código que le muestra, que también abre la sesión.
export default function EnrollAuthenticatorStep({ qr, onSubmit, onBack, submitting }) {
  const styles = useFormStyles()

  return (
    <>
      <Subtitle1 as="h2">Configura Microsoft Authenticator</Subtitle1>
      <div className={styles.form}>
        <Body1>
          Abre Microsoft Authenticator, pulsa «Añadir cuenta» y escanea este código QR. Solo tendrás que
          hacerlo esta vez.
        </Body1>
        <img src={qr} alt="Código QR para añadir ESIBuy a Microsoft Authenticator" width={200} height={200} />
      </div>
      <CodeStep
        label="Código de Microsoft Authenticator"
        hint="Introduce el código de 6 dígitos que aparece en la app para terminar la configuración."
        onSubmit={onSubmit}
        onBack={onBack}
        submitting={submitting}
      />
    </>
  )
}

EnrollAuthenticatorStep.propTypes = {
  qr: PropTypes.string.isRequired,
  onSubmit: PropTypes.func.isRequired,
  onBack: PropTypes.func.isRequired,
  submitting: PropTypes.bool.isRequired,
}
