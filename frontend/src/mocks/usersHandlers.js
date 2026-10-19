import { http, HttpResponse } from 'msw'
import { buscarVendedorMock, editarVendedorMock, vendedoresMock } from './adminSellersHandlers'
import { getUsuarioEnSesion } from './authHandlers'
import { esMayorDeEdadMock } from './fechas'
import { CATEGORIAS } from './productsHandlers'

const TAMANO_PAGINA_POR_DEFECTO = 10

// [nombre, apellidos, rol, estado, fechaAlta]. 28 usuarios (3 páginas de 10). La búsqueda "ana" coincide
// solo con Ana López García y Mariana Ruiz Pascual; ningún otro nombre, apellido o email contiene "ana".
// Toda cuenta nueva queda pendiente hasta que un administrador la verifica (HU1): los tres últimos son
// un cliente, otro cliente y un administrador pendientes. Van al final para no cambiar los ids de los demás.
const USUARIOS_INICIALES = [
  ['Ana', 'López García', 'ADMINISTRADOR', 'ACTIVO', '2026-01-12T09:15:00'],
  ['Mariana', 'Ruiz Pascual', 'VENDEDOR', 'PENDIENTE', '2026-10-01T10:00:00'],
  ['Carlos', 'Moreno Gil', 'CLIENTE', 'ACTIVO', '2026-02-03T11:30:00'],
  ['Lucía', 'Torres Vega', 'CLIENTE', 'BLOQUEADO', '2026-09-28T17:45:00'],
  ['Javier', 'Romero Díaz', 'VENDEDOR', 'ACTIVO', '2026-03-14T08:20:00'],
  ['Elena', 'Muñoz Alonso', 'CLIENTE', 'BLOQUEADO', '2026-04-02T12:00:00'],
  ['Pablo', 'Gómez Martín', 'CLIENTE', 'ACTIVO', '2026-04-18T16:10:00'],
  ['Marta', 'Sanz Ramos', 'VENDEDOR', 'BLOQUEADO', '2026-05-06T10:40:00'],
  ['Sergio', 'Castro Ortega', 'CLIENTE', 'ACTIVO', '2026-05-21T19:05:00'],
  ['Laura', 'Rubio Molina', 'CLIENTE', 'BLOQUEADO', '2026-09-30T09:00:00'],
  ['Diego', 'Delgado Iglesias', 'ADMINISTRADOR', 'BLOQUEADO', '2026-01-20T13:25:00'],
  ['Paula', 'Medina Cruz', 'CLIENTE', 'ACTIVO', '2026-06-09T15:50:00'],
  ['Hugo', 'Herrera Blanco', 'VENDEDOR', 'BLOQUEADO', '2026-06-25T11:15:00'],
  ['Irene', 'Prieto Vidal', 'CLIENTE', 'ACTIVO', '2026-07-03T18:30:00'],
  ['Álvaro', 'Campos Serrano', 'CLIENTE', 'BLOQUEADO', '2026-07-11T08:45:00'],
  ['Nuria', 'Navarro Lozano', 'VENDEDOR', 'ACTIVO', '2026-07-19T14:20:00'],
  ['Raúl', 'Ortiz Fuentes', 'CLIENTE', 'ACTIVO', '2026-07-27T10:05:00'],
  ['Sofía', 'Cabrera Soler', 'CLIENTE', 'BLOQUEADO', '2026-09-15T12:35:00'],
  ['Iván', 'Morales Reyes', 'VENDEDOR', 'ACTIVO', '2026-08-04T09:55:00'],
  ['Claudia', 'Peña Calvo', 'CLIENTE', 'ACTIVO', '2026-08-12T17:00:00'],
  ['Óscar', 'León Garrido', 'CLIENTE', 'BLOQUEADO', '2026-08-20T13:40:00'],
  ['Beatriz', 'Vargas Méndez', 'VENDEDOR', 'PENDIENTE', '2026-09-05T11:10:00'],
  ['Rubén', 'Cortés Pastor', 'CLIENTE', 'ACTIVO', '2026-08-28T16:25:00'],
  ['Jorge', 'Benítez Sáez', 'CLIENTE', 'BLOQUEADO', '2026-09-10T15:15:00'],
  ['Gonzalo', 'Ibáñez Roca', 'CLIENTE', 'PENDIENTE', '2026-10-06T18:20:00'],
  ['Marcos', 'Aguilar Bravo', 'CLIENTE', 'PENDIENTE', '2026-10-08T09:40:00'],
  ['Manuel', 'Esteve Pons', 'ADMINISTRADOR', 'ACTIVO', '2026-10-04T12:05:00'],
]
  .map(([nombre, apellidos, rol, estado, fechaAlta], indice) => ({
    id: `usr-${indice + 1}`,
    nombre,
    apellidos,
    email: `${normalizar(nombre)}.${normalizar(apellidos.split(' ')[0])}@esibuy.test`,
    rol,
    estado,
    fechaAlta,
  }))
  // El administrador con el que se inicia sesión en el mock (mismo id que en authHandlers), para que
  // la regla "un administrador no puede bloquearse a sí mismo" (HU23) se vea en la lista.
  .concat({
    id: 'u-1',
    nombre: 'Ada',
    apellidos: 'Admin',
    email: 'admin@esibuy.test',
    rol: 'ADMINISTRADOR',
    estado: 'ACTIVO',
    fechaAlta: '2026-02-27T10:30:00',
  })
  .sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))

