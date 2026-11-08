import { describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const esperarRuta = (ruta) =>
  waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))

const MENSAJE_GENERICO = 'No se ha podido completar el pago. Inténtalo de nuevo más tarde.'

// Cuenta las peticiones de paso a Premium que llegan al servidor
function contarPasosAPremium() {
  const peticiones = []
  server.events.on('request:start', ({ request }) => {
    if (new URL(request.url).pathname === '/api/perfil/premium') peticiones.push(request.method)
  })
  return peticiones
}

// Nora (nuevo@esibuy.test) es cliente Estándar en el mock: entra en Premium y pulsa Hazte Premium
async function abrirPasarelaDesdePremium() {
  const user = userEvent.setup()
  await abrirSesionPorApi('nuevo@esibuy.test')
  renderApp('/premium')
  await user.click(await screen.findByRole('link', { name: /hazte premium/i }))
  await screen.findByRole('button', { name: /simular pago/i })
  return user
}

describe('PremiumPaymentPage (HU45): paso a Premium del cliente Estándar', () => {
  it('HU45-CP32: Simular pago hace al cliente Premium, lo confirma y vuelve a la vista en estado Premium', async () => {
    const peticiones = contarPasosAPremium()
    try {
      const user = await abrirPasarelaDesdePremium()
      expect(screen.getByRole('heading', { level: 2, name: 'ESIBuy Premium' })).toBeInTheDocument()
      expect(peticiones).toHaveLength(0)

      await user.click(screen.getByRole('button', { name: /simular pago/i }))

      await esperarRuta('/premium')
      const confirmacion = await screen.findByText('¡Bienvenido a ESIBuy Premium! Ya disfrutas de sus ventajas.')
      expect(confirmacion.closest('[aria-live]')).toHaveAttribute('aria-live', 'polite')
      expect(screen.getByRole('heading', { level: 2, name: /ya eres esibuy premium/i })).toBeInTheDocument()
      expect(screen.queryByRole('link', { name: /hazte premium/i })).not.toBeInTheDocument()
      expect(peticiones).toEqual(['POST'])
    } finally {
      server.events.removeAllListeners()
    }
  })

  it('HU45-CP32: tras hacerse Premium, su perfil lo muestra sin recargar', async () => {
    const user = await abrirPasarelaDesdePremium()
    await user.click(screen.getByRole('button', { name: /simular pago/i }))
    await screen.findByRole('heading', { level: 2, name: /ya eres esibuy premium/i })

    await user.click(screen.getByRole('link', { name: /mi perfil/i }))

    expect(await screen.findByLabelText('Tipo de cliente')).toHaveValue('Premium')
  })

  it('HU45-CP32: Volver deja al cliente en la vista Premium sin hacerle Premium', async () => {
    const peticiones = contarPasosAPremium()
    try {
      const user = await abrirPasarelaDesdePremium()

      await user.click(screen.getByRole('button', { name: /volver/i }))

      await esperarRuta('/premium')
      expect(await screen.findByRole('link', { name: /hazte premium/i })).toBeInTheDocument()
      expect(peticiones).toHaveLength(0)
    } finally {
      server.events.removeAllListeners()
    }
  })

  it('HU45-CP31: siendo Premium, entrar directo en la pasarela lleva a la vista Premium', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/premium/pago')

    await esperarRuta('/premium')
    expect(await screen.findByRole('heading', { level: 2, name: /ya eres esibuy premium/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /simular pago/i })).not.toBeInTheDocument()
  })

  it('HU45-CP30: sin sesión, la pasarela de Premium lleva al login; el vendedor vuelve a su inicio', async () => {
    const { unmount } = renderApp('/premium/pago')
    await esperarRuta('/login')
    unmount()

    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/premium/pago')
    await esperarRuta('/seller/products')
  })

  it('HU45-CP33: si el servidor responde ALREADY_PREMIUM, se avisa con un mensaje propio y queda en estado Premium', async () => {
    server.use(
      http.post('/api/perfil/premium', () =>
        HttpResponse.json({ mensaje: 'Texto del backend', code: 'ALREADY_PREMIUM' }, { status: 400 }),
      ),
    )
    const user = await abrirPasarelaDesdePremium()

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    await esperarRuta('/premium')
    expect(await screen.findByText('Ya eras cliente ESIBuy Premium: no hacía falta pagar.')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 2, name: /ya eres esibuy premium/i })).toBeInTheDocument()
    expect(screen.queryByText('Texto del backend')).not.toBeInTheDocument()
  })

  it('HU45-CP34: un error del servidor muestra un mensaje genérico, sin el texto del backend, y se puede reintentar', async () => {
    server.use(
      http.post('/api/perfil/premium', () =>
        HttpResponse.json({ mensaje: 'NullPointerException en PerfilService' }, { status: 500 }),
      ),
    )
    const user = await abrirPasarelaDesdePremium()

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(await screen.findByText(MENSAJE_GENERICO)).toBeInTheDocument()
    expect(screen.queryByText(/NullPointerException/)).not.toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/premium\/pago$/)
    expect(screen.getByRole('button', { name: /simular pago/i })).toBeEnabled()
  })

  it('HU45-CP34: un fallo de red muestra el mismo mensaje genérico', async () => {
    server.use(http.post('/api/perfil/premium', () => HttpResponse.error()))
    const user = await abrirPasarelaDesdePremium()

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(await screen.findByText(MENSAJE_GENERICO)).toBeInTheDocument()
  })

  it('HU45-CP34: un 403 ACCESS_DENIED avisa de que no tiene permiso, sin el texto del backend', async () => {
    server.use(
      http.post('/api/perfil/premium', () =>
        HttpResponse.json({ mensaje: 'Acceso denegado', code: 'ACCESS_DENIED' }, { status: 403 }),
      ),
    )
    const user = await abrirPasarelaDesdePremium()

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(await screen.findByText('No tienes permiso para realizar esta operación.')).toBeInTheDocument()
    expect(screen.queryByText('Acceso denegado')).not.toBeInTheDocument()
  })

  it('HU45-CP34: un 401 (sesión caducada) lleva al login con su aviso', async () => {
    server.use(http.post('/api/perfil/premium', () => new HttpResponse(null, { status: 401 })))
    const user = await abrirPasarelaDesdePremium()

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    await esperarRuta('/login')
    expect(await screen.findByText(/tu sesión ha caducado/i)).toBeInTheDocument()
  })
})
