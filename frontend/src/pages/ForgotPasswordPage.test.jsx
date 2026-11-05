import { afterEach, describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Recuperación de contraseña (HU3, T398) contra el contrato del backend (T399, PR #176):
// POST /api/auth/password-recovery {email} responde siempre 204, y
// POST /api/auth/password-recovery/confirm {email, code, password} responde 204, 401 (código que no sirve)
// o 400 con el code del motivo si la contraseña no cumple la política.
// El código válido del mock es 112233 y la cuenta, cualquiera de los usuarios del mock de autenticación.

const EMAIL = 'cliente@esibuy.test'
const CODIGO = '112233'
const NUEVA = 'Nueva-Clave-9!'
const AVISO_ENVIADO = /si el email está registrado, te hemos enviado un código de 6 dígitos/i

async function pedirCodigo(user, email = EMAIL) {
  await user.type(await screen.findByLabelText(/correo electrónico/i), email)
  await user.click(screen.getByRole('button', { name: /enviar código/i }))
}

async function restablecer(user, { codigo = CODIGO, nueva = NUEVA, repetida = nueva } = {}) {
  if (codigo) await user.type(await screen.findByLabelText(/código recibido por correo/i), codigo)
  if (nueva) await user.type(screen.getByLabelText(/^contraseña nueva/i), nueva)
  if (repetida) await user.type(screen.getByLabelText(/^repetir contraseña nueva/i), repetida)
  await user.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
}

// Guarda los cuerpos que llegan a un endpoint, sin cambiar la respuesta del mock.
function espiarPeticiones(url) {
  const cuerpos = []
  server.events.on('request:start', async ({ request }) => {
    if (new URL(request.url).pathname === url) cuerpos.push(await request.clone().json())
  })
  return cuerpos
}

afterEach(() => server.events.removeAllListeners())

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('Recuperación de contraseña (HU3): paso 1, pedir el código', () => {
  it('HU3-CP19: sin email, o con uno mal formado, avisa en el campo y no pide ningún código', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPeticiones('/api/auth/password-recovery')
    renderApp('/forgot-password')

    await user.click(await screen.findByRole('button', { name: /enviar código/i }))
    expect(await screen.findByText(/introduce tu correo electrónico/i)).toBeInTheDocument()

    await user.type(screen.getByLabelText(/correo electrónico/i), 'no-es-un-email')
    await user.click(screen.getByRole('button', { name: /enviar código/i }))
    expect(await screen.findByText(/introduce un correo electrónico válido/i)).toBeInTheDocument()

    expect(peticiones).toHaveLength(0)
  })

  it('HU3-CP20: pide el código para el email escrito y pasa al segundo paso con el aviso de que se ha enviado', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPeticiones('/api/auth/password-recovery')
    renderApp('/forgot-password')

    await pedirCodigo(user, `  ${EMAIL} `)

    expect(await screen.findByText(AVISO_ENVIADO)).toBeInTheDocument()
    expect(screen.getByText(/caduca en 10 minutos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/código recibido por correo/i)).toBeInTheDocument()
    expect(peticiones).toEqual([{ email: EMAIL }])
  })

  it('HU3-CP20: con un email que no está registrado muestra el mismo aviso (no revela qué emails existen)', async () => {
    const user = userEvent.setup()
    renderApp('/forgot-password')

    await pedirCodigo(user, 'nadie@esibuy.test')

    expect(await screen.findByText(AVISO_ENVIADO)).toBeInTheDocument()
  })

  it('HU3-CP20: si el servidor falla al pedir el código, lo avisa y se queda en el primer paso', async () => {
    server.use(http.post('/api/auth/password-recovery', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    renderApp('/forgot-password')

    await pedirCodigo(user)

    expect(await screen.findByText(/no se ha podido enviar el código/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/código recibido por correo/i)).not.toBeInTheDocument()
  })
})

describe('Recuperación de contraseña (HU3): paso 2, cambiar la contraseña', () => {
  it('HU3-CP21: comprueba el código y la contraseña nueva antes de enviarlos', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPeticiones('/api/auth/password-recovery/confirm')
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await restablecer(user, { codigo: '123', nueva: 'corta', repetida: 'otra-distinta' })

    expect(await screen.findByText(/el código tiene 6 dígitos/i)).toBeInTheDocument()
    expect(screen.getByText(/no cumple los requisitos/i)).toBeInTheDocument()
    expect(screen.getByText(/no coinciden/i)).toBeInTheDocument()
    expect(peticiones).toHaveLength(0)
  })

  it('HU3-CP21: muestra los requisitos de la contraseña nueva y marca los que ya cumple', async () => {
    const user = userEvent.setup()
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await user.type(await screen.findByLabelText(/^contraseña nueva/i), 'abcdefgh')

    const requisitos = screen.getByRole('list', { name: /requisitos/i })
    expect(requisitos).toBeInTheDocument()
    expect(screen.getAllByLabelText('Cumplido')).toHaveLength(2)
  })

  it('HU3-CP22: con el código correcto cambia la contraseña y vuelve al login avisando del cambio', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPeticiones('/api/auth/password-recovery/confirm')
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await restablecer(user)

    await esperarRuta('/login')
    expect(await screen.findByText(/contraseña cambiada\. ya puedes iniciar sesión/i)).toBeInTheDocument()
    expect(peticiones).toEqual([{ email: EMAIL, code: CODIGO, password: NUEVA }])
  })

  it('HU3-CP23: con un código incorrecto o caducado lo avisa y se queda en el paso para volver a intentarlo', async () => {
    const user = userEvent.setup()
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await restablecer(user, { codigo: '000000' })

    expect(await screen.findByText(/el código no es correcto o ha caducado/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/código recibido por correo/i)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/forgot-password$/)
  })

  it('HU3-CP23: se puede pedir otro código para el mismo email', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPeticiones('/api/auth/password-recovery')
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await user.click(await screen.findByRole('button', { name: /pedir otro código/i }))

    expect(await screen.findByText(/te hemos enviado otro código/i)).toBeInTheDocument()
    expect(peticiones).toEqual([{ email: EMAIL }, { email: EMAIL }])
  })

  it('HU3-CP24: si el servidor rechaza la contraseña por la política, lo muestra en el campo', async () => {
    server.use(
      http.post('/api/auth/password-recovery/confirm', () =>
        HttpResponse.json({ code: 'PASSWORD_CONTAINS_PERSONAL_DATA' }, { status: 400 }),
      ),
    )
    const user = userEvent.setup()
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await restablecer(user)

    expect(
      await screen.findByText(/la contraseña no puede contener tu nombre, tus apellidos ni tu email/i),
    ).toBeInTheDocument()
  })

  it('HU3-CP24: si el servidor falla por otro motivo, muestra un aviso genérico sin detalles internos', async () => {
    server.use(http.post('/api/auth/password-recovery/confirm', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    renderApp('/forgot-password')
    await pedirCodigo(user)

    await restablecer(user)

    expect(await screen.findByText(/no se ha podido completar la operación/i)).toBeInTheDocument()
  })
})

describe('Recuperación de contraseña (HU3): acceso', () => {
  it('HU3-CP25: desde la recuperación se puede volver al inicio de sesión', async () => {
    const user = userEvent.setup()
    renderApp('/forgot-password')

    await user.click(await screen.findByRole('link', { name: /volver al inicio de sesión/i }))

    await esperarRuta('/login')
  })

  it('HU3-CP25: con la sesión abierta no se muestra la recuperación y lleva a su inicio', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/forgot-password')

    await esperarRuta('/products')
  })
})
