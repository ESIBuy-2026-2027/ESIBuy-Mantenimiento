// Reglas de los datos de una cuenta compartidas por el registro y el alta de administradores.

export const REQUISITOS_PASSWORD = [
  { texto: 'Al menos 8 caracteres', cumple: (password) => password.length >= 8 },
  { texto: 'Una mayúscula', cumple: (password) => /\p{Lu}/u.test(password) },
  { texto: 'Una minúscula', cumple: (password) => /\p{Ll}/u.test(password) },
  { texto: 'Un número', cumple: (password) => /\p{N}/u.test(password) },
  { texto: 'Un símbolo', cumple: (password) => /[^\p{L}\p{N}]/u.test(password) },
]

export const esEmailValido = (email) => /^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/.test(email)

// Nombres personales: letras Unicode, espacios, guiones y apóstrofos; longitud de 2 a 30 caracteres.
// Descarta caracteres no admitidos al escribir o pegar.
export const filtrarNombre = (nombre) => nombre.replace(/[^\p{L}\p{M} '-]/gu, '')

export const esNombreValido = (nombre) => {
  const valor = nombre.trim()
  return valor.length >= 2 && valor.length <= 30 && /^\p{L}[\p{L}\p{M}]*(?:[ '-][\p{L}\p{M}]+)*$/u.test(valor)
}

export const esTelefonoValido = (telefono) => /^\d{9}$/.test(telefono)

export function esMayorDeEdad(fecha) {
  const nacimiento = new Date(fecha)
  const hoy = new Date()
  let edad = hoy.getFullYear() - nacimiento.getFullYear()
  const cumpleEsteAnio =
    hoy.getMonth() > nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() >= nacimiento.getDate())
  if (!cumpleEsteAnio) edad--
  return edad >= 18
}

// Errores de la contraseña y de su repetición. Solo se comprueba lo que está relleno:
// los campos vacíos los marca la validación de obligatorios.
export function validarPasswords({ password, repeticionPassword }) {
  const errores = {}
  if (password && !REQUISITOS_PASSWORD.every(({ cumple }) => cumple(password))) {
    errores.password = 'La contraseña no cumple los requisitos'
  }
  if (password && repeticionPassword && password !== repeticionPassword) {
    errores.repeticionPassword = 'Las contraseñas no coinciden'
  }
  return errores
}

// El teléfono es opcional: si está vacío no se envía, porque el backend solo lo da por ausente si no llega
export function sinTelefonoVacio(datos) {
  const { telefono, ...resto } = datos
  return telefono ? datos : resto
}