// Lista que puede crecer durante la sesión (p. ej. con los administradores dados de alta en la HU21).
let usuarios = [...USUARIOS_INICIALES]
// Datos de la ficha que el administrador ha modificado durante la sesión (HU22), por id de usuario.
let detallesModificados = new Map()

export function resetUsersMock() {
  usuarios = [...USUARIOS_INICIALES]
  detallesModificados = new Map()
}

export const existeEmailDeUsuario = (email) => usuarios.some((usuario) => usuario.email === email)

// Añade un usuario al principio: el listado va del alta más reciente a la más antigua.
export function agregarUsuario(usuario) {
  usuarios = [usuario, ...usuarios]
}

function normalizar(texto) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

// Con searchBy=name (listados de clientes y administradores) no se busca en el email.
const coincideBusqueda = (usuario, busqueda, soloNombre) =>
  [usuario.nombre, usuario.apellidos, ...(soloNombre ? [] : [usuario.email])].some((campo) =>
    normalizar(campo).includes(busqueda),
  )

const ORDEN_ESTADOS = ['PENDIENTE', 'ACTIVO', 'BLOQUEADO']
const nombreCompleto = (usuario) => `${usuario.nombre} ${usuario.apellidos}`

const COMPARADORES = new Map([
  ['name', (a, b) => nombreCompleto(a).localeCompare(nombreCompleto(b), 'es')],
  [
    'status',
    (a, b) =>
      ORDEN_ESTADOS.indexOf(a.estado) - ORDEN_ESTADOS.indexOf(b.estado) ||
      nombreCompleto(a).localeCompare(nombreCompleto(b), 'es'),
  ],
])

// Sin sort se mantiene el orden por fecha de alta, de la más reciente a la más antigua (HU19).
function ordenar(lista, criterio, sentido) {
  const comparar = COMPARADORES.get(criterio)
  if (!comparar) return lista
  const signo = sentido === 'desc' ? -1 : 1
  return [...lista].sort((a, b) => signo * comparar(a, b))
}

const ESTADOS_ADMITIDOS = new Set(['ACTIVO', 'BLOQUEADO'])

// Datos de la ficha (HU20) que no salen en el listado. Son ficticios y se calculan a partir del id para
// que cada usuario tenga siempre los mismos. Nunca incluyen la contraseña.
const LETRAS_DNI = 'TRWAGMYFPDXBNJZSQVHLCKE'
const SEDES = ['Ciudad Real', 'Toledo', 'Albacete', 'Cuenca']

function detalleDe(usuario) {
  const n = Number(usuario.id.replace(/\D/g, ''))
  const avatar = `avatar${1 + (n % 3)}`

  if (usuario.rol === 'CLIENTE') {
    const numeroDni = 10000000 + n * 1373
    return {
      dni: `${numeroDni}${LETRAS_DNI[numeroDni % 23]}`,
      telefono: `6${String(20000000 + n * 7919).slice(0, 8)}`,
      fechaNacimiento: `${1975 + (n % 25)}-0${1 + (n % 9)}-1${n % 9}`,
      tipoCliente: n % 3 === 0 ? 'PREMIUM' : 'ESTANDAR',
      avatar,
    }
  }
  if (usuario.rol === 'ADMINISTRADOR') {
    return { sede: SEDES[n % SEDES.length], fechaIncorporacion: usuario.fechaAlta.slice(0, 10), avatar }
  }
  return {}
}

