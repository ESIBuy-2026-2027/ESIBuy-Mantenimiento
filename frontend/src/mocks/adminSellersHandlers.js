import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { leerPagina, normalizar, paginar } from './paginar'
import { CATEGORIAS, OTRO_VENDEDOR, PRODUCTOS, VENDEDOR_CON_PRODUCTOS, aRespuesta } from './productsHandlers'

const VENDEDORES_DATOS = [
  // [id, nombre, apellidos, nombreComercial, email, dni, categoría principal, estado]
  [VENDEDOR_CON_PRODUCTOS, 'Vera', 'Vendedora', 'TecnoVera', 'vendedor@esibuy.test', '12345601V', 'cat-1', 'ACTIVO'],
  ['u-5', 'Vicente', 'Vacío', 'Casa Vicente', 'vendedor-vacio@esibuy.test', '12345602V', 'cat-2', 'ACTIVO'],
  [OTRO_VENDEDOR, 'Otto', 'Otero', 'Otero Hogar y Tecnología', 'otto@esibuy.test', '12345603V', 'cat-1', 'ACTIVO'],
  ['s-04', 'Marta', 'Gil Prieto', 'Electro Marta', 'marta@esibuy.test', '12345604V', 'cat-1', 'ACTIVO'],
  ['s-05', 'Hugo', 'Ramos León', 'Hogar Hugo', 'hugo@esibuy.test', '12345605V', 'cat-2', 'PENDIENTE'],
  ['s-06', 'Lucía', 'Navarro Sanz', 'Lucía Digital', 'lucia@esibuy.test', '12345606V', 'cat-1', 'ACTIVO'],
  ['s-07', 'Pablo', 'Iglesias Mora', 'Cocina Pablo', 'pablo@esibuy.test', '12345607V', 'cat-2', 'BLOQUEADO'],
  ['s-08', 'Elena', 'Soto Vidal', 'Soto Gadgets', 'elena@esibuy.test', '12345608V', 'cat-1', 'ACTIVO'],
  ['s-09', 'Raúl', 'Ortega Pons', 'Ortega Muebles', 'raul@esibuy.test', '12345609V', 'cat-2', 'ACTIVO'],
  ['s-10', 'Irene', 'Campos Ruiz', 'Irene Smart Home', 'irene@esibuy.test', '12345610V', 'cat-2', 'ACTIVO'],
  ['s-11', 'Sergio', 'Molina Vega', 'Molina Informática', 'sergio@esibuy.test', '12345611V', 'cat-1', 'PENDIENTE'],
  ['s-12', 'Noelia', 'Prado Ferrer', 'Noelia Decoración', 'noelia@esibuy.test', '12345612V', 'cat-2', 'ACTIVO'],
  ['s-13', 'Adrián', 'Bravo Cano', 'Bravo Audio', 'adrian@esibuy.test', '12345613V', 'cat-1', 'BLOQUEADO'],
  ['s-14', 'Carmen', 'Lara Duque', 'Carmen Electrodomésticos', 'carmen@esibuy.test', '12345614V', 'cat-2', 'ACTIVO'],
].map(([id, nombre, apellidos, nombreComercial, email, dni, categoria, estado]) => ({
  id,
  nombre,
  apellidos,
  nombreComercial,
  email,
  dni,
  categoria,
  estado,
}))

// [id, fecha, artículos, total, estado]. El vendedor u-2 tiene 12 (3 páginas de 5); u-99 tiene 2; el resto, ninguno.
const PEDIDOS_POR_VENDEDOR = new Map([
  [
    VENDEDOR_CON_PRODUCTOS,
    [
      ['PED-1012', '2026-10-06', 2, 94.4, 'PENDIENTE'],
      ['PED-1011', '2026-10-05', 1, 249, 'PENDIENTE'],
      ['PED-1010', '2026-10-04', 3, 112.85, 'ENVIADO'],
      ['PED-1009', '2026-10-03', 1, 59.9, 'ENVIADO'],
      ['PED-1008', '2026-10-01', 2, 168.99, 'ENTREGADO'],
      ['PED-1007', '2026-09-29', 1, 34.99, 'ENTREGADO'],
      ['PED-1006', '2026-09-27', 4, 79.8, 'ENTREGADO'],
      ['PED-1005', '2026-09-25', 1, 179, 'CANCELADO'],
      ['PED-1004', '2026-09-23', 2, 129.9, 'ENTREGADO'],
      ['PED-1003', '2026-09-21', 1, 89.5, 'ENTREGADO'],
      ['PED-1002', '2026-09-19', 1, 14.99, 'ENTREGADO'],
      ['PED-1001', '2026-09-16', 2, 53.45, 'ENTREGADO'],
    ],
  ],
  [
    OTRO_VENDEDOR,
    [
      ['PED-2002', '2026-10-02', 1, 349, 'ENVIADO'],
      ['PED-2001', '2026-09-30', 1, 129, 'ENTREGADO'],
    ],
  ],
])

const TAMANO_PAGINA_PESTANA = 5

// Estados que el administrador ha cambiado durante la sesión (activar o bloquear), por id de vendedor.
let estadosModificados = new Map()
// Datos que el administrador ha modificado durante la sesión (HU22), por id de vendedor.
let datosModificados = new Map()

// Vendedores que el administrador ha eliminado durante la sesión.
const eliminados = new Set()

