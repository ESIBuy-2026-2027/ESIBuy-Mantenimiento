import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi } from '../test/renderApp'
import { ApiError, request } from './client'
import { deleteUser, getUser, listUsers, updateUser, updateUserStatus } from './users'

const RESPUESTA = { items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 }

function capturarPeticion() {
  const peticion = {}
  server.use(
    http.get('/api/admin/usuarios', ({ request }) => {
      peticion.url = new URL(request.url)
      return HttpResponse.json(RESPUESTA)
    }),
  )
  return peticion
}

describe('listUsers', () => {
  it('HU19-CP05: pide la página y el tamaño indicados', async () => {
    const peticion = capturarPeticion()

    const respuesta = await listUsers({ page: 1, size: 10 })

    expect(peticion.url.searchParams.get('page')).toBe('1')
    expect(peticion.url.searchParams.get('size')).toBe('10')
    expect(respuesta).toEqual(RESPUESTA)
  })

  it('HU19-CP02: envía la búsqueda solo cuando tiene texto', async () => {
    const peticion = capturarPeticion()

    await listUsers({ search: 'ana', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('search')).toBe('ana')

    await listUsers({ search: '   ', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('search')).toBe(false)
  })

  it('HU19-CP03: envía el rol cuando se filtra por rol', async () => {
    const peticion = capturarPeticion()

    await listUsers({ role: 'VENDEDOR', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('role')).toBe('VENDEDOR')

    await listUsers({ role: '', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('role')).toBe(false)
  })

  it('HU19-CP04: envía el estado cuando se filtra por estado', async () => {
    const peticion = capturarPeticion()

    await listUsers({ status: 'PENDIENTE', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('status')).toBe('PENDIENTE')

    await listUsers({ status: '', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('status')).toBe(false)
  })

  it('HU19-CP08: propaga un ApiError si la respuesta no es correcta', async () => {
    server.use(
      http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 })),
    )

    const error = await listUsers({ page: 1, size: 10 }).catch((e) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(500)
  })

describe('updateUserStatus', () => {
  function capturarCambio() {
    const peticion = {}
    server.use(
      http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
        peticion.id = params.id
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id, ...peticion.cuerpo })
      }),
    )
    return peticion
  }

  it('updateUserStatus_paraActivar_enviaActivoPorPatchALaRutaDelUsuario', async () => {
    const peticion = capturarCambio()

    const respuesta = await updateUserStatus('usr-4', 'ACTIVO')

    expect(peticion).toEqual({ id: 'usr-4', metodo: 'PATCH', cuerpo: { estado: 'ACTIVO' } })
    expect(respuesta).toEqual({ id: 'usr-4', estado: 'ACTIVO' })
  })

  it('updateUserStatus_paraBloquear_enviaBloqueado', async () => {
    const peticion = capturarCambio()

    await updateUserStatus('usr-3', 'BLOQUEADO')

    expect(peticion.cuerpo).toEqual({ estado: 'BLOQUEADO' })
  })

  it('updateUserStatus_conConflicto_lanzaApiErrorConSuCodigo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'CANNOT_DEACTIVATE_SELF' }, { status: 409 }),
      ),
    )

    const error = await updateUserStatus('u-1', 'BLOQUEADO').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('CANNOT_DEACTIVATE_SELF')
  })
})

describe('listUsers con búsqueda por nombre y ordenación (HU24)', () => {
  it('listUsers_conSearchBySortYOrder_losEnviaEnLaConsulta', async () => {
    const peticion = capturarPeticion()

    await listUsers({ search: 'ana', page: 1, size: 10, searchBy: 'name', sort: 'status', order: 'desc' })

    expect(peticion.url.searchParams.get('searchBy')).toBe('name')
    expect(peticion.url.searchParams.get('sort')).toBe('status')
    expect(peticion.url.searchParams.get('order')).toBe('desc')
  })

  it('listUsers_sinSearchBySortNiOrder_noLosEnvia', async () => {
    const peticion = capturarPeticion()

    await listUsers({ page: 1, size: 10 })

    expect(peticion.url.searchParams.has('searchBy')).toBe(false)
    expect(peticion.url.searchParams.has('sort')).toBe(false)
    expect(peticion.url.searchParams.has('order')).toBe(false)
  })
})
})

