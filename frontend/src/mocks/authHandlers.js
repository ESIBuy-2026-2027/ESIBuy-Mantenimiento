import { http, HttpResponse } from 'msw'

const CLAVE_VALIDA = 'Mock-Clave-1!'
const CODIGO_EMAIL = '654321'
const CODIGO_AUTENTICADOR = '123456'
// Código de recuperación de contraseña (HU3) que el mock da por bueno para cualquier cuenta
const CODIGO_RECUPERACION = '112233'
const MAX_INTENTOS_FALLIDOS = 5
// Imagen de 1x1 píxel: en los mocks basta con una imagen PNG cualquiera para el QR del alta (T20)
const QR_DE_PRUEBA = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=='

export const USUARIOS = [
  { id: 'u-1', nombre: 'Ada', apellidos: 'Admin', email: 'admin@esibuy.test', rol: 'ADMINISTRADOR', estado: 'ACTIVO' },
  { id: 'u-2', nombre: 'Vera', apellidos: 'Vendedora', email: 'vendedor@esibuy.test', rol: 'VENDEDOR', estado: 'ACTIVO' },
  { id: 'u-3', nombre: 'Carlos', apellidos: 'Cliente', email: 'cliente@esibuy.test', rol: 'CLIENTE', estado: 'ACTIVO' },
  { id: 'u-5', nombre: 'Vicente', apellidos: 'Vacío', email: 'vendedor-vacio@esibuy.test', rol: 'VENDEDOR', estado: 'ACTIVO' },
  { id: 'u-4', nombre: 'Dani', apellidos: 'Bloqueado', email: 'bloqueado@esibuy.test', rol: 'CLIENTE', estado: 'BLOQUEADO' },
  { id: 'u-6', nombre: 'Paula', apellidos: 'Pendiente', email: 'pendiente@esibuy.test', rol: 'CLIENTE', estado: 'PENDIENTE' },
  // Sin Microsoft Authenticator todavía: tras el código del correo pasa al alta con QR (T20)
  { id: 'u-7', nombre: 'Nora', apellidos: 'Nueva', email: 'nuevo@esibuy.test', rol: 'CLIENTE', estado: 'ACTIVO', sinAuthenticator: true },
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

    if (usuario.estado === 'PENDIENTE') {
      return HttpResponse.json({ code: 'ACCOUNT_PENDING' }, { status: 403 })
    }

    if (usuario.estado !== 'ACTIVO') {
      return HttpResponse.json({ code: 'ACCOUNT_BLOCKED' }, { status: 403 })
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

    if (estado.usuarioPendiente.sinAuthenticator) {
      estado.paso = 'enrollAuthenticator'
      return HttpResponse.json({ nextStep: 'enrollAuthenticator' })
    }

    estado.paso = 'authenticator'
    return HttpResponse.json({ nextStep: 'authenticator' })
  }),

  http.post('/api/auth/enroll-authenticator', () => {
    if (estado.paso !== 'enrollAuthenticator') {
      return sinAutorizacion()
    }
    return HttpResponse.json({ qr: QR_DE_PRUEBA })
  }),

  http.post('/api/auth/verify-authenticator-code', async ({ request }) => {
    const { code } = await request.json()
    const enPasoTres = estado.paso === 'authenticator' || estado.paso === 'enrollAuthenticator'
    if (!enPasoTres || code !== CODIGO_AUTENTICADOR) {
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

  // Como el backend (T399): pedir el código responde siempre 204, exista o no la cuenta
  http.post('/api/auth/password-recovery', () => new HttpResponse(null, { status: 204 })),

  http.post('/api/auth/password-recovery/confirm', async ({ request }) => {
    const { email, code } = await request.json()
    const existe = USUARIOS.some((usuario) => usuario.email === email)
    return existe && code === CODIGO_RECUPERACION ? new HttpResponse(null, { status: 204 }) : sinAutorizacion()
  }),

  http.post('/api/auth/logout', () => {
    estado = nuevoEstado()
    return new HttpResponse(null, { status: 204 })
  }),
]