export function resetAdminSellersMock() {
  estadosModificados = new Map()
  datosModificados = new Map()
  eliminados.clear()
}

const conCambios = (vendedor) => ({
  ...vendedor,
  ...datosModificados.get(vendedor.id),
  ...(estadosModificados.has(vendedor.id) && { estado: estadosModificados.get(vendedor.id) }),
})

// Funciones que usa el mock de usuarios (usersHandlers): el administrador edita, bloquea y elimina a los
// vendedores con los mismos endpoints que al resto de usuarios.
export const vendedoresMock = () => VENDEDORES_DATOS.filter((vendedor) => !eliminados.has(vendedor.id)).map(conCambios)
export const buscarVendedorMock = (id) => vendedoresMock().find((vendedor) => vendedor.id === id)

// Ficha del vendedor tal y como la devuelve GET /api/admin/usuarios/{id}: la de siempre más su rol.
export const detalleVendedorMock = (id) => {
  const vendedor = buscarVendedorMock(id)
  return vendedor && { ...aVendedorDetalle(vendedor), rol: 'VENDEDOR' }
}

export function cambiarEstadoVendedorMock(id, estado) {
  estadosModificados.set(id, estado)
  return buscarVendedorMock(id)
}

export const eliminarVendedorMock = (id) => eliminados.add(id)

export function editarVendedorMock(id, { nombre, apellidos, nombreComercial, dni, telefono, categoriaPrincipalId, avatar }) {
  datosModificados.set(id, {
    ...datosModificados.get(id),
    nombre,
    apellidos,
    nombreComercial,
    dni,
    telefono,
    categoria: categoriaPrincipalId,
    avatar,
  })
}

// Orden ascendente de los estados: primero los pendientes de revisar.
const ORDEN_ESTADOS = ['PENDIENTE', 'ACTIVO', 'BLOQUEADO']

const COMPARADORES = new Map([
  ['status', (a, b) => ORDEN_ESTADOS.indexOf(a.estado) - ORDEN_ESTADOS.indexOf(b.estado)],
  ['category', (a, b) => CATEGORIAS.get(a.categoria).localeCompare(CATEGORIAS.get(b.categoria), 'es')],
  ['name', (a, b) => `${a.nombre} ${a.apellidos}`.localeCompare(`${b.nombre} ${b.apellidos}`, 'es')],
])

function ordenar(vendedores, criterio, sentido) {
  const comparar = COMPARADORES.get(criterio) ?? COMPARADORES.get('status')
  const signo = sentido === 'desc' ? -1 : 1

  return [...vendedores].sort(
    (a, b) => signo * comparar(a, b) || a.nombreComercial.localeCompare(b.nombreComercial, 'es'),
  )
}

const aVendedorResumen = ({ id, nombre, apellidos, nombreComercial, email, categoria, estado }) => ({
  id,
  nombre,
  apellidos,
  nombreComercial,
  email,
  categoriaPrincipal: CATEGORIAS.get(categoria),
  estado,
})

const aVendedorDetalle = (vendedor) => ({
  ...aVendedorResumen(vendedor),
  categoriaPrincipalId: vendedor.categoria,
  dni: vendedor.dni,
  telefono: vendedor.telefono,
  avatar: vendedor.avatar,
})

const aPedido = ([id, fecha, articulos, total, estado]) => ({ id, fecha, articulos, total, estado })

// Devuelve la respuesta de error si quien pregunta no es administrador, o null si puede continuar.
function rechazarSiNoEsAdmin() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })
  return null
}

function conVendedor(params, respuestaPara) {
  const rechazo = rechazarSiNoEsAdmin()
  if (rechazo) return rechazo

  const vendedor = buscarVendedorMock(params.id)
  return vendedor ? respuestaPara(vendedor) : new HttpResponse(null, { status: 404 })
}

const paginaDePestana = (request) => leerPagina(new URL(request.url).searchParams, TAMANO_PAGINA_PESTANA)

export const adminSellersHandlers = [
  http.get('/api/admin/sellers', ({ request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const categoria = params.get('category')
    const estado = params.get('status')

    const coincidentes = vendedoresMock().filter(
      (vendedor) =>
        normalizar(`${vendedor.nombre} ${vendedor.apellidos} ${vendedor.nombreComercial} ${vendedor.email}`).includes(
          busqueda,
        ) &&
        (!categoria || vendedor.categoria === categoria) &&
        (!estado || vendedor.estado === estado),
    )
    const ordenados = ordenar(coincidentes, params.get('sort'), params.get('order'))

    return HttpResponse.json(paginar(ordenados, leerPagina(params), aVendedorResumen))
  }),

  http.get('/api/admin/sellers/:id', ({ params }) =>
    conVendedor(params, (vendedor) => HttpResponse.json(aVendedorDetalle(vendedor))),
  ),

  http.get('/api/admin/sellers/:id/products', ({ params, request }) =>
    conVendedor(params, (vendedor) => {
      const productos = PRODUCTOS.filter((producto) => producto.vendedorId === vendedor.id)
      return HttpResponse.json(paginar(productos, paginaDePestana(request), aRespuesta))
    }),
  ),

  http.get('/api/admin/sellers/:id/orders', ({ params, request }) =>
    conVendedor(params, (vendedor) => {
      const pedidos = PEDIDOS_POR_VENDEDOR.get(vendedor.id) ?? []
      return HttpResponse.json(paginar(pedidos, paginaDePestana(request), aPedido))
    }),
  ),
]
