import { describe, it, expect } from 'vitest'
import { fireEvent, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

const CLAVE = 'Mock-Clave-1!'

async function escribirCredenciales(user, email, clave) {
  await user.type(await screen.findByLabelText(/correo electrónico/i), email)
  await user.type(screen.getByLabelText(/^contraseña/i), clave)
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

    expect(screen.getByLabelText(/^contraseña/i)).toHaveValue('')
  })

  it('HU2-CP28: una cuenta bloqueada muestra su mensaje antes del código del correo y no avanza al 2FA', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'bloqueado@esibuy.test', CLAVE)

    expect(await screen.findByText(/cuenta está bloqueada/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/correo electrónico/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/recibido por correo/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/código de microsoft authenticator/i)).not.toBeInTheDocument()
  })

  it('HU2-CP28: el mensaje de cuenta bloqueada no es el de demasiados intentos ni el genérico', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'bloqueado@esibuy.test', CLAVE)
    await screen.findByText(/cuenta está bloqueada/i)

    expect(screen.queryByText(/demasiados intentos/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/no se ha podido iniciar sesión/i)).not.toBeInTheDocument()
  })

  it('login_cuentaPendiente_muestraLaPantallaDeActivacionConSuCorreo', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'pendiente@esibuy.test', CLAVE)

    expect(await screen.findByRole('heading', { name: /pendiente de activación/i })).toBeInTheDocument()
    expect(screen.getByText('pendiente@esibuy.test')).toBeInTheDocument()
    expect(screen.getByText(/verificación por un administrador/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/recibido por correo/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/^contraseña/i)).not.toBeInTheDocument()
  })

  it('login_cuentaPendiente_noMuestraUnMensajeDeError', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'pendiente@esibuy.test', CLAVE)
    await screen.findByRole('heading', { name: /pendiente de activación/i })

    expect(screen.queryByText(/no se ha podido iniciar sesión/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/bloqueada/i)).not.toBeInTheDocument()
  })

  it('login_cuentaPendiente_volverRecuperaElFormularioVacio', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'pendiente@esibuy.test', CLAVE)
    await user.click(await screen.findByRole('button', { name: /volver al inicio de sesión/i }))

    expect(await screen.findByLabelText(/correo electrónico/i)).toHaveValue('')
    expect(screen.getByLabelText(/^contraseña/i)).toHaveValue('')
    expect(screen.queryByRole('heading', { name: /pendiente de activación/i })).not.toBeInTheDocument()
  })

  it('login_cuentaPendienteConContrasenaIncorrecta_noRevelaQueEstaPendiente', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'pendiente@esibuy.test', 'otra-clave')

    expect(await screen.findByText(/no se ha podido iniciar sesión/i)).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: /pendiente de activación/i })).not.toBeInTheDocument()
  })

  it('login_cuentaPendiente_unaVezActivadaPermiteEntrarTrasVolver', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'pendiente@esibuy.test', CLAVE)
    await user.click(await screen.findByRole('button', { name: /volver al inicio de sesión/i }))
    await completarLogin(user, 'cliente@esibuy.test')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/products$')))
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

