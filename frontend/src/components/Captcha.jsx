import ReCAPTCHA from 'react-google-recaptcha'
import { MessageBar, MessageBarBody } from '@fluentui/react-components'

const SITE_KEY = import.meta.env.VITE_RECAPTCHA_SITE_KEY

export default function Captcha({ onChange }) {
  if (!SITE_KEY) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>El captcha no está configurado. Avisa al equipo de desarrollo.</MessageBarBody>
      </MessageBar>
    )
  }

  return (
    <ReCAPTCHA
      sitekey={SITE_KEY}
      hl="es"
      onChange={(token) => onChange(token ?? '')}
      onExpired={() => onChange('')}
      onErrored={() => onChange('')}
    />
  )
}
