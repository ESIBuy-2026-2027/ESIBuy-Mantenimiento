import { http, HttpResponse } from 'msw'
import { USUARIOS } from './authHandlers'

const emailsRegistrados = new Set(USUARIOS.map((usuario) => usuario.email))
const nombresComercialesRegistrados = new Set(['Tienda Vera'])
let siguienteId = USUARIOS.length + 1

const conflicto = (code) => HttpResponse.json({ code }, { status: 409 })

function crearUsuario(email, rol) {
  emailsRegistrados.add(email)
  const usuario = { id: `u-${siguienteId}`, email, rol }
  siguienteId += 1
  return HttpResponse.json(usuario, { status: 201 })
}

export const registroHandlers = [
  http.post('/api/auth/register/cliente', async ({ request }) => {
    const { email } = await request.json()
    if (emailsRegistrados.has(email)) {
      return conflicto('EMAIL_ALREADY_REGISTERED')
    }
    return crearUsuario(email, 'CLIENTE')
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
    return crearUsuario(email, 'VENDEDOR')
  }),
]
