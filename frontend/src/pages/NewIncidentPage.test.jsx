import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Nueva incidencia» (HU44, T289 y T290) contra el contrato del backend (PR #224): POST /api/perfil/incidencias crea la
// incidencia del usuario en sesión y responde 201; un 400 VALIDATION_ERROR tiene un mensaje genérico.

const URL_INCIDENCIAS = '/api/perfil/incidencias'
const CLIENTE = 'cliente@esibuy.test'
const ASUNTO = 'No puedo cambiar mi contraseña'
const DESCRIPCION = 'El enlace del correo de recuperación ya ha caducado.'
const AVISO_400 = 'No se ha podido crear la incidencia. Revisa el asunto y la descripción e inténtalo de nuevo.'
const AVISO_RED = 'No se ha podido crear la incidencia. Comprueba tu conexión e inténtalo de nuevo.'

async function abrirFormulario(email = CLIENTE) {
  await abrirSesionPorApi(email)
  renderApp('/incidents/new')
  await screen.findByRole('heading', { level: 1, name: /nueva incidencia/i })
}

async function rellenar(user, asunto = ASUNTO, descripcion = DESCRIPCION) {
  await user.type(screen.getByLabelText(/^asunto/i), asunto)
  await user.type(screen.getByLabelText(/^descripción/i), descripcion)
}

const enviar = () => screen.getByRole('button', { name: /enviar incidencia/i })

function contarEnvios(respuesta) {
  const envio = { llamadas: 0, cuerpo: null }
  server.use(
    http.post(URL_INCIDENCIAS, async ({ request }) => {
      envio.llamadas += 1
      envio.cuerpo = await request.json()
      return respuesta()
    }),
  )
  return envio
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('NewIncidentPage (HU44): crear', () => {
  it('HU44-CP30: crear la incidencia avisa del éxito y vuelve a la lista con la incidencia nueva visible', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenar(user)
    await user.click(enviar())

    await esperarRuta('/incidents')
    expect(await screen.findByText(/incidencia enviada/i)).toBeInTheDocument()
    const fila = await screen.findByRole('row', { name: new RegExp(ASUNTO) })
    expect(within(fila).getByText('Pendiente')).toBeInTheDocument()
    expect(within(fila).getByText('Sin respuesta todavía')).toBeInTheDocument()
    expect(screen.getByText('Página 2 de 2')).toBeInTheDocument()
  })

  it('HU44-CP30: se envían el asunto y la descripción sin espacios en los extremos y sin ningún autor', async () => {
    const user = userEvent.setup()
    const envio = contarEnvios(() =>
      HttpResponse.json(
        { id: 'inc-1', asunto: ASUNTO, descripcion: DESCRIPCION, estado: 'PENDIENTE', respuesta: '', fechaCreacion: '2026-11-06T10:15:30Z' },
        { status: 201 },
      ),
    )
    await abrirFormulario()

    await rellenar(user, `  ${ASUNTO}  `, `  ${DESCRIPCION}  `)
    await user.click(enviar())

    await esperarRuta('/incidents')
    expect(envio.cuerpo).toEqual({ asunto: ASUNTO, descripcion: DESCRIPCION })
  })

  it('HU44-CP29: un formulario no válido no llama al servidor', async () => {
    const user = userEvent.setup()
    const envio = contarEnvios(() => HttpResponse.json({}, { status: 201 }))
    await abrirFormulario()

    await user.click(enviar())

    expect(await screen.findByText('El asunto es obligatorio.')).toBeInTheDocument()
    expect(envio.llamadas).toBe(0)
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent('/incidents/new')
  })

  it('HU44-CP30: mientras se envía, el botón queda deshabilitado', async () => {
    const user = userEvent.setup()
    server.use(
      http.post(URL_INCIDENCIAS, async () => {
        await delay(200)
        return HttpResponse.json({}, { status: 201 })
      }),
    )
    await abrirFormulario()
    await rellenar(user)

    await user.click(enviar())

    expect(enviar()).toBeDisabled()
    await esperarRuta('/incidents')
  })
})

describe('NewIncidentPage (HU44): errores y cancelar', () => {
  it('HU44-CP31: un 400 avisa con un mensaje genérico, no sale del formulario y conserva lo escrito', async () => {
    const user = userEvent.setup()
    server.use(
      http.post(URL_INCIDENCIAS, () =>
        HttpResponse.json({ mensaje: 'La incidencia es inválida', code: 'VALIDATION_ERROR' }, { status: 400 }),
      ),
    )
    await abrirFormulario()
    await rellenar(user)

    await user.click(enviar())

    expect(await screen.findByText(AVISO_400)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent('/incidents/new')
    expect(screen.getByLabelText(/^asunto/i)).toHaveValue(ASUNTO)
    expect(screen.getByLabelText(/^descripción/i)).toHaveValue(DESCRIPCION)
    expect(enviar()).toBeEnabled()
  })

  it('HU44-CP31: un error de red avisa de la conexión y conserva lo escrito', async () => {
    const user = userEvent.setup()
    server.use(http.post(URL_INCIDENCIAS, () => HttpResponse.error()))
    await abrirFormulario()
    await rellenar(user)

    await user.click(enviar())

    expect(await screen.findByText(AVISO_RED)).toBeInTheDocument()
    expect(screen.getByLabelText(/^asunto/i)).toHaveValue(ASUNTO)
    expect(screen.getByLabelText(/^descripción/i)).toHaveValue(DESCRIPCION)
  })

  it('HU44-CP31: tras un fallo se puede volver a enviar y se crea la incidencia', async () => {
    const user = userEvent.setup()
    server.use(http.post(URL_INCIDENCIAS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirFormulario()
    await rellenar(user)
    await user.click(enviar())
    await screen.findByText(AVISO_RED)

    await user.click(enviar())

    await esperarRuta('/incidents')
    expect(await screen.findByRole('row', { name: new RegExp(ASUNTO) })).toBeInTheDocument()
  })

  it('HU44-CP31: cancelar vuelve a la lista sin crear nada', async () => {
    const user = userEvent.setup()
    const envio = contarEnvios(() => HttpResponse.json({}, { status: 201 }))
    await abrirFormulario()
    await rellenar(user)

    await user.click(screen.getByRole('link', { name: /cancelar/i }))

    await esperarRuta('/incidents')
    await screen.findByRole('table', { name: 'Mis incidencias' })
    expect(envio.llamadas).toBe(0)
    expect(screen.queryByText(/incidencia enviada/i)).not.toBeInTheDocument()
  })
})
