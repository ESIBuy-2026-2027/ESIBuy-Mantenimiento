import { http, HttpResponse } from 'msw'
import { USUARIOS, cerrarSesionMock, eliminarCuentaMock, esClaveValida, getUsuarioEnSesion } from './authHandlers'

const TEXTO_MAXIMO = 30
const EDAD_MINIMA = 18

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

// Lo que cada usuario ha editado de su perfil (HU45), encima de los datos de ejemplo; se vacía al reiniciar los mocks.
let ediciones = new Map()

export function resetProfileMock() {
  ediciones = new Map()
}

const perfilDe = ({ id, nombre, apellidos, email, rol }) => ({
  id,
  nombre,
  apellidos,
  email,
  rol,
  ...DATOS_DE_PERFIL[id],
  ...ediciones.get(id),
})

const respuestaDeError = (status, code) => HttpResponse.json({ mensaje: 'Datos inválidos', code }, { status })

const textoValido = (valor) => typeof valor === 'string' && valor.trim().length > 0 && valor.trim().length <= TEXTO_MAXIMO

const esMayorDeEdad = (fecha) => {
  const limite = new Date()
  limite.setFullYear(limite.getFullYear() - EDAD_MINIMA)
  return new Date(fecha) <= limite
}

// Valida el cuerpo del PUT como el backend: primero la forma (nombre, apellidos y, en el vendedor, nombre comercial y
// categoría), después el teléfono, la mayoría de edad del cliente y, por último, que el nombre comercial sea único.
// Devuelve la respuesta de error o null si todo es correcto.
function rechazarSiEsInvalido(usuario, cuerpo) {
  if (!textoValido(cuerpo?.nombre) || !textoValido(cuerpo?.apellidos)) return respuestaDeError(400, 'VALIDATION_ERROR')
  if (usuario.rol === 'VENDEDOR' && (!textoValido(cuerpo.nombreComercial) || !cuerpo.categoriaPrincipalId)) {
    return respuestaDeError(400, 'VALIDATION_ERROR')
  }
  if (cuerpo.telefono && !/^[0-9]{9}$/.test(cuerpo.telefono)) return respuestaDeError(400, 'INVALID_PHONE')
  if (usuario.rol === 'CLIENTE' && !(cuerpo.fechaNacimiento && esMayorDeEdad(cuerpo.fechaNacimiento))) {
    return respuestaDeError(400, cuerpo.fechaNacimiento ? 'UNDERAGE' : 'VALIDATION_ERROR')
  }
  if (usuario.rol === 'VENDEDOR') {
    const repetido = USUARIOS.some(
      (otro) => otro.id !== usuario.id && perfilDe(otro).nombreComercial === cuerpo.nombreComercial.trim(),
    )
    if (repetido) return respuestaDeError(409, 'COMMERCIAL_NAME_ALREADY_REGISTERED')
  }
  return null
}

// Como el backend (PR #140): solo el cliente y el vendedor tienen perfil; el usuario sale de la sesión.
export const profileHandlers = [
  http.get('/api/perfil', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE' && usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(perfilDe(usuario))
  }),

  // Editar el perfil propio (HU45, PR #239): el PUT sustituye todos los datos modificables del rol. Sin sesión 401 y
  // el administrador 403. El email, el DNI, el tipo de cliente y el rol no se editan y se ignoran. Responde 200 con el
  // perfil actualizado, con la misma forma que el GET.
  http.put('/api/perfil', async ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol === 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })

    const cuerpo = await request.json()
    const rechazo = rechazarSiEsInvalido(usuario, cuerpo)
    if (rechazo) return rechazo

    const comunes = {
      nombre: cuerpo.nombre.trim(),
      apellidos: cuerpo.apellidos.trim(),
      avatar: cuerpo.avatar || undefined,
      telefono: cuerpo.telefono || undefined,
    }
    const propios =
      usuario.rol === 'CLIENTE'
        ? { fechaNacimiento: cuerpo.fechaNacimiento }
        : { nombreComercial: cuerpo.nombreComercial.trim(), categoriaPrincipalId: cuerpo.categoriaPrincipalId }
    ediciones.set(usuario.id, { ...comunes, ...propios })
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
