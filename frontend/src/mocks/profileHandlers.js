import { http, HttpResponse } from 'msw'
import { cerrarSesionMock, eliminarCuentaMock, esClaveValida, getUsuarioEnSesion } from './authHandlers'

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

// Clientes que se han hecho Premium (HU45) desde que se reiniciaron los mocks
let pasadosAPremium = new Set()

export function resetProfileMock() {
  pasadosAPremium = new Set()
}

function perfilDe({ id, nombre, apellidos, email, rol }) {
  const perfil = { id, nombre, apellidos, email, rol, ...DATOS_DE_PERFIL[id] }
  return pasadosAPremium.has(id) ? { ...perfil, tipoCliente: 'PREMIUM' } : perfil
}

// Como el backend (PR #140): solo el cliente y el vendedor tienen perfil; el usuario sale de la sesión.
export const profileHandlers = [
  http.get('/api/perfil', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE' && usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(perfilDe(usuario))
  }),

  // Paso a Premium (HU45), con el contrato de las tasks T422 y T423 del backend: solo el cliente sobre sí mismo.
  // 200 con el perfil ya Premium; 400 ALREADY_PREMIUM si ya lo era; 403 ACCESS_DENIED con otro rol; 401 sin sesión.
  http.post('/api/perfil/premium', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') {
      return HttpResponse.json({ mensaje: 'Acceso denegado', code: 'ACCESS_DENIED' }, { status: 403 })
    }
    if (perfilDe(usuario).tipoCliente === 'PREMIUM') {
      return HttpResponse.json({ mensaje: 'El cliente ya es Premium', code: 'ALREADY_PREMIUM' }, { status: 400 })
    }

    pasadosAPremium.add(usuario.id)
    return HttpResponse.json(perfilDe(usuario))
  }),

  // Eliminar la cuenta propia: un único endpoint para el vendedor (HU47, PR #203) y el cliente (HU25, PR #235). Sin
  // sesión 401 y el administrador 403. Sin contraseña, 400 VALIDATION_ERROR; con una incorrecta, 400 WRONG_PASSWORD y
  // la sesión sigue abierta. Con la correcta responde 204 y cierra la sesión; la cuenta del cliente deja de existir
  // para el login. La del vendedor no se borra de USUARIOS: así el siguiente test, o la siguiente recarga, vuelve a
  // tenerla.
  http.delete('/api/perfil', async ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol === 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })

    const { password } = await request.json()
    if (typeof password !== 'string' || !password.trim()) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }
    if (!esClaveValida(password)) return HttpResponse.json({ code: 'WRONG_PASSWORD' }, { status: 400 })

    if (usuario.rol === 'CLIENTE') eliminarCuentaMock(usuario)
    else cerrarSesionMock()
    return new HttpResponse(null, { status: 204 })
  }),
]