// Alta de Microsoft Authenticator en el primer login (HU2, T20): tras el código del correo, un usuario
// sin Authenticator escanea un QR y confirma el alta con su primer código, que también abre la sesión.
describe('Login (HU2): alta de Microsoft Authenticator', () => {
  it('login_sinAuthenticator_trasElCodigoDeCorreo_muestraElQrDelAlta', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'nuevo@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)

    const qr = await screen.findByRole('img', { name: /código qr.*microsoft authenticator/i })
    expect(qr).toHaveAttribute('src', expect.stringMatching(/^data:image\/png;base64,/))
    expect(screen.getByText(/escanea/i)).toBeInTheDocument()
  })

  it('login_sinAuthenticator_conElPrimerCodigo_confirmaElAltaYEntra', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'nuevo@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)
    await screen.findByRole('img', { name: /código qr.*microsoft authenticator/i })
    await resolverCodigoDeAutenticador(user)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/))
  })

  it('login_sinAuthenticator_conUnPrimerCodigoIncorrecto_muestraErrorYSigueEnElAlta', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'nuevo@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)
    await screen.findByRole('img', { name: /código qr.*microsoft authenticator/i })
    await resolverCodigoDeAutenticador(user, '000000')

    expect(await screen.findByText(/código de microsoft authenticator no es válido/i)).toBeInTheDocument()
    expect(screen.getByRole('img', { name: /código qr.*microsoft authenticator/i })).toBeInTheDocument()
  })

  it('login_conAuthenticatorYaDadoDeAlta_noMuestraElQr', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'cliente@esibuy.test', CLAVE)
    await resolverCodigoDeCorreo(user)

    await screen.findByLabelText(/código de microsoft authenticator/i)
    expect(screen.queryByRole('img', { name: /código qr/i })).not.toBeInTheDocument()
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

  it('productos_sinSesion_llevaAIniciarSesion', async () => {
    renderApp('/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/login$')))
  })

  it('HU43-CP07: si la sesión caduca, vuelve al login avisando de que ha caducado', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    // El servidor ha cerrado la sesión por inactividad: la siguiente petición recibe 401.
    server.use(http.get('/api/perfil', () => new HttpResponse(null, { status: 401 })))
    renderApp('/profile')

    await esperarRuta('/login')
    expect(await screen.findByText(/tu sesión ha caducado/i)).toBeInTheDocument()
    expect(screen.queryByText('Has cerrado sesión.')).not.toBeInTheDocument()
  })

  it('HU37-CP19: si otro administrador elimina tu cuenta, tu siguiente petición da 401 y vuelves al login con el aviso', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    // CuentaActivaFilter ya no encuentra la cuenta borrada: invalida la sesión y responde 401 a lo siguiente que pide.
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 401 })))
    renderApp('/admin/admins')

    await esperarRuta('/login')
    expect(await screen.findByText(/tu sesión ha caducado/i)).toBeInTheDocument()
  })

  it('HU43-CP07: una contraseña incorrecta (401) no se toma por una sesión caducada', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await escribirCredenciales(user, 'cliente@esibuy.test', 'Otra-Clave-1!')

    expect(await screen.findByText(/no se ha podido iniciar sesión/i)).toBeInTheDocument()
    expect(screen.queryByText(/tu sesión ha caducado/i)).not.toBeInTheDocument()
  })

  // Fallos de la demo del Sprint 1: en el login no se podía ver la contraseña escrita
  it('HU2-CP27: el campo de la contraseña tiene un botón para mostrarla y ocultarla', async () => {
    const user = userEvent.setup()
    renderApp('/login')
    const contrasena = await screen.findByLabelText(/^contraseña/i)
    expect(contrasena).toHaveAttribute('type', 'password')

    await user.click(screen.getByRole('button', { name: /mostrar contraseña/i }))

    expect(contrasena).toHaveAttribute('type', 'text')
    await user.click(screen.getByRole('button', { name: /ocultar contraseña/i }))
    expect(contrasena).toHaveAttribute('type', 'password')
  })
})

describe('Recuperación de contraseña (HU3)', () => {
  it('HU3-CP18: el inicio de sesión tiene un enlace a la recuperación de contraseña', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await user.click(await screen.findByRole('link', { name: /¿has olvidado tu contraseña\?/i }))

    await esperarRuta('/forgot-password')
    expect(await screen.findByRole('heading', { name: /recuperar contraseña/i })).toBeInTheDocument()
  })

  // R9 de la revisión del Sprint 1: la contraseña no se puede copiar, pero sí pegar (gestores de contraseñas)
  it('HU2-CP31: la contraseña del login no se puede copiar ni cortar', async () => {
    const user = userEvent.setup()
    renderApp('/login')
    await user.type(await screen.findByLabelText(/^contraseña/i), CLAVE)
    const contrasena = screen.getByLabelText(/^contraseña/i)

    // fireEvent devuelve false cuando el evento se ha cancelado
    expect(fireEvent.copy(contrasena)).toBe(false)
    expect(fireEvent.cut(contrasena)).toBe(false)
  })

  it('HU2-CP31: tampoco se puede copiar ni cortar con la contraseña visible', async () => {
    const user = userEvent.setup()
    renderApp('/login')
    await user.type(await screen.findByLabelText(/^contraseña/i), CLAVE)

    await user.click(screen.getByRole('button', { name: /mostrar contraseña/i }))
    const contrasena = screen.getByLabelText(/^contraseña/i)

    expect(contrasena).toHaveAttribute('type', 'text')
    expect(fireEvent.copy(contrasena)).toBe(false)
    expect(fireEvent.cut(contrasena)).toBe(false)
  })

  it('HU2-CP31: sí se puede pegar la contraseña y el login sigue funcionando', async () => {
    const user = userEvent.setup()
    renderApp('/login')
    await user.type(await screen.findByLabelText(/correo electrónico/i), 'admin@esibuy.test')
    await user.click(screen.getByLabelText(/^contraseña/i))
    await user.paste(CLAVE)
    expect(screen.getByLabelText(/^contraseña/i)).toHaveValue(CLAVE)
    await user.click(screen.getByRole('button', { name: /continuar/i }))
    await resolverCodigoDeCorreo(user)
    await resolverCodigoDeAutenticador(user)

    await esperarRuta('/admin')
  })

  it('HU2-CP31: escribiendo la contraseña a mano el login sigue funcionando', async () => {
    const user = userEvent.setup()
    renderApp('/login')

    await completarLogin(user, 'cliente@esibuy.test')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/products$')))
  })
})
