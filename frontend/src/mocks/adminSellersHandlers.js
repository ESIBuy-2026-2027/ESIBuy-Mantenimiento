import { http, HttpResponse } from 'msw'
import { USUARIOS, getUsuarioEnSesion } from './authHandlers'
import { leerPagina, normalizar, paginar } from './paginar'
import { existeEmailDeUsuario } from './usersHandlers'
import { CATEGORIAS, OTRO_VENDEDOR, PRODUCTOS, VENDEDOR_CON_PRODUCTOS, aRespuesta } from './productsHandlers'

const VENDEDORES_DATOS = [
  // [id, nombre, apellidos, nombreComercial, email, nif, categoría principal, estado]
  [VENDEDOR_CON_PRODUCTOS, 'Vera', 'Vendedora', 'TecnoVera', 'vendedor@esibuy.test', 'B12345601', 'cat-1', 'ACTIVO'],
  ['u-5', 'Vicente', 'Vacío', 'Casa Vicente', 'vendedor-vacio@esibuy.test', 'B12345602', 'cat-2', 'ACTIVO'],
  [OTRO_VENDEDOR, 'Otto', 'Otero', 'Otero Hogar y Tecnología', 'otto@esibuy.test', 'B12345603', 'cat-1', 'ACTIVO'],
  ['s-04', 'Marta', 'Gil Prieto', 'Electro Marta', 'marta@esibuy.test', 'B12345604', 'cat-1', 'ACTIVO'],
  ['s-05', 'Hugo', 'Ramos León', 'Hogar Hugo', 'hugo@esibuy.test', 'B12345605', 'cat-2', 'PENDIENTE'],
  ['s-06', 'Lucía', 'Navarro Sanz', 'Lucía Digital', 'lucia@esibuy.test', 'B12345606', 'cat-1', 'ACTIVO'],
  ['s-07', 'Pablo', 'Iglesias Mora', 'Cocina Pablo', 'pablo@esibuy.test', 'B12345607', 'cat-2', 'DESACTIVADO'],
  ['s-08', 'Elena', 'Soto Vidal', 'Soto Gadgets', 'elena@esibuy.test', 'B12345608', 'cat-1', 'ACTIVO'],
  ['s-09', 'Raúl', 'Ortega Pons', 'Ortega Muebles', 'raul@esibuy.test', 'B12345609', 'cat-2', 'ACTIVO'],
  ['s-10', 'Irene', 'Campos Ruiz', 'Irene Smart Home', 'irene@esibuy.test', 'B12345610', 'cat-2', 'ACTIVO'],
  ['s-11', 'Sergio', 'Molina Vega', 'Molina Informática', 'sergio@esibuy.test', 'B12345611', 'cat-1', 'PENDIENTE'],
  ['s-12', 'Noelia', 'Prado Ferrer', 'Noelia Decoración', 'noelia@esibuy.test', 'B12345612', 'cat-2', 'ACTIVO'],
  ['s-13', 'Adrián', 'Bravo Cano', 'Bravo Audio', 'adrian@esibuy.test', 'B12345613', 'cat-1', 'DESACTIVADO'],
  ['s-14', 'Carmen', 'Lara Duque', 'Carmen Electrodomésticos', 'carmen@esibuy.test', 'B12345614', 'cat-2', 'ACTIVO'],
].map(([id, nombre, apellidos, nombreComercial, email, nif, categoria, estado]) => ({
  id,
  nombre,
  apellidos,
  nombreComercial,
  email,
  nif,
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

// Estados que el administrador ha cambiado durante la sesión (activar o desactivar), por id de vendedor.
const ESTADOS_ADMITIDOS = new Set(['ACTIVO', 'DESACTIVADO'])
let estadosModificados = new Map()
// Datos que el administrador ha modificado durante la sesión (HU22), por id de vendedor.
let datosModificados = new Map()

export function resetAdminSellersMock() {
  estadosModificados = new Map()
  datosModificados = new Map()
}

const conCambios = (vendedor) => ({
  ...vendedor,
  ...datosModificados.get(vendedor.id),
  ...(estadosModificados.has(vendedor.id) && { estado: estadosModificados.get(vendedor.id) }),
})

// Orden ascendente de los estados: primero los pendientes de revisar.
const ORDEN_ESTADOS = ['PENDIENTE', 'ACTIVO', 'DESACTIVADO']

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
  nif: vendedor.nif,
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

  const vendedor = VENDEDORES_DATOS.find((candidato) => candidato.id === params.id)
  return vendedor ? respuestaPara(conCambios(vendedor)) : new HttpResponse(null, { status: 404 })
}

const hayTexto = (valor) => typeof valor === 'string' && valor.trim() !== ''

// El backend real también valida: lo mínimo que el formulario ya comprueba.
const datosValidos = (datos) =>
  hayTexto(datos.nombre) &&
  hayTexto(datos.apellidos) &&
  hayTexto(datos.nombreComercial) &&
  /^[^\s@]+@[^\s@]+$/.test(datos.email ?? '') &&
  /^[A-Za-z0-9]{9}$/.test(datos.nif ?? '') &&
  CATEGORIAS.has(datos.categoriaPrincipalId)

// Código de error si el nombre comercial o el email ya son de otra cuenta, o null si están libres.
function conflictoDeDatos(idVendedor, { nombreComercial, email }) {
  const otros = VENDEDORES_DATOS.filter((candidato) => candidato.id !== idVendedor).map(conCambios)
  if (otros.some((otro) => otro.nombreComercial.toLowerCase() === nombreComercial.trim().toLowerCase())) {
    return 'COMMERCIAL_NAME_ALREADY_REGISTERED'
  }

  const propio = USUARIOS.find((usuario) => usuario.id === idVendedor)
  const emailPropio = conCambios(VENDEDORES_DATOS.find((candidato) => candidato.id === idVendedor)).email
  const usadoPorOtroVendedor = otros.some((otro) => otro.email === email)
  const usadoPorOtroUsuario = email !== emailPropio && email !== propio?.email && existeEmailDeUsuario(email)
  const usadoPorOtraCuenta = USUARIOS.some((usuario) => usuario.id !== idVendedor && usuario.email === email)
  return usadoPorOtroVendedor || usadoPorOtroUsuario || usadoPorOtraCuenta ? 'EMAIL_ALREADY_REGISTERED' : null
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

    const coincidentes = VENDEDORES_DATOS.map(conCambios).filter(
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

  // Modificar los datos de un vendedor (HU22): el nombre comercial y el email tienen que seguir siendo únicos.
  http.put('/api/admin/sellers/:id', async ({ params, request }) => {
    const datos = await request.json()

    return conVendedor(params, (vendedor) => {
      if (!datosValidos(datos)) return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })

      const conflicto = conflictoDeDatos(vendedor.id, datos)
      if (conflicto) return HttpResponse.json({ code: conflicto }, { status: 409 })

      const { nombre, apellidos, email, nombreComercial, nif, categoriaPrincipalId } = datos
      datosModificados.set(vendedor.id, {
        ...datosModificados.get(vendedor.id),
        nombre,
        apellidos,
        email,
        nombreComercial,
        nif,
        categoria: categoriaPrincipalId,
      })
      return HttpResponse.json(aVendedorDetalle(conCambios(vendedor)))
    })
  }),

  http.patch('/api/admin/sellers/:id/status', async ({ params, request }) => {
    const { estado } = await request.json()
    if (!ESTADOS_ADMITIDOS.has(estado)) return new HttpResponse(null, { status: 400 })

    return conVendedor(params, (vendedor) => {
      estadosModificados.set(vendedor.id, estado)
      return HttpResponse.json(aVendedorDetalle({ ...vendedor, estado }))
    })
  }),

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
