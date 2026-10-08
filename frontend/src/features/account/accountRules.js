// Reglas de los datos de una cuenta compartidas por el registro y el alta de administradores.

export const REQUISITOS_PASSWORD = [
  { texto: 'Al menos 8 caracteres', cumple: (password) => password.length >= 8 },
  { texto: 'Una mayúscula', cumple: (password) => /\p{Lu}/u.test(password) },
  { texto: 'Una minúscula', cumple: (password) => /\p{Ll}/u.test(password) },
  { texto: 'Un número', cumple: (password) => /\p{N}/u.test(password) },
  { texto: 'Un símbolo', cumple: (password) => /[^\p{L}\p{N}]/u.test(password) },
]

export const esEmailValido = (email) => /^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/.test(email)

// Errores del password y de su repetición. Solo se comprueba lo que está relleno:
// los campos vacíos los marca la validación de obligatorios.
export function validarPasswords({ password, repeticionPassword }) {
  const errores = {}
  if (password && !REQUISITOS_PASSWORD.every(({ cumple }) => cumple(password))) {
    errores.password = 'El password no cumple los requisitos'
  }
  if (password && repeticionPassword && password !== repeticionPassword) {
    errores.repeticionPassword = 'Los passwords no coinciden'
  }
  return errores
}
