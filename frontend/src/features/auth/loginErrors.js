import { ApiError } from '../../api/client'

const MESSAGES = {
  generic: 'No se ha podido iniciar sesión. Revisa tus datos e inténtalo de nuevo.',
  code: 'El código no es válido o ha caducado.',
  captcha: 'No se ha podido verificar el captcha. Inténtalo de nuevo.',
  disabled: 'Esta cuenta está desactivada. Contacta con un administrador.',
  tooManyAttempts: 'Demasiados intentos. Espera unos minutos antes de volver a intentarlo.',
  connection: 'No se ha podido conectar con el servidor. Inténtalo de nuevo más tarde.',
}

const STEP_MESSAGES = {
  credentials: MESSAGES.generic,
  emailCode: MESSAGES.code,
  captcha: MESSAGES.captcha,
}

export function getLoginErrorMessage(error, step) {
  if (!(error instanceof ApiError) || error.status >= 500) {
    return MESSAGES.connection
  }
  if (error.status === 429) {
    return MESSAGES.tooManyAttempts
  }
  if (error.status === 403 && error.code === 'ACCOUNT_DISABLED') {
    return MESSAGES.disabled
  }
  return STEP_MESSAGES[step] ?? MESSAGES.generic
}
