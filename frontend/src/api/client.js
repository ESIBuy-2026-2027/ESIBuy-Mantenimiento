const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(status, message, code = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

async function readErrorCode(response) {
  try {
    const data = await response.json()
    return typeof data?.code === 'string' ? data.code : null
  } catch {
    return null
  }
}

// Protección CSRF (T230, D4): el backend deja el token en la cookie XSRF-TOKEN (legible desde
// JavaScript, a diferencia de la de sesión) y espera recibirlo en esta cabecera en cada petición que
// cambia datos. Una web maliciosa no puede leer las cookies de ESIBuy, así que no puede copiarlo.
const COOKIE_CSRF = 'XSRF-TOKEN'
const CABECERA_CSRF = 'X-XSRF-TOKEN'
const METODOS_SIN_CSRF = new Set(['GET', 'HEAD', 'OPTIONS'])

function leerTokenCsrf() {
  const cookie = document.cookie
    .split('; ')
    .find((par) => par.startsWith(`${COOKIE_CSRF}=`))
  return cookie ? decodeURIComponent(cookie.slice(COOKIE_CSRF.length + 1)) : null
}

function construirCabeceras(method, body) {
  const headers = {}
  if (body) {
    headers['Content-Type'] = 'application/json'
  }
  const tokenCsrf = METODOS_SIN_CSRF.has(method) ? null : leerTokenCsrf()
  if (tokenCsrf) {
    headers[CABECERA_CSRF] = tokenCsrf
  }
  return Object.keys(headers).length > 0 ? headers : undefined
}

export async function request(path, { method = 'GET', body } = {}) {
  const url = new URL(`${BASE_URL}${path}`, window.location.origin)
  const response = await fetch(url, {
    method,
    credentials: 'include',
    headers: construirCabeceras(method, body),
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(
      response.status,
      `Error ${response.status} en ${method} ${path}`,
      await readErrorCode(response),
    )
  }

  // Una respuesta sin cuerpo (204, o un 201 como el del registro) no es un error: devuelve null
  const texto = await response.text()
  return texto ? JSON.parse(texto) : null
}
