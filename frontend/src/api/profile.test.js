import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi } from '../test/renderApp'
import { becomePremium, cancelPremium, getProfile } from './profile'

// Del mock: Nora (nuevo@esibuy.test) es cliente Estándar y Carlos (cliente@esibuy.test), cliente Premium.
describe('becomePremium (HU45)', () => {
  it('HU45-CP35: pide POST /api/perfil/premium sin cuerpo y devuelve el perfil ya Premium', async () => {
    const peticiones = []
    server.events.on('request:start', ({ request }) => {
      if (new URL(request.url).pathname === '/api/perfil/premium') peticiones.push(request.clone())
    })
    await abrirSesionPorApi('nuevo@esibuy.test')

    try {
      const perfil = await becomePremium()

      expect(peticiones).toHaveLength(1)
      expect(peticiones[0].method).toBe('POST')
      expect(await peticiones[0].text()).toBe('')
      expect(perfil).toMatchObject({ email: 'nuevo@esibuy.test', rol: 'CLIENTE', tipoCliente: 'PREMIUM' })
      expect(await getProfile()).toMatchObject({ tipoCliente: 'PREMIUM' })
    } finally {
      server.events.removeAllListeners()
    }
  })

  it('HU45-CP35: si el cliente ya era Premium, el mock responde 400 ALREADY_PREMIUM', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')

    await expect(becomePremium()).rejects.toMatchObject({ status: 400, code: 'ALREADY_PREMIUM' })
  })

  it('HU45-CP35: un vendedor recibe 403 ACCESS_DENIED y sin sesión se responde 401', async () => {
    await expect(becomePremium()).rejects.toMatchObject({ status: 401 })

    await abrirSesionPorApi('vendedor@esibuy.test')
    await expect(becomePremium()).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })

  it('HU45-CP35: el cambio a Premium del mock no pasa de un test a otro', async () => {
    await abrirSesionPorApi('nuevo@esibuy.test')

    expect(await getProfile()).toMatchObject({ tipoCliente: 'ESTANDAR' })
  })

  it('HU45-CP35: un error del servidor llega con su estado y sin el texto del backend', async () => {
    server.use(
      http.post('/api/perfil/premium', () =>
        HttpResponse.json({ mensaje: 'Detalle interno del servidor' }, { status: 500 }),
      ),
    )

    const error = await becomePremium().catch((e) => e)

    expect(error).toMatchObject({ status: 500, code: null })
    expect(error.message).not.toContain('Detalle interno')
  })
})


describe('cancelPremium (CLI-02)', () => {
  it('CLI-02: envía DELETE /api/perfil/premium sin cuerpo y devuelve el perfil Estándar', async () => {
    const peticiones = []
    server.events.on('request:start', ({ request }) => {
      if (new URL(request.url).pathname === '/api/perfil/premium') peticiones.push(request.clone())
    })
    await abrirSesionPorApi('cliente@esibuy.test')

    try {
      const perfil = await cancelPremium()

      expect(peticiones).toHaveLength(1)
      expect(peticiones[0].method).toBe('DELETE')
      expect(await peticiones[0].text()).toBe('')
      expect(perfil).toMatchObject({ email: 'cliente@esibuy.test', rol: 'CLIENTE', tipoCliente: 'ESTANDAR' })
      expect(await getProfile()).toMatchObject({ tipoCliente: 'ESTANDAR' })
    } finally {
      server.events.removeAllListeners()
    }
  })

  it('CLI-02: un cliente Estándar recibe 400 NOT_PREMIUM', async () => {
    await abrirSesionPorApi('nuevo@esibuy.test')
    await expect(cancelPremium()).rejects.toMatchObject({ status: 400, code: 'NOT_PREMIUM' })
  })

  it('CLI-02: vendedor recibe 403 y sin sesión se responde 401', async () => {
    await expect(cancelPremium()).rejects.toMatchObject({ status: 401 })
    await abrirSesionPorApi('vendedor@esibuy.test')
    await expect(cancelPremium()).rejects.toMatchObject({ status: 403, code: 'ACCESS_DENIED' })
  })

  it('CLI-02: un error del servidor se propaga sin exponer detalles internos', async () => {
    server.use(
      http.delete('/api/perfil/premium', () =>
        HttpResponse.json({ mensaje: 'Detalle interno del servidor' }, { status: 500 }),
      ),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    const error = await cancelPremium().catch((e) => e)
    expect(error).toMatchObject({ status: 500, code: null })
    expect(error.message).not.toContain('Detalle interno')
  })
})
