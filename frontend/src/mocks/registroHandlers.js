import { http, HttpResponse } from 'msw'
import { USUARIOS } from './authHandlers'

const emailsRegistrados = new Set(USUARIOS.map((usuario) => usuario.email))
const nombresComercialesRegistrados = new Set(['Tienda Vera'])

const conflicto = (code) => HttpResponse.json({ code }, { status: 409 })

// Como el backend real: 201 sin cuerpo (fallos de la demo del Sprint 1)
function crearUsuario(email) {
  emailsRegistrados.add(email)
  return new HttpResponse(null, { status: 201 })
}

export const registroHandlers = [
  http.post('/api/auth/register/cliente', async ({ request }) => {
    const { email } = await request.json()
    if (emailsRegistrados.has(email)) {
      return conflicto('EMAIL_ALREADY_REGISTERED')
    }
    return crearUsuario(email)
  }),

  http.post('/api/auth/register/vendedor', async ({ request }) => {
    const { email, nombreComercial } = await request.json()
    const emailRepetido = emailsRegistrados.has(email)
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