function detalleActual(usuario) {
  const detalle = { ...usuario, ...detalleDe(usuario), ...detallesModificados.get(usuario.id) }
  return detalle.categoriaPrincipalId
    ? { ...detalle, categoriaPrincipal: CATEGORIAS.get(detalle.categoriaPrincipalId) }
    : detalle
}

// Campos de la ficha que se pueden modificar según el rol (aparte del nombre y los apellidos).
const CAMPOS_EDITABLES = new Map([
  ['CLIENTE', ['dni', 'telefono', 'fechaNacimiento', 'tipoCliente', 'avatar']],
  ['ADMINISTRADOR', ['sede', 'avatar']],
  ['VENDEDOR', ['nombreComercial', 'dni', 'telefono', 'categoriaPrincipalId', 'avatar']],
])

const hayTexto = (valor) => typeof valor === 'string' && valor.trim() !== ''

// El backend real también valida: lo mínimo que el formulario ya comprueba, según el rol de la cuenta.
function datosValidos(rol, datos) {
  if (!hayTexto(datos.nombre) || !hayTexto(datos.apellidos)) return false
  // El DNI y el teléfono (opcional) siguen las mismas reglas en el cliente y en el vendedor.
  const dniYTelefonoValidos = /^\d{8}[A-Za-z]$/.test(datos.dni ?? '') && (!datos.telefono || /^\d{9}$/.test(datos.telefono))
  if (rol === 'CLIENTE') return dniYTelefonoValidos && esMayorDeEdadMock(datos.fechaNacimiento)
  if (rol === 'VENDEDOR') {
    return dniYTelefonoValidos && hayTexto(datos.nombreComercial) && CATEGORIAS.has(datos.categoriaPrincipalId)
  }
  return true
}

// Código de error si el nombre comercial ya es de otro vendedor, o null si está libre. El email no se
// comprueba: no se puede cambiar.
function conflictoDeVendedor(idCuenta, { nombreComercial }) {
  const repetido = vendedoresMock().some(
    (vendedor) => vendedor.id !== idCuenta && vendedor.nombreComercial.toLowerCase() === nombreComercial.trim().toLowerCase(),
  )
  return repetido ? 'COMMERCIAL_NAME_ALREADY_REGISTERED' : null
}

const esAdminActivo = (usuario) => usuario.rol === 'ADMINISTRADOR' && usuario.estado === 'ACTIVO'

// Código de error si la cuenta no se puede bloquear, o null si se puede.
function conflictoAlBloquear(usuario) {
  if (usuario.id === getUsuarioEnSesion()?.id) return 'CANNOT_BLOCK_SELF'
  if (esAdminActivo(usuario) && usuarios.filter(esAdminActivo).length === 1) return 'LAST_ACTIVE_ADMIN'
  return null
}

// Código de error si la cuenta no se puede eliminar, o null si se puede (mismas reglas que para bloquear).
function conflictoAlEliminar(usuario) {
  if (usuario.id === getUsuarioEnSesion()?.id) return 'CANNOT_DELETE_SELF'
  if (esAdminActivo(usuario) && usuarios.filter(esAdminActivo).length === 1) return 'LAST_ACTIVE_ADMIN'
  return null
}

// Devuelve la respuesta de error si quien pregunta no es administrador, o null si puede continuar.
function rechazarSiNoEsAdmin() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })
  return null
}

