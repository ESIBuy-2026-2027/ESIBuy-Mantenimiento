import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const RUTA = '/admin/admins/new'

async function abrirFormulario() {
  await abrirSesionPorApi('admin@esibuy.test')
  renderApp(RUTA)
  await screen.findByRole('heading', { level: 1, name: /nuevo administrador/i })
}

async function rellenarFormularioValido(user, { email = 'nora@esibuy.test' } = {}) {
  await user.type(screen.getByLabelText(/^nombre/i), 'Nora')
  await user.type(screen.getByLabelText(/apellidos/i), 'Díaz Ruiz')
  await user.type(screen.getByLabelText(/email/i), email)
  await user.type(screen.getByLabelText(/^password/i), 'Password123!')
  await user.type(screen.getByLabelText(/^repetir password/i), 'Password123!')
}

const crear = (user) => user.click(screen.getByRole('button', { name: /crear administrador/i }))

describe('AdminNewAdminPage (HU21): formulario', () => {
  it('adminNewAdminPage_alAbrirse_muestraLosCamposDelFormulario', async () => {
    await abrirFormulario()

    expect(screen.getByLabelText(/^nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/sede/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de incorporación/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^password/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^repetir password/i)).toBeInTheDocument()
    expect(screen.getAllByRole('radio')).toHaveLength(3)
    expect(screen.getByRole('list', { name: /requisitos del password/i })).toBeInTheDocument()
  })

  it('adminNewAdminPage_marcaComoObligatoriosLosCamposObligatorios', async () => {
    await abrirFormulario()

    expect(screen.getByLabelText(/^nombre/i)).toBeRequired()
    expect(screen.getByLabelText(/apellidos/i)).toBeRequired()
    expect(screen.getByLabelText(/email/i)).toBeRequired()
    expect(screen.getByLabelText(/^password/i)).toBeRequired()
    expect(screen.getByLabelText(/^repetir password/i)).toBeRequired()
    expect(screen.getByLabelText(/sede/i)).not.toBeRequired()
  })

  it('adminNewAdminPage_laFechaDeIncorporacionEsLaDeHoyYNoSeEdita', async () => {
    await abrirFormulario()
    const hoy = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(
      new Date(),
    )

    const fecha = screen.getByLabelText(/fecha de incorporación/i)

    expect(fecha).toHaveValue(hoy)
    expect(fecha).toHaveAttribute('readonly')
  })

  it('adminNewAdminPage_limitaLaLongitudDeLosCampos', async () => {
    await abrirFormulario()

    expect(screen.getByLabelText(/^nombre/i)).toHaveAttribute('maxlength', '30')
    expect(screen.getByLabelText(/apellidos/i)).toHaveAttribute('maxlength', '30')
    expect(screen.getByLabelText(/email/i)).toHaveAttribute('maxlength', '100')
    expect(screen.getByLabelText(/sede/i)).toHaveAttribute('maxlength', '30')
    expect(screen.getByLabelText(/^password/i)).toHaveAttribute('maxlength', '64')
    expect(screen.getByLabelText(/^repetir password/i)).toHaveAttribute('maxlength', '64')
  })

  it('adminNewAdminPage_alPulsarMostrarPassword_loMuestraEnClaro', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await user.click(screen.getByRole('button', { name: /^mostrar password$/i }))

    expect(screen.getByLabelText(/^password/i)).toHaveAttribute('type', 'text')
  })
})