describe('getUser', () => {
  it('getUser_pideLaFichaALaRutaDelUsuario', async () => {
    let ruta = null
    server.use(
      http.get('/api/admin/usuarios/:id', ({ request, params }) => {
        ruta = new URL(request.url).pathname
        return HttpResponse.json({ id: params.id, nombre: 'Lucía' })
      }),
    )

    const usuario = await getUser('usr-4')

    expect(ruta).toBe('/api/admin/usuarios/usr-4')
    expect(usuario).toEqual({ id: 'usr-4', nombre: 'Lucía' })
  })

  it('getUser_conUsuarioInexistente_lanzaApiErrorConEstado404', async () => {
    server.use(http.get('/api/admin/usuarios/:id', () => new HttpResponse(null, { status: 404 })))

    const error = await getUser('no-existe').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(404)
  })
})

describe('updateUser', () => {
  it('updateUser_enviaLosDatosPorPutALaRutaDelUsuario', async () => {
    const peticion = {}
    server.use(
      http.put('/api/admin/usuarios/:id', async ({ params, request }) => {
        peticion.id = params.id
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id })
      }),
    )

    const respuesta = await updateUser('usr-3', { nombre: 'Carla', apellidos: 'Moreno Gil' })

    expect(peticion).toEqual({ id: 'usr-3', cuerpo: { nombre: 'Carla', apellidos: 'Moreno Gil' } })
    expect(respuesta).toEqual({ id: 'usr-3' })
  })

  it('updateUser_codificaElIdentificadorEnLaRuta', async () => {
    let id
    server.use(
      http.put('/api/admin/usuarios/:id', ({ params }) => {
        id = params.id
        return HttpResponse.json({})
      }),
    )

    await updateUser('a b/c', {})

    expect(id).toBe('a b/c')
  })

  it('updateUser_conDatosRechazados_lanzaApiErrorConElCodigo', async () => {
    server.use(
      http.put('/api/admin/usuarios/:id', () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })),
    )

    const error = await updateUser('usr-3', {}).catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 400, code: 'VALIDATION_ERROR' })
  })
})

describe('PUT /api/admin/usuarios/{id} con el mock (HU22)', () => {
  const datosDeCliente = {
    nombre: 'Elena',
    apellidos: 'Pérez',
    dni: '12345678Z',
    telefono: '',
    fechaNacimiento: '1990-05-20',
    tipoCliente: 'ESTANDAR',
    avatar: 'avatar1',
  }

  const datosDeVendedor = {
    nombre: 'Vicente',
    apellidos: 'Vacío',
    nombreComercial: 'Casa Vicente',
    dni: '12345602V',
    categoriaPrincipalId: 'cat-2',
  }

  it('updateUser_conElMock_unAdministradorPuedeEditarSusPropiosDatos', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    const respuesta = await updateUser('u-1', { nombre: 'Adela', apellidos: 'Admin', sede: 'Toledo', avatar: 'avatar2' })

    expect(respuesta).toMatchObject({ rol: 'ADMINISTRADOR', nombre: 'Adela' })
  })

  // El rol de una cuenta no se puede cambiar ("el que nace cliente muere cliente"): como el backend, se ignora.
  it('updateUser_conElMock_elRolDelCuerpoSeIgnora', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    const respuesta = await updateUser('usr-3', { ...datosDeCliente, rol: 'ADMINISTRADOR' })

    expect(respuesta).toMatchObject({ rol: 'CLIENTE', nombre: 'Elena', dni: '12345678Z' })
  })

  it('updateUser_conElMock_unVendedorConOtroRolEnElCuerpoSigueSiendoVendedor', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    await updateUser('u-5', { ...datosDeVendedor, rol: 'CLIENTE', nombreComercial: 'Casa Vicente Nueva' })

    expect(await request('/api/admin/sellers/u-5')).toMatchObject({ nombreComercial: 'Casa Vicente Nueva' })
  })

  it('updateUser_conElMock_unNombreComercialRepetidoDevuelve409', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    const error = await updateUser('u-5', { ...datosDeVendedor, nombreComercial: 'TecnoVera' }).catch(
      (excepcion) => excepcion,
    )

    expect(error).toMatchObject({ status: 409, code: 'COMMERCIAL_NAME_ALREADY_REGISTERED' })
  })

  it('updateUser_conElMock_unDatoInvalidoDelRolDeLaCuentaDevuelve400', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    const error = await updateUser('u-5', { ...datosDeVendedor, dni: 'ABC' }).catch((excepcion) => excepcion)

    expect(error).toMatchObject({ status: 400 })
  })
})

