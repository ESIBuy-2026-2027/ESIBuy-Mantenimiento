import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Datos del perfil de los usuarios de ejemplo (HU4), además de los de la sesión (nombre, apellidos, email y rol).
// Vicente (vendedor-vacio@esibuy.test) y Nora (nuevo@esibuy.test) no tienen teléfono ni imagen de perfil.
const DATOS_DE_PERFIL = {
  'u-2': {
    fechaAlta: '2026-03-02T09:15:00Z',
    dni: '12345678Z',
    telefono: '600111222',
    avatar: 'avatar2',
    nombreComercial: 'Tienda de Vera',
    categoriaPrincipalId: 'cat-1',
  },
  'u-3': {
    fechaAlta: '2026-04-10T17:40:00Z',
    dni: '87654321X',
    telefono: '611222333',
    avatar: 'avatar1',
    fechaNacimiento: '1998-05-14',
    tipoCliente: 'PREMIUM',
  },
  'u-5': {
    fechaAlta: '2026-05-20T11:00:00Z',
    dni: '11111111H',
    nombreComercial: 'Casa Vicente',
    categoriaPrincipalId: 'cat-2',
  },
  'u-7': {
    fechaAlta: '2026-09-01T08:30:00Z',
    dni: '22222222J',
    fechaNacimiento: '2001-11-30',
    tipoCliente: 'ESTANDAR',
  },
}

// Como el backend (PR #140): solo el cliente y el vendedor tienen perfil; el usuario sale de la sesión.
export const profileHandlers = [
  http.get('/api/perfil', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE' && usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })

    const { id, nombre, apellidos, email, rol } = usuario
    return HttpResponse.json({ id, nombre, apellidos, email, rol, ...DATOS_DE_PERFIL[id] })
  }),
]
