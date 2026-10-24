import { describe, it, expect } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { useLastActiveAdmin } from './useLastActiveAdmin'

const ESPERA = { timeout: 3000 }

function capturarPeticiones(respuesta) {
  const peticiones = []
  server.use(
    http.get('/api/admin/usuarios', ({ request }) => {
      peticiones.push(Object.fromEntries(new URL(request.url).searchParams))
      return respuesta ? HttpResponse.json(respuesta) : undefined
    }),
  )
  return peticiones
}

describe('useLastActiveAdmin (HU19, AB#250)', () => {
  it('useLastActiveAdmin_pideSoloLosAdministradoresActivos', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    const peticiones = capturarPeticiones()

    renderHook(() => useLastActiveAdmin())

    await waitFor(() => expect(peticiones).toHaveLength(1), ESPERA)
    expect(peticiones[0]).toMatchObject({ role: 'ADMINISTRADOR', status: 'ACTIVO', page: '1', size: '1' })
  })

  it('useLastActiveAdmin_conVariosAdministradoresActivos_noHayUltimo', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    const peticiones = capturarPeticiones()

    const { result } = renderHook(() => useLastActiveAdmin())

    await waitFor(() => expect(peticiones).toHaveLength(1), ESPERA)
    await waitFor(() => expect(result.current.lastAdminId).toBeNull(), ESPERA)
  })

  it('useLastActiveAdmin_conUnSoloAdministradorActivo_devuelveSuId', async () => {
    capturarPeticiones({
      items: [{ id: 'u-9', nombre: 'Uno', apellidos: 'Solo', rol: 'ADMINISTRADOR', estado: 'ACTIVO' }],
      page: 1,
      size: 1,
      totalItems: 1,
      totalPages: 1,
    })

    const { result } = renderHook(() => useLastActiveAdmin())

    await waitFor(() => expect(result.current.lastAdminId).toBe('u-9'), ESPERA)
  })

  it('useLastActiveAdmin_deshabilitado_noPideNadaYNoHayUltimo', async () => {
    const peticiones = capturarPeticiones()

    const { result } = renderHook(() => useLastActiveAdmin(false))

    await new Promise((resolver) => setTimeout(resolver, 150))
    expect(peticiones).toHaveLength(0)
    expect(result.current.lastAdminId).toBeNull()
  })

  it('useLastActiveAdmin_alRefrescar_vuelveAPedirlo', async () => {
    const peticiones = capturarPeticiones({ items: [], page: 1, size: 1, totalItems: 0, totalPages: 0 })

    const { result } = renderHook(() => useLastActiveAdmin())
    await waitFor(() => expect(peticiones).toHaveLength(1), ESPERA)
    result.current.refresh()

    await waitFor(() => expect(peticiones).toHaveLength(2), ESPERA)
  })
})
