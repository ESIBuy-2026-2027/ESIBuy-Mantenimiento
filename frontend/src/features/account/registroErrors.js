// Traduce los errores del backend en los formularios de cuenta (registro, alta de administrador y edición)
// al campo que hay que marcar y su mensaje. El backend indica el motivo con un code (fallos de la demo del
// Sprint 1, PR #104); el mensaje del backend es siempre genérico y no se muestra.

const EMAIL_YA_REGISTRADO = 'Este email ya está registrado'
const NOMBRE_COMERCIAL_YA_REGISTRADO = 'Este nombre comercial ya está registrado'

const CAMPOS_POR_CODE = {
  PASSWORD_TOO_SHORT: { password: 'La contraseña debe tener al menos 8 caracteres' },
  PASSWORD_TOO_LONG: { password: 'La contraseña no puede tener más de 64 caracteres' },
  PASSWORD_WEAK: { password: 'La contraseña debe tener mayúscula, minúscula, número y símbolo' },
  PASSWORD_TOO_COMMON: { password: 'Esta contraseña es demasiado común' },
  PASSWORD_CONTAINS_PERSONAL_DATA: {
    password: 'La contraseña no puede contener tu nombre, tus apellidos ni tu email',
  },
  PASSWORDS_DO_NOT_MATCH: { repeticionPassword: 'Las contraseñas no coinciden' },
  INVALID_EMAIL: { email: 'El email no tiene un formato válido' },
  INVALID_DNI: { dni: 'El DNI debe tener 8 números y una letra' },
  INVALID_PHONE: { telefono: 'El teléfono debe tener 9 números' },
  UNDERAGE: { fechaNacimiento: 'Debes ser mayor de edad para registrarte' },
  PRIVACY_NOT_ACCEPTED: { aceptaPrivacidad: 'Debes aceptar la política de privacidad' },
  EMAIL_ALREADY_REGISTERED: { email: EMAIL_YA_REGISTRADO },
  DNI_ALREADY_REGISTERED: { dni: 'Este DNI ya está registrado' },
  COMMERCIAL_NAME_ALREADY_REGISTERED: { nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO },
  EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED: {
    email: EMAIL_YA_REGISTRADO,
    nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO,
  },
}

const ERRORES_DE_DATOS = new Set([400, 409])

export const MENSAJE_DATOS_INVALIDOS = 'Revisa los datos del formulario'
export const MENSAJE_ERROR_SERVIDOR = 'No se ha podido completar la operación. Inténtalo de nuevo más tarde'

// Devuelve { campos: { campo: mensaje } } si el error se puede marcar en el formulario, o { general: mensaje }.
export function erroresDelServidor(error) {
  if (!ERRORES_DE_DATOS.has(error?.status)) {
    return { general: MENSAJE_ERROR_SERVIDOR }
  }
  const campos = CAMPOS_POR_CODE[error.code]
  return campos ? { campos } : { general: MENSAJE_DATOS_INVALIDOS }
}
