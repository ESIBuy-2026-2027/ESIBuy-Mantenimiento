import { http, HttpResponse } from 'msw'
import { USUARIOS, getUsuarioEnSesion } from './authHandlers'
import { agregarUsuario, existeEmailDeUsuario } from './usersHandlers'

let siguienteId = 1

export function resetAdminsMock() {
  siguienteId = 1
}

const emailYaRegistrado = (email) =>
  existeEmailDeUsuario(email) || USUARIOS.some((usuario) => usuario.email === email)

export const adminsHandlers = [
  // El administrador creado se añade al listado de usuarios (HU19), así que aparece en el de
  // administradores. Nunca se guarda el password.
  http.post('/api/admin/admins', async ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })

    const { nombre, apellidos, email, sede } = await request.json()
    if (emailYaRegistrado(email)) {
      return HttpResponse.json({ code: 'EMAIL_ALREADY_REGISTERED' }, { status: 409 })
    }

    const ahora = new Date().toISOString().slice(0, 19)
    const administrador = {
      id: `adm-${siguienteId}`,
      nombre,
      apellidos,
      email,
      sede,
      rol: 'ADMINISTRADOR',
      estado: 'ACTIVO',
      fechaAlta: ahora,
    }
    siguienteId += 1
    agregarUsuario(administrador)
    return HttpResponse.json(administrador, { status: 201 })
  }),
]
