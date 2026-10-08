import { http, HttpResponse } from 'msw'

const CLAVE_VALIDA = 'Mock-Clave-1!'
const CODIGO_EMAIL = '654321'
const CODIGO_AUTENTICADOR = '123456'
const MAX_INTENTOS_FALLIDOS = 5

export const USUARIOS = [
  { id: 'u-1', nombre: 'Ada', apellidos: 'Admin', email: 'admin@esibuy.test', rol: 'ADMINISTRADOR', estado: 'ACTIVO' },
  { id: 'u-2', nombre: 'Vera', apellidos: 'Vendedora', email: 'vendedor@esibuy.test', rol: 'VENDEDOR', estado: 'ACTIVO' },
  { id: 'u-3', nombre: 'Carlos', apellidos: 'Cliente', email: 'cliente@esibuy.test', rol: 'CLIENTE', estado: 'ACTIVO' },
  { id: 'u-5', nombre: 'Vicente', apellidos: 'Vacío', email: 'vendedor-vacio@esibuy.test', rol: 'VENDEDOR', estado: 'ACTIVO' },
  { id: 'u-4', nombre: 'Dani', apellidos: 'Desactivado', email: 'desactivado@esibuy.test', rol: 'CLIENTE', estado: 'DESACTIVADO' },
]

const nuevoEstado = () => ({
  intentosFallidos: 0,
  paso: 'credenciales',
  usuarioPendiente: null,
  usuarioEnSesion: null,
})

let estado = nuevoEstado()

export function resetAuthMock() {
  estado = nuevoEstado()
}

export function getUsuarioEnSesion() {
  return estado.usuarioEnSesion
}

const sinAutorizacion = () => new HttpResponse(null, { status: 401 })

export const authHandlers = [
  http.post('/api/auth/login', async ({ request }) => {
    if (estado.intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
      return new HttpResponse(null, { status: 429 })
    }

    const { email, password } = await request.json()
    const usuario = USUARIOS.find((candidato) => candidato.email === email)

    if (!usuario || password !== CLAVE_VALIDA) {
      estado.intentosFallidos += 1
      return sinAutorizacion()
    }

    if (usuario.estado !== 'ACTIVO') {
      return HttpResponse.json({ code: 'ACCOUNT_DISABLED' }, { status: 403 })
    }

    estado.intentosFallidos = 0
    estado.usuarioPendiente = usuario
    estado.paso = 'emailCode'
    return HttpResponse.json({ nextStep: 'emailCode' })
  }),

  http.post('/api/auth/verify-email-code', async ({ request }) => {
    const { code } = await request.json()
    if (estado.paso !== 'emailCode' || code !== CODIGO_EMAIL) {
      return sinAutorizacion()
    }

    estado.paso = 'authenticator'
    return HttpResponse.json({ nextStep: 'authenticator' })
  }),

  http.post('/api/auth/verify-authenticator-code', async ({ request }) => {
    const { code } = await request.json()
    if (estado.paso !== 'authenticator' || code !== CODIGO_AUTENTICADOR) {
      return sinAutorizacion()
    }

    estado.usuarioEnSesion = estado.usuarioPendiente
    estado.usuarioPendiente = null
    estado.paso = 'credenciales'
    return HttpResponse.json(estado.usuarioEnSesion)
  }),

  http.get('/api/auth/me', () =>
    estado.usuarioEnSesion ? HttpResponse.json(estado.usuarioEnSesion) : sinAutorizacion(),
  ),

  http.post('/api/auth/logout', () => {
    estado = nuevoEstado()
    return new HttpResponse(null, { status: 204 })
  }),
]
