import { ApiError } from '../../api/client'

const MESSAGES = {
  generic: 'No se ha podido iniciar sesión. Revisa tus datos e inténtalo de nuevo.',
  code: 'El código no es válido o ha caducado.',
  authenticatorCode: 'El código de Microsoft Authenticator no es válido o ha caducado.',
  blocked: 'Esta cuenta está bloqueada. Contacta con un administrador.',
  tooManyAttempts: 'Demasiados intentos. Espera unos minutos antes de volver a intentarlo.',
  connection: 'No se ha podido conectar con el servidor. Inténtalo de nuevo más tarde.',
}

const STEP_MESSAGES = {
  credentials: MESSAGES.generic,
  emailCode: MESSAGES.code,
  authenticator: MESSAGES.authenticatorCode,
  enrollAuthenticator: MESSAGES.authenticatorCode,
}

// La cuenta existe y la contraseña es correcta, pero un administrador aún no la ha activado (HU2, AB#236).
// No es un error de formulario: el login lo muestra con una pantalla propia, no con un mensaje.
export function isPendingAccountError(error) {
  return error instanceof ApiError && error.status === 403 && error.code === 'ACCOUNT_PENDING'
}

export function getLoginErrorMessage(error, step) {
  if (!(error instanceof ApiError) || error.status >= 500) {
    return MESSAGES.connection
  }
  if (error.status === 429) {
    return MESSAGES.tooManyAttempts
  }
  // Cuenta bloqueada por un administrador (antes "desactivada"): se rechaza antes del código del correo.
  if (error.status === 403 && error.code === 'ACCOUNT_BLOCKED') {
    return MESSAGES.blocked
  }
  return STEP_MESSAGES[step] ?? MESSAGES.generic
}
