import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

const CLAVE = 'Mock-Clave-1!'

async function escribirCredenciales(user, email, clave) {
  await user.type(await screen.findByLabelText(/correo electrónico/i), email)
  await user.type(screen.getByLabelText(/contraseña/i), clave)
  await user.click(screen.getByRole('button', { name: /continuar/i }))
}

async function resolverCodigoDeCorreo(user, codigo = '654321') {
  await user.type(await screen.findByLabelText(/recibido por correo/i), codigo)
  await user.click(screen.getByRole('button', { name: /verificar/i }))
}

async function resolverCodigoDeAutenticador(user, codigo = '123456') {
  await user.type(await screen.findByLabelText(/código de microsoft authenticator/i), codigo)
  await user.click(screen.getByRole('button', { name: /verificar/i }))
}

async function completarLogin(user, email) {
  await escribirCredenciales(user, email, CLAVE)
  await resolverCodigoDeCorreo(user)
  await resolverCodigoDeAutenticador(user)
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('Login (HU2): redirección por rol', () => {
  it('login_administrador_redirigeAlPanelDeAdmin', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await completarLogin(user, 'admin@esibuy.test')

    await esperarRuta('/admin')
  })

  it('login_vendedor_redirigeASusProductos', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await completarLogin(user, 'vendedor@esibuy.test')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/seller/products$')))
  })

  it('login_cliente_redirigeALosProductos', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await completarLogin(user, 'cliente@esibuy.test')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/products$')))
  })

  it('login_tras_completarse_noGuardaNadaEnElAlmacenamientoDelNavegador', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await completarLogin(user, 'admin@esibuy.test')
    await esperarRuta('/admin')

    expect(window.localStorage).toHaveLength(0)
    expect(window.sessionStorage).toHaveLength(0)
  })
})

describe('Login (HU2): paso 1, credenciales', () => {
  it('login_conCamposVacios_muestraTodosLosErroresAlaVez', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await user.click(await screen.findByRole('button', { name: /continuar/i }))

    expect(screen.getByText(/introduce tu correo electrónico/i)).toBeInTheDocument()
    expect(screen.getByText(/introduce tu contraseña/i)).toBeInTheDocument()
  })

  it('login_conContrasenaIncorrecta_muestraMensajeGenerico', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', 'otra-clave')

    expect(await screen.findByText(/no se ha podido iniciar sesión/i)).toBeInTheDocument()
  })

  it('login_conUsuarioInexistente_muestraElMismoMensajeQueConContrasenaIncorrecta', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'nadie@esibuy.test', CLAVE)

    expect(await screen.findByText(/no se ha podido iniciar sesión\. revisa tus datos/i)).toBeInTheDocument()
  })

  it('login_tras_fallar_vaciaElCampoContrasena', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', 'otra-clave')
    await screen.findByText(/no se ha podido iniciar sesión/i)

    expect(screen.getByLabelText(/contraseña/i)).toHaveValue('')
  })

  it('login_conCuentaDesactivada_muestraMensajeEspecifico', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'desactivado@esibuy.test', CLAVE)

    expect(await screen.findByText(/cuenta está desactivada/i)).toBeInTheDocument()
  })

  it('login_tras_cincoIntentosFallidos_muestraMensajeDeBloqueo', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    for (let intento = 0; intento < 5; intento++) {
      await escribirCredenciales(user, 'admin@esibuy.test', 'otra-clave')
      await screen.findByText(/no se ha podido iniciar sesión/i)
      await user.clear(screen.getByLabelText(/correo electrónico/i))
    }
    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)

    expect(await screen.findByText(/demasiados intentos/i)).toBeInTheDocument()
  })

  it('login_conErrorDeServidor_muestraMensajeDeConexionSinDetallesInternos', async () => {
    server.use(
      http.post('/api/auth/login', () =>
        HttpResponse.json({ message: 'NullPointerException en AuthService' }, { status: 500 }),
      ),
    )
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)

    expect(await screen.findByText(/no se ha podido conectar con el servidor/i)).toBeInTheDocument()
    expect(screen.queryByText(/nullpointer/i)).not.toBeInTheDocument()
  })
})

describe('Login (HU2): paso 2, código por correo', () => {
  it('login_conCodigoDeCorreoIncorrecto_muestraErrorYNoAvanza', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user, '000000')

    expect(await screen.findByText(/código no es válido o ha caducado/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/código de microsoft authenticator/i)).not.toBeInTheDocument()
  })

  it('login_enElCampoDeCodigo_ignoraLetrasYLimitaA6Digitos', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    const campo = await screen.findByLabelText(/recibido por correo/i)
    await user.type(campo, '12ab34567890')

    expect(campo).toHaveValue('123456')
  })

  it('login_conCodigoIncompleto_avisaSinLlamarAlServidor', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await user.type(await screen.findByLabelText(/recibido por correo/i), '123')
    await user.click(screen.getByRole('button', { name: /verificar/i }))

    expect(screen.getByText(/el código tiene 6 dígitos/i)).toBeInTheDocument()
  })

  it('volverAlInicio_desdeElCodigo_regresaAlFormularioDeCredenciales', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await screen.findByLabelText(/recibido por correo/i)
    await user.click(screen.getByRole('button', { name: /volver al inicio/i }))

    expect(await screen.findByLabelText(/correo electrónico/i)).toBeInTheDocument()
  })
})

describe('Login (HU2): paso 3, Microsoft Authenticator', () => {
  it('login_conCodigoDeAutenticadorIncorrecto_muestraErrorYNoAvanza', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)
    await resolverCodigoDeAutenticador(user, '000000')

    expect(await screen.findByText(/código de microsoft authenticator no es válido/i)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/)
  })

  it('login_conCodigoDeAutenticadorIncompleto_avisaSinLlamarAlServidor', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)
    await user.type(await screen.findByLabelText(/código de microsoft authenticator/i), '123')
    await user.click(screen.getByRole('button', { name: /verificar/i }))

    expect(screen.getByText(/el código tiene 6 dígitos/i)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/)
  })

  it('login_alReintentarTrasUnCodigoDeAutenticadorFallido_permiteIntroducirloDeNuevo', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'admin@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)
    await resolverCodigoDeAutenticador(user, '000000')
    await screen.findByText(/código de microsoft authenticator no es válido/i)
    await resolverCodigoDeAutenticador(user)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/admin$')))
  })
})

describe('Rutas protegidas y sesión', () => {
  it('rutaDeAdmin_sinSesion_redirigeAlLogin', async () => {
    renderApp('/admin')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/login$')))
  })

  it('rutaDeAdmin_conSesionDeCliente_redirigeASusProductos', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/admin')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/products$')))
  })

  it('rutaDeVendedor_conSesionDeAdmin_redirigeAlPanelDeAdmin', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/seller/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/admin$')))
  })

  it('login_conSesionYaIniciada_redirigeSegunElRol', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/login')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/seller/products$')))
  })

  it('productos_sinSesion_esUnaRutaPublica', async () => {
    renderApp('/products')

    expect(await screen.findByRole('link', { name: /iniciar sesión/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })
})
