import { http, HttpResponse } from 'msw'
import { USUARIOS } from './authHandlers'
import { vendedoresMock } from './adminSellersHandlers'
import { existeEmailDeUsuario } from './usersHandlers'

// Como el backend: el email se compara sin espacios en los extremos y en minúsculas
const normalizarEmail = (email) => String(email ?? '').trim().toLowerCase()

const emailsRegistrados = new Set(USUARIOS.map((usuario) => usuario.email))

// Un correo es único entre todos los roles: también cuentan los usuarios de los listados de administración
const emailYaRegistrado = (email) => {
  const normalizado = normalizarEmail(email)
  return emailsRegistrados.has(normalizado)
    || existeEmailDeUsuario(normalizado)
    || vendedoresMock().some((vendedor) => vendedor.email === normalizado)
}
const nombresComercialesRegistrados = new Set(['Tienda Vera'])

const conflicto = (code) => HttpResponse.json({ code }, { status: 409 })

// Como el backend real: 201 sin cuerpo (fallos de la demo del Sprint 1)
function crearUsuario(email) {
  emailsRegistrados.add(normalizarEmail(email))
  return new HttpResponse(null, { status: 201 })
}

export const registroHandlers = [
  http.post('/api/auth/register/cliente', async ({ request }) => {
    const { email } = await request.json()
    if (emailYaRegistrado(email)) {
      return conflicto('EMAIL_ALREADY_REGISTERED')
    }
    return crearUsuario(email)
  }),

  http.post('/api/auth/register/vendedor', async ({ request }) => {
    const { email, nombreComercial } = await request.json()
    const emailRepetido = emailYaRegistrado(email)
    const nombreComercialRepetido = nombresComercialesRegistrados.has(nombreComercial)

    if (emailRepetido && nombreComercialRepetido) {
      return conflicto('EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED')
    }
    if (emailRepetido) {
      return conflicto('EMAIL_ALREADY_REGISTERED')
    }
    if (nombreComercialRepetido) {
      return conflicto('COMMERCIAL_NAME_ALREADY_REGISTERED')
    }

    nombresComercialesRegistrados.add(nombreComercial)
    return crearUsuario(email)
  }),
]
