import { http, HttpResponse } from 'msw'
import { USUARIOS, getUsuarioEnSesion } from './authHandlers'
import { paginar } from './paginar'

const ASUNTO_MAXIMO = 100
const DESCRIPCION_MAXIMA = 1000
const RESPUESTA_MAXIMA = 1000
const TAMANO_MAXIMO = 50

const fecha = (dia) => `2026-10-${String(dia).padStart(2, '0')}T10:15:30Z`

const incidencia = (n, asunto, descripcion, estado = 'PENDIENTE', respuesta = '') => ({
  id: `inc-seed-${n}`,
  asunto,
  descripcion,
  estado,
  respuesta,
  fechaCreacion: fecha(n),
})

// Incidencias de ejemplo de cada usuario (HU44), de las más antiguas a las más recientes, como las devuelve el
// backend. El cliente tiene 12 (dos páginas de 10), el vendedor 2 y el otro vendedor ninguna.
const SEMILLA = {
  'u-3': [
    incidencia(1, 'No me llega el pedido', 'Lo pedí hace una semana y sigue sin llegar. ' + 'Necesito saber dónde está. '.repeat(8)),
    incidencia(2, 'Cobro duplicado', 'Me han cobrado dos veces el mismo pedido.', 'ABIERTA', 'Estamos revisando el cobro con el banco.'),
    incidencia(3, 'No puedo cambiar mi dirección', 'El formulario de direcciones no guarda los cambios.'),
    incidencia(4, 'Producto dañado', 'La lámpara llegó con la pantalla rota.', 'ABIERTA', 'Te enviaremos una nueva sin coste.'),
    incidencia(5, 'Error al pagar con tarjeta', 'El pago se queda cargando y no termina.'),
    incidencia(6, 'Factura incorrecta', 'El nombre de la factura no es el mío.'),
    incidencia(7, 'No encuentro mi pedido', 'No aparece en el historial el pedido de ayer.'),
    incidencia(8, 'Cupón que no funciona', 'El cupón de bienvenida no se aplica.'),
    incidencia(9, 'Consulta sobre devoluciones', '¿Cuántos días tengo para devolver un producto?', 'ABIERTA', 'Dispones de 14 días desde la entrega.'),
    incidencia(10, 'Imagen que no carga', 'En la ficha de un producto la imagen no se ve.'),
    incidencia(11, 'Valoración que no se publica', 'He valorado un producto y no aparece mi valoración.'),
    incidencia(12, 'Aviso de stock', 'Quiero que me avisen cuando haya stock de un producto.'),
  ],
  'u-2': [
    incidencia(13, 'No puedo subir la imagen de un producto', 'Al guardar el producto con una imagen nueva da error.', 'ABIERTA', 'Ya lo hemos corregido, prueba de nuevo.'),
    incidencia(14, 'Estadísticas sin datos', 'La pantalla de estadísticas sale vacía desde ayer.'),
  ],
  // De una cuenta que ya se ha eliminado (HU25, HU47): sus incidencias se quedan, y el administrador las ve sin
  // autor (HU49). Es la más reciente de todas.
  'u-eliminado': [
    incidencia(15, 'Producto que no existe', 'Aparece en la búsqueda pero no se puede abrir.'),
  ],
}

let almacen = nuevoAlmacen()
let siguienteId = 1

function nuevoAlmacen() {
  return new Map(Object.entries(SEMILLA).map(([usuarioId, lista]) => [usuarioId, lista.map((item) => ({ ...item }))]))
}

export function resetIncidentsMock() {
  almacen = nuevoAlmacen()
  siguienteId = 1
}

const incidenciasDe = (usuario) => {
  if (!almacen.has(usuario.id)) almacen.set(usuario.id, [])
  return almacen.get(usuario.id)
}

// Solo cliente y vendedor: sin sesión 401 y el administrador 403, como el backend.
const rechazarSiNoEsClienteNiVendedor = () => {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol === 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })
  return null
}

// Solo el administrador: sin sesión 401 y el cliente o el vendedor 403, como el backend (HU49).
const rechazarSiNoEsAdministrador = () => {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })
  return null
}

// Las incidencias de todos, de las más antiguas a las más recientes (por fecha y después por id), cada una con el
// correo y el rol de su autor; null si la cuenta ya no existe.
const incidenciasDeTodos = () =>
  [...almacen.entries()]
    .flatMap(([usuarioId, lista]) => {
      const usuario = USUARIOS.find(({ id }) => id === usuarioId)
      const autor = usuario ? { email: usuario.email, rol: usuario.rol } : null
      return lista.map((item) => ({ ...item, autor }))
    })
    .sort((a, b) => a.fechaCreacion.localeCompare(b.fechaCreacion) || a.id.localeCompare(b.id))

// Lee page y size como el backend: 400 sin cuerpo si no son números (el error por defecto de Spring) y 400
// VALIDATION_ERROR si están fuera de rango. Devuelve la respuesta de error o la página pedida.
function leerPaginacion(request) {
  const params = new URL(request.url).searchParams
  const noEsNumero = ['page', 'size'].some((nombre) => params.has(nombre) && !/^-?\d+$/.test(params.get(nombre)))
  if (noEsNumero) return { error: new HttpResponse(null, { status: 400 }) }

  const pagina = params.has('page') ? Number(params.get('page')) : 1
  const tamano = params.has('size') ? Number(params.get('size')) : 10
  if (pagina < 1 || tamano < 1 || tamano > TAMANO_MAXIMO) return { error: incidenciaInvalida() }
  return { pagina, tamano }
}

