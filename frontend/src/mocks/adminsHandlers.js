import { http, HttpResponse } from 'msw'
import { USUARIOS, getUsuarioEnSesion } from './authHandlers'

// Emails de los administradores creados durante la sesión. Nunca se guarda el password.
let emailsCreados = new Set()
let siguienteId = 1

export function resetAdminsMock() {
  emailsCreados = new Set()
  siguienteId = 1
}

const emailYaRegistrado = (email) =>
  emailsCreados.has(email) || USUARIOS.some((usuario) => usuario.email === email)

export const adminsHandlers = [
  http.post('/api/admin/admins', async ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })

    const { nombre, apellidos, email, sede } = await request.json()
    if (emailYaRegistrado(email)) {
      return HttpResponse.json({ code: 'EMAIL_ALREADY_REGISTERED' }, { status: 409 })
    }

    emailsCreados.add(email)
    const administrador = {
      id: `adm-${siguienteId}`,
      nombre,
      apellidos,
      email,
      sede,
      rol: 'ADMINISTRADOR',
      estado: 'ACTIVO',
      fechaIncorporacion: new Date().toISOString().slice(0, 10),
    }
    siguienteId += 1
    return HttpResponse.json(administrador, { status: 201 })
  }),
]