describe('AdminNewAdminPage (HU21): validaciones', () => {
  it('adminNewAdminPage_alEnviarVacio_muestraTodosLosErroresALaVezYNoCreaNada', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await crear(user)

    expect(screen.getAllByText(/este campo es obligatorio/i)).toHaveLength(5)
    expect(screen.queryByText(/administrador creado/i)).not.toBeInTheDocument()
  })

  it('adminNewAdminPage_conEmailInvalido_muestraErrorDeFormato', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user, { email: 'esto-no-es-un-email' })
    await crear(user)

    expect(screen.getByText(/introduce un email válido/i)).toBeInTheDocument()
  })

  it('adminNewAdminPage_conPasswordQueNoCumpleLosRequisitos_muestraError', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.clear(screen.getByLabelText(/^password/i))
    await user.clear(screen.getByLabelText(/^repetir password/i))
    await user.type(screen.getByLabelText(/^password/i), 'password')
    await user.type(screen.getByLabelText(/^repetir password/i), 'password')
    await crear(user)

    expect(screen.getByText(/no cumple los requisitos/i)).toBeInTheDocument()
    expect(screen.queryByText(/administrador creado/i)).not.toBeInTheDocument()
  })

  it('adminNewAdminPage_siLosPasswordsNoCoinciden_muestraError', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.type(screen.getByLabelText(/^repetir password/i), 'Extra')
    await crear(user)

    expect(screen.getByText(/los passwords no coinciden/i)).toBeInTheDocument()
  })
})

describe('AdminNewAdminPage (HU21): alta', () => {
  it('adminNewAdminPage_conDatosValidos_creaElAdministradorYMuestraConfirmacion', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await crear(user)

    expect(await screen.findByText(/administrador creado/i)).toBeInTheDocument()
    expect(screen.getByText(/nora díaz ruiz ya puede iniciar sesión/i)).toBeInTheDocument()
  })

  it('adminNewAdminPage_alCrear_enviaLosDatosSinLaRepeticionDelPassword', async () => {
    let cuerpo = null
    server.use(
      http.post('/api/admin/admins', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json({ id: 'adm-1' }, { status: 201 })
      }),
    )
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.type(screen.getByLabelText(/sede/i), 'Ciudad Real')
    await user.click(screen.getByRole('radio', { name: /avatar 2/i }))
    await crear(user)

    await screen.findByText(/administrador creado/i)
    expect(cuerpo).toEqual({
      nombre: 'Nora',
      apellidos: 'Díaz Ruiz',
      email: 'nora@esibuy.test',
      sede: 'Ciudad Real',
      avatar: 'avatar2',
      password: 'Password123!',
    })
  })

  it('adminNewAdminPage_conEmailYaRegistrado_muestraErrorEnElEmail', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user, { email: 'cliente@esibuy.test' })
    await crear(user)

    expect(await screen.findByText(/este email ya está registrado/i)).toBeInTheDocument()
    expect(screen.queryByText(/administrador creado/i)).not.toBeInTheDocument()
  })

  it('adminNewAdminPage_conErrorDeServidor_muestraMensajeGenerico', async () => {
    server.use(
      http.post('/api/admin/admins', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 })),
    )
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await crear(user)

    expect(await screen.findByText(/no se ha podido crear el administrador/i)).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
  })

  it('adminNewAdminPage_trasCrear_elNuevoAdministradorApareceEnElListado', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await crear(user)
    await user.click(await screen.findByRole('link', { name: /ver administradores/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /^administradores$/i })).toBeInTheDocument()
    expect(await screen.findByText('nora@esibuy.test')).toBeInTheDocument()
  })

  it('adminNewAdminPage_alPulsarCrearOtro_muestraElFormularioVacio', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await crear(user)
    await user.click(await screen.findByRole('button', { name: /crear otro administrador/i }))

    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('')
    expect(screen.getByLabelText(/email/i)).toHaveValue('')
  })
})

describe('AdminNewAdminPage (HU21): acceso', () => {
  it('adminNewAdminPage_alPulsarVolver_vuelveAlListadoDeAdministradores', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await user.click(screen.getByRole('link', { name: /volver a administradores/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin\/admins$/))
  })

  it('adminNewAdminPage_sinSesion_redirigeAlLogin', async () => {
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('adminNewAdminPage_conSesionDeVendedor_noMuestraElFormulario', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp(RUTA)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
    expect(screen.queryByRole('heading', { name: /nuevo administrador/i })).not.toBeInTheDocument()
  })

  it('adminNewAdminPage_losAvataresSonLosMismosQueEnElRegistro', async () => {
    await abrirFormulario()

    const avatares = within(screen.getByRole('radiogroup'))
    expect(avatares.getByRole('radio', { name: /avatar 1/i })).toBeInTheDocument()
    expect(avatares.getByRole('radio', { name: /avatar 3/i })).toBeInTheDocument()
  })
})