const incidenciaInvalida = () =>
  HttpResponse.json({ mensaje: 'La incidencia es inválida', code: 'VALIDATION_ERROR' }, { status: 400 })

const incidenciaNoEncontrada = () =>
  HttpResponse.json({ mensaje: 'La incidencia no existe', code: 'INCIDENT_NOT_FOUND' }, { status: 404 })

// La lista que guarda la incidencia con ese id y su posición en ella, o null si no existe (HU50)
function buscarIncidencia(id) {
  for (const lista of almacen.values()) {
    const posicion = lista.findIndex((item) => item.id === id)
    if (posicion >= 0) return { lista, posicion }
  }
  return null
}

const textoValido = (valor, maximo) => {
  if (typeof valor !== 'string') return false
  const recortado = valor.trim()
  return recortado.length > 0 && recortado.length <= maximo
}

export const incidentsHandlers = [
  // Las incidencias del usuario en sesión, de las más antiguas a las más recientes, por páginas (page desde 1, size de
  // 1 a 50, por defecto 10). Una página o un tamaño fuera de rango responde 400 VALIDATION_ERROR y uno que no es un
  // número, 400 sin cuerpo (el error por defecto de Spring).
  http.get('/api/perfil/incidencias', ({ request }) => {
    const rechazo = rechazarSiNoEsClienteNiVendedor()
    if (rechazo) return rechazo

    const { error, pagina, tamano } = leerPaginacion(request)
    if (error) return error

    return HttpResponse.json(paginar(incidenciasDe(getUsuarioEnSesion()), { pagina, tamano }))
  }),

  // Supervisión del administrador (HU49): las incidencias de todos con su autor, por páginas como las del perfil.
  http.get('/api/admin/incidencias', ({ request }) => {
    const rechazo = rechazarSiNoEsAdministrador()
    if (rechazo) return rechazo

    const { error, pagina, tamano } = leerPaginacion(request)
    if (error) return error

    return HttpResponse.json(paginar(incidenciasDeTodos(), { pagina, tamano }))
  }),

  // Crea una incidencia del usuario en sesión: nace PENDIENTE, sin respuesta y con los textos sin espacios en los
  // extremos. Nunca se lee un autor del cuerpo.
  http.post('/api/perfil/incidencias', async ({ request }) => {
    const rechazo = rechazarSiNoEsClienteNiVendedor()
    if (rechazo) return rechazo

    const { asunto, descripcion } = await request.json()
    if (!textoValido(asunto, ASUNTO_MAXIMO) || !textoValido(descripcion, DESCRIPCION_MAXIMA)) {
      return incidenciaInvalida()
    }

    const nueva = {
      id: `inc-${siguienteId}`,
      asunto: asunto.trim(),
      descripcion: descripcion.trim(),
      estado: 'PENDIENTE',
      respuesta: '',
      fechaCreacion: new Date().toISOString(),
    }
    siguienteId += 1
    incidenciasDe(getUsuarioEnSesion()).push(nueva)
    return HttpResponse.json(nueva, { status: 201 })
  }),

  // Abre una incidencia pendiente con la respuesta del administrador (HU50), en el orden del backend: un cuerpo que no
  // es JSON, 400 sin cuerpo; si no existe, 404; si ya está abierta, 409; y una respuesta vacía o de más de 1000
  // caracteres, 400 VALIDATION_ERROR. Responde la incidencia abierta, sin autor.
  http.patch('/api/admin/incidencias/:id/abrir', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsAdministrador()
    if (rechazo) return rechazo

    let cuerpo
    try {
      cuerpo = await request.json()
    } catch {
      return new HttpResponse(null, { status: 400 })
    }

    const encontrada = buscarIncidencia(params.id)
    if (!encontrada) return incidenciaNoEncontrada()
    const { lista, posicion } = encontrada
    if (lista[posicion].estado !== 'PENDIENTE') {
      return HttpResponse.json({ mensaje: 'La incidencia ya está abierta', code: 'INCIDENT_ALREADY_OPEN' }, { status: 409 })
    }
    if (!textoValido(cuerpo?.respuesta, RESPUESTA_MAXIMA)) return incidenciaInvalida()

    const abierta = { ...lista[posicion], estado: 'ABIERTA', respuesta: cuerpo.respuesta.trim() }
    lista[posicion] = abierta
    return HttpResponse.json(abierta)
  }),

  // Cierra una incidencia, pendiente o abierta (HU50): se elimina y responde 204 sin cuerpo; si no existe, 404.
  http.delete('/api/admin/incidencias/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsAdministrador()
    if (rechazo) return rechazo

    const encontrada = buscarIncidencia(params.id)
    if (!encontrada) return incidenciaNoEncontrada()
    encontrada.lista.splice(encontrada.posicion, 1)
    return new HttpResponse(null, { status: 204 })
  }),
]
