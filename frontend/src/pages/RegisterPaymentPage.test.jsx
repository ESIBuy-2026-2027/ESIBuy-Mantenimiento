import { afterEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { renderApp } from '../test/renderApp'

const PASSWORD = 'Password123!'

const esperarRuta = (ruta) =>
  waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))

// Guarda el cuerpo de cada registro de cliente que llega al servidor y responde como el backend (201 sin cuerpo), o
// con el error indicado.
function capturarRegistros(respuesta = () => new HttpResponse(null, { status: 201 })) {
  const cuerpos = []
  server.use(
    http.post('/api/auth/register/cliente', async ({ request }) => {
      cuerpos.push(await request.json())
      return respuesta()
    }),
  )
  return cuerpos
}

async function rellenarCliente(user, email) {
  await user.click(await screen.findByRole('button', { name: /cliente/i }))
  await user.type(screen.getByLabelText(/^nombre/i), 'Ana')
  await user.type(screen.getByLabelText(/apellidos/i), 'García')
  await user.type(screen.getByLabelText(/fecha de nacimiento/i), '1990-05-20')
  await user.type(screen.getByLabelText(/dni/i), '12345678Z')
  await user.type(screen.getByLabelText(/email/i), email)
  await user.type(screen.getByLabelText(/^password/i), PASSWORD)
  await user.type(screen.getByLabelText(/^repetir password/i), PASSWORD)
  await user.click(screen.getByRole('checkbox', { name: /política de privacidad/i }))
}

// Rellena el registro de cliente, elige Premium y pulsa Registrarse: lleva a la pasarela
async function llegarALaPasarela(email) {
  const user = userEvent.setup()
  const vista = renderApp('/register')
  await rellenarCliente(user, email)
  await user.selectOptions(screen.getByLabelText(/tipo de cliente/i), 'PREMIUM')
  await user.click(screen.getByRole('button', { name: /registrarse/i }))
  await screen.findByRole('button', { name: /simular pago/i })
  return { user, ...vista }
}

function contenidoDe(almacen) {
  return Array.from({ length: almacen.length }, (_, i) => `${almacen.key(i)}=${almacen.getItem(almacen.key(i))}`).join(';')
}

describe('RegisterPaymentPage (HU45): pasarela obligatoria del registro Premium', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('HU45-CP16: navegar directo a la pasarela sin datos lleva al registro, sin pasarela', async () => {
    renderApp('/register/pago')

    await esperarRuta('/register')
    expect(await screen.findByRole('heading', { level: 1, name: /crear cuenta/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /simular pago/i })).not.toBeInTheDocument()
  })

  it('HU45-CP16: al recargar la pasarela se pierden los datos y vuelve al registro con el formulario vacío', async () => {
    const cuerpos = capturarRegistros()
    const { unmount } = await llegarALaPasarela('recarga@correo.test')

    unmount()
    renderApp('/register/pago')

    await esperarRuta('/register')
    expect(await screen.findByRole('heading', { level: 1, name: /crear cuenta/i })).toBeInTheDocument()
    expect(cuerpos).toHaveLength(0)
  })

  it('HU45-CP17: la pasarela no registra nada hasta pulsar Simular pago; entonces se registra como PREMIUM', async () => {
    const cuerpos = capturarRegistros()
    const { user } = await llegarALaPasarela('premium@correo.test')

    expect(screen.getByRole('heading', { level: 2, name: 'ESIBuy Premium' })).toBeInTheDocument()
    expect(cuerpos).toHaveLength(0)

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /cuenta creada/i })).toBeInTheDocument()
    await esperarRuta('/register')
    expect(cuerpos).toHaveLength(1)
    expect(cuerpos[0]).toMatchObject({ email: 'premium@correo.test', tipoCliente: 'PREMIUM' })
    expect(screen.getByRole('link', { name: /ir a iniciar sesión/i })).toHaveAttribute('href', '/login')
  })

  it('HU45-CP17: si el servidor rechaza el registro tras el pago, vuelve al formulario con sus datos y el motivo', async () => {
    capturarRegistros(() => HttpResponse.json({ code: 'EMAIL_ALREADY_REGISTERED' }, { status: 409 }))
    const { user } = await llegarALaPasarela('repetido@correo.test')

    await user.click(screen.getByRole('button', { name: /simular pago/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /registro de cliente/i })).toBeInTheDocument()
    await esperarRuta('/register')
    expect(screen.getByLabelText(/email/i)).toHaveValue('repetido@correo.test')
    expect(screen.getByLabelText(/email/i)).toHaveAccessibleDescription('Este email ya está registrado')
  })

  it('HU45-CP18: cancelar vuelve al formulario con sus datos; con Estándar se registra sin pasar por la pasarela', async () => {
    const cuerpos = capturarRegistros()
    const { user } = await llegarALaPasarela('cambia@correo.test')

    await user.click(screen.getByRole('button', { name: /volver al registro/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /registro de cliente/i })).toBeInTheDocument()
    await esperarRuta('/register')
    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Ana')
    expect(screen.getByLabelText(/email/i)).toHaveValue('cambia@correo.test')
    expect(screen.getByLabelText(/tipo de cliente/i)).toHaveValue('PREMIUM')
    expect(cuerpos).toHaveLength(0)

    await user.selectOptions(screen.getByLabelText(/tipo de cliente/i), 'ESTANDAR')
    await user.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /cuenta creada/i })).toBeInTheDocument()
    expect(cuerpos).toHaveLength(1)
    expect(cuerpos[0]).toMatchObject({ email: 'cambia@correo.test', tipoCliente: 'ESTANDAR' })
  })

  it('HU45-CP18: tras cancelar, volver a elegir Premium vuelve a exigir la pasarela', async () => {
    const cuerpos = capturarRegistros()
    const { user } = await llegarALaPasarela('reintenta@correo.test')

    await user.click(screen.getByRole('button', { name: /volver al registro/i }))
    await user.click(await screen.findByRole('button', { name: /registrarse/i }))

    expect(await screen.findByRole('button', { name: /simular pago/i })).toBeInTheDocument()
    await esperarRuta('/register/pago')
    expect(cuerpos).toHaveLength(0)
  })

  it('HU45-CP19: la contraseña no sale en la URL ni se guarda en localStorage ni en sessionStorage', async () => {
    capturarRegistros()
    const guardados = vi.spyOn(Storage.prototype, 'setItem')
    const { user } = await llegarALaPasarela('privado@correo.test')

    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/register\/pago$/)
    expect(window.location.href).not.toContain(encodeURIComponent(PASSWORD))
    await user.click(screen.getByRole('button', { name: /simular pago/i }))
    await screen.findByRole('heading', { level: 1, name: /cuenta creada/i })

    expect(guardados.mock.calls.flat().join(' ')).not.toContain(PASSWORD)
    expect(contenidoDe(window.localStorage)).not.toContain(PASSWORD)
    expect(contenidoDe(window.sessionStorage)).not.toContain(PASSWORD)
    expect(window.location.href).not.toContain(encodeURIComponent(PASSWORD))
  })
})