describe('deleteUser', () => {
  it('deleteUser_envia_DELETE_ALaRutaDelUsuario_yDevuelveNullSinCuerpo', async () => {
    const peticion = {}
    server.use(
      http.delete('/api/admin/usuarios/:id', ({ params, request }) => {
        peticion.id = params.id
        peticion.metodo = request.method
        return new HttpResponse(null, { status: 204 })
      }),
    )

    const respuesta = await deleteUser('usr-3')

    expect(peticion).toEqual({ id: 'usr-3', metodo: 'DELETE' })
    expect(respuesta).toBeNull()
  })

  it('deleteUser_codificaElIdEnLaRuta', async () => {
    let ruta = null
    server.use(
      http.delete('/api/admin/usuarios/:id', ({ request }) => {
        ruta = new URL(request.url).pathname
        return new HttpResponse(null, { status: 204 })
      }),
    )

    await deleteUser('a/b')

    expect(ruta).toBe('/api/admin/usuarios/a%2Fb')
  })

  it('deleteUser_conConflicto_lanzaApiErrorConSuCodigo', async () => {
    server.use(
      http.delete('/api/admin/usuarios/:id', () => HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 })),
    )

    const error = await deleteUser('usr-1').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('LAST_ACTIVE_ADMIN')
  })
})

describe('deleteUser (HU37): el mock se comporta como el backend', () => {
  it('HU37-CP20: eliminar a un administrador responde 204 sin cuerpo', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    // Diego Delgado Iglesias (usr-11) es un administrador bloqueado
    await expect(deleteUser('usr-11')).resolves.toBeNull()
  })

  it.each([
    ['un cliente', 'usr-3'],
    ['un vendedor', 'u-2'],
    ['un usuario que no existe', 'no-existe'],
  ])('HU37-CP20: eliminar a %s responde 404 y no lo borra', async (_quien, id) => {
    await abrirSesionPorApi('admin@esibuy.test')

    await expect(deleteUser(id)).rejects.toMatchObject({ status: 404 })
    if (id.startsWith('usr-')) await expect(getUser(id)).resolves.toMatchObject({ id })
  })

  it('HU37-CP20: el mock da 409 con el código al eliminar la propia cuenta', async () => {
    await abrirSesionPorApi('admin@esibuy.test')

    await expect(deleteUser('u-1')).rejects.toMatchObject({ status: 409, code: 'CANNOT_DELETE_SELF' })
  })

  it('HU37-CP20: sin sesión responde 401 y con otro rol 403', async () => {
    await expect(deleteUser('usr-11')).rejects.toMatchObject({ status: 401 })

    await abrirSesionPorApi('cliente@esibuy.test')
    await expect(deleteUser('usr-11')).rejects.toMatchObject({ status: 403 })
  })
})
