import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError } from './client'
import { addAddress, deleteAddress, getAddresses, setPrincipalAddress, updateAddress } from './addresses'

// Cliente de la API de direcciones de envío del cliente (HU5): GET, POST, PUT /{id}, PUT /{id}/principal y
// DELETE /{id} sobre /api/customer/addresses. Los mocks de MSW sustituyen al backend.

const URL_DIRECCIONES = '/api/customer/addresses'

const DATOS = {
  destinatario: 'Carlos Cliente',
  calle: 'Calle Mayor 1',
  detalles: '2º B',
  codigoPostal: '13071',
  ciudad: 'Ciudad Real',
  provincia: 'Ciudad Real',
  telefono: '612345678',
}
const DIRECCION = { id: 'd1', ...DATOS, principal: true }

describe('api de direcciones de envío', () => {
  it('HU5-CP17: getAddresses pide las direcciones del cliente en sesión con la marca de la principal', async () => {
    server.use(http.get(URL_DIRECCIONES, () => HttpResponse.json([DIRECCION])))

    const direcciones = await getAddresses()

    expect(direcciones).toEqual([DIRECCION])
  })

  it('HU5-CP17: addAddress envía los datos de la dirección con un POST', async () => {
    const peticion = {}
    server.use(
      http.post(URL_DIRECCIONES, async ({ request }) => {
        peticion.cuerpo = await request.json()
        return HttpResponse.json(DIRECCION, { status: 201 })
      }),
    )

    const creada = await addAddress(DATOS)

    expect(peticion.cuerpo).toEqual(DATOS)
    expect(creada).toEqual(DIRECCION)
  })

  it('HU5-CP17: updateAddress envía los datos nuevos con un PUT a /{id}', async () => {
    const peticion = {}
    server.use(
      http.put(`${URL_DIRECCIONES}/:id`, async ({ request, params }) => {
        peticion.id = params.id
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ ...DIRECCION, calle: 'Calle Toledo 5' })
      }),
    )

    const editada = await updateAddress('d1', { ...DATOS, calle: 'Calle Toledo 5' })

    expect(peticion.id).toBe('d1')
    expect(peticion.cuerpo).toEqual({ ...DATOS, calle: 'Calle Toledo 5' })
    expect(editada.calle).toBe('Calle Toledo 5')
  })

  it('HU5-CP17: setPrincipalAddress marca la dirección con un PUT a /{id}/principal', async () => {
    const peticion = {}
    server.use(
      http.put(`${URL_DIRECCIONES}/:id/principal`, ({ params }) => {
        peticion.id = params.id
        return new HttpResponse(null, { status: 204 })
      }),
    )

    const respuesta = await setPrincipalAddress('d2')

    expect(peticion.id).toBe('d2')
    expect(respuesta).toBeNull()
  })

  it('HU5-CP17: deleteAddress elimina la dirección y envía la sustituta solo si se indica', async () => {
    const peticiones = []
    server.use(
      http.delete(`${URL_DIRECCIONES}/:id`, ({ request, params }) => {
        peticiones.push({ id: params.id, url: new URL(request.url) })
        return new HttpResponse(null, { status: 204 })
      }),
    )

    // Al eliminar la principal, el cliente elige cuál la sustituye
    await deleteAddress('d1', 'd2')
    // Una dirección que no es la principal se elimina sin sustituta
    await deleteAddress('d3')

    expect(peticiones[0].id).toBe('d1')
    expect(peticiones[0].url.searchParams.get('nuevaPrincipal')).toBe('d2')
    expect(peticiones[1].id).toBe('d3')
    expect(peticiones[1].url.searchParams.has('nuevaPrincipal')).toBe(false)
  })

  it('HU5-CP17: un error del backend llega como ApiError con su código', async () => {
    server.use(
      http.post(URL_DIRECCIONES, () =>
        HttpResponse.json(
          { mensaje: 'Se ha alcanzado el máximo de direcciones de envío', code: 'ADDRESS_LIMIT_REACHED' },
          { status: 409 },
        ),
      ),
    )

    const error = await addAddress(DATOS).catch((e) => e)

    // La vista decide el mensaje a partir del código, no del texto del backend
    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('ADDRESS_LIMIT_REACHED')
  })
})