export const usersHandlers = [
  http.get('/api/admin/usuarios', ({ request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const rol = params.get('role')
    const estado = params.get('status')
    const soloNombre = params.get('searchBy') === 'name'
    const pagina = Math.max(1, Number(params.get('page')) || 1)
    const tamano = Math.max(1, Number(params.get('size')) || TAMANO_PAGINA_POR_DEFECTO)

    const coincidentes = ordenar(
      usuarios.filter(
        (candidato) =>
          coincideBusqueda(candidato, busqueda, soloNombre) &&
          (!rol || candidato.rol === rol) &&
          (!estado || candidato.estado === estado),
      ),
      params.get('sort'),
      params.get('order'),
    )
    const inicio = (pagina - 1) * tamano

    return HttpResponse.json({
      items: coincidentes.slice(inicio, inicio + tamano),
      page: pagina,
      size: tamano,
      totalItems: coincidentes.length,
      totalPages: Math.ceil(coincidentes.length / tamano),
    })
  }),

  // Ficha de un usuario (HU20).
  http.get('/api/admin/usuarios/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const usuario = usuarios.find((candidato) => candidato.id === params.id)
    return usuario ? HttpResponse.json(detalleActual(usuario)) : new HttpResponse(null, { status: 404 })
  }),

  // Modificar los datos de CUALQUIER usuario (HU22): cliente, administrador o vendedor se editan con el mismo
  // endpoint. El email, la contraseña y el rol no se tocan desde aquí: el rol de una cuenta no cambia nunca
  // ("el que nace cliente muere cliente") y, como en el backend, se ignora aunque llegue en el cuerpo.
  http.put('/api/admin/usuarios/:id', async ({ params, request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const vendedor = buscarVendedorMock(params.id)
    const existente = vendedor ?? usuarios.find((candidato) => candidato.id === params.id)
    if (!existente) return new HttpResponse(null, { status: 404 })

    const datos = await request.json()
    const rol = vendedor ? 'VENDEDOR' : existente.rol
    if (!datosValidos(rol, datos)) return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })

    if (vendedor) {
      const conflicto = conflictoDeVendedor(existente.id, { nombreComercial: datos.nombreComercial })
      if (conflicto) return HttpResponse.json({ code: conflicto }, { status: 409 })
      editarVendedorMock(existente.id, datos)
    }

    const { nombre, apellidos, ...resto } = datos
    const detalle = Object.fromEntries(
      CAMPOS_EDITABLES.get(rol)
        .filter((campo) => campo in resto)
        .map((campo) => [campo, resto[campo]]),
    )
    usuarios = usuarios.map((candidato) =>
      candidato.id === existente.id ? { ...candidato, nombre, apellidos } : candidato,
    )
    detallesModificados.set(existente.id, { ...detallesModificados.get(existente.id), ...detalle })

    const actualizado = usuarios.find((candidato) => candidato.id === existente.id) ?? {
      id: existente.id,
      nombre,
      apellidos,
      email: existente.email,
      rol,
      estado: existente.estado,
      fechaAlta: existente.fechaAlta,
    }
    return HttpResponse.json(detalleActual(actualizado))
  }),

  // Activar (HU24) o bloquear (antes desactivar, HU23) una cuenta. Un administrador no puede bloquearse a sí mismo
  // ni bloquear al último administrador activo.
  http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const { estado } = await request.json()
    if (!ESTADOS_ADMITIDOS.has(estado)) return new HttpResponse(null, { status: 400 })

    const existente = usuarios.find((candidato) => candidato.id === params.id)
    if (!existente) return new HttpResponse(null, { status: 404 })

    if (estado === 'BLOQUEADO') {
      const conflicto = conflictoAlBloquear(existente)
      if (conflicto) return HttpResponse.json({ code: conflicto }, { status: 409 })
    }

    const actualizado = { ...existente, estado }
    usuarios = usuarios.map((candidato) => (candidato.id === params.id ? actualizado : candidato))
    return HttpResponse.json(actualizado)
  }),

  // Eliminar una cuenta (contrato propuesto por el frontend, AB#249). Un administrador no puede eliminarse a sí
  // mismo ni eliminar al último administrador activo.
  http.delete('/api/admin/usuarios/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const existente = usuarios.find((candidato) => candidato.id === params.id)
    if (!existente) return new HttpResponse(null, { status: 404 })

    const conflicto = conflictoAlEliminar(existente)
    if (conflicto) return HttpResponse.json({ code: conflicto }, { status: 409 })

    usuarios = usuarios.filter((candidato) => candidato.id !== params.id)
    detallesModificados.delete(params.id)
    return new HttpResponse(null, { status: 204 })
  }),
]
