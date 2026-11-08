import { describe, it, expect } from 'vitest'
import { cleanup, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { login } from '../api/auth'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Del mock del perfil: Carlos (cliente@esibuy.test) es cliente Premium con el avatar 1; Vera (vendedor@esibuy.test)
// es vendedora de Tecnología con el avatar 2; Vicente (vendedor-vacio@esibuy.test) no tiene teléfono ni avatar.
async function abrirPerfil(email) {
  await abrirSesionPorApi(email)
  renderApp('/profile')
  await screen.findByRole('heading', { level: 1, name: /mi perfil/i })
  await screen.findByLabelText('Nombre')
}

const valorDe = (etiqueta) => screen.getByLabelText(etiqueta)

describe('ProfilePage (HU4): datos del perfil', () => {
  it('HU4-CP05: el cliente ve sus datos personales y los de cliente', async () => {
    await abrirPerfil('cliente@esibuy.test')

    expect(valorDe('Nombre')).toHaveValue('Carlos')
    expect(valorDe('Apellidos')).toHaveValue('Cliente')
    expect(valorDe('Email')).toHaveValue('cliente@esibuy.test')
    expect(valorDe('Rol')).toHaveValue('Cliente')
    expect(valorDe('DNI')).toHaveValue('87654321X')
    expect(valorDe('Teléfono')).toHaveValue('611222333')
    expect(valorDe('Fecha de nacimiento')).toHaveValue('14/05/1998')
    expect(valorDe('Tipo de cliente')).toHaveValue('Premium')
    expect(valorDe('Fecha de alta')).toHaveValue('10/04/2026')
    expect(screen.queryByLabelText('Nombre comercial')).not.toBeInTheDocument()
  })

  it('HU4-CP05: los datos del perfil son de solo lectura', async () => {
    await abrirPerfil('cliente@esibuy.test')

    screen.getAllByRole('textbox').forEach((campo) => expect(campo).toHaveAttribute('readonly'))
    expect(screen.queryByRole('button', { name: /guardar/i })).not.toBeInTheDocument()
  })

  it('HU4-CP06: el vendedor ve sus datos y los de vendedor, con el nombre de su categoría', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(valorDe('Nombre')).toHaveValue('Vera')
    expect(valorDe('Rol')).toHaveValue('Vendedor')
    expect(valorDe('Nombre comercial')).toHaveValue('Tienda de Vera')
    expect(await screen.findByDisplayValue('Tecnología')).toBe(valorDe('Categoría principal'))
    expect(valorDe('DNI')).toHaveValue('12345678Z')
    expect(screen.queryByLabelText('Fecha de nacimiento')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Tipo de cliente')).not.toBeInTheDocument()
  })

  it('HU4-CP06: los datos opcionales que no tiene se muestran con un guion', async () => {
    await abrirPerfil('vendedor-vacio@esibuy.test')

    expect(valorDe('Teléfono')).toHaveValue('—')
  })

  it('HU4-CP07: muestra como imagen el avatar que eligió el usuario', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(screen.getByRole('img', { name: 'Avatar 2' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 2, name: 'Vera Vendedora' })).toBeInTheDocument()
  })

  it('HU4-CP07: sin avatar elegido muestra sus iniciales', async () => {
    await abrirPerfil('vendedor-vacio@esibuy.test')

    const avatar = screen.getByRole('img', { name: /sin imagen de perfil/i })
    expect(avatar).toHaveTextContent('VV')
  })

  it('HU4-CP08: no muestra la contraseña ni el estado de la cuenta', async () => {
    await abrirPerfil('cliente@esibuy.test')

    expect(screen.queryByLabelText(/password|contraseña/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/password|contraseña/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Estado')).not.toBeInTheDocument()
  })

  it('HU4-CP09: si el perfil no carga, avisa sin detalles internos y permite reintentar', async () => {
    server.use(
      http.get('/api/perfil', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }), {
        once: true,
      }),
    )
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/profile')

    const aviso = await screen.findByText(/no se ha podido cargar tu perfil/i)
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(aviso).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByLabelText('Nombre')).toHaveValue('Carlos')
  })
})

// Preferencias del cliente (HU46, T286): las categorías que le interesan. Del mock de categorías: Tecnología (cat-1) y
// Hogar (cat-2), las dos activas.
describe('ProfilePage (HU46): preferencias', () => {
  const seccion = () => screen.findByRole('group', { name: 'Mis preferencias' })

  it('HU46-CP20: el cliente ve sus preferencias, con una casilla por cada categoría activa', async () => {
    await abrirPerfil('cliente@esibuy.test')

    const preferencias = await seccion()
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
    expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).not.toBeChecked()
  })

  it('HU46-CP20: las categorías que ya eligió aparecen marcadas', async () => {
    server.use(http.get('/api/customer/preferences', () => HttpResponse.json({ categorias: ['cat-2'] })))
    await abrirPerfil('cliente@esibuy.test')

    const preferencias = await seccion()
    await waitFor(() => expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).toBeChecked())
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
  })

  it('HU46-CP21: al marcar una categoría se guarda al momento y se avisa', async () => {
    const user = userEvent.setup()
    let cuerpo = null
    server.use(
      http.put('/api/customer/preferences', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json(cuerpo)
      }),
    )
    await abrirPerfil('cliente@esibuy.test')
    const preferencias = await seccion()

    await user.click(within(preferencias).getByRole('checkbox', { name: 'Tecnología' }))

    expect(await screen.findByText('Preferencias guardadas.')).toBeInTheDocument()
    expect(cuerpo).toEqual({ categorias: ['cat-1'] })
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).toBeChecked()
  })

  it('HU46-CP21: al desmarcar una categoría elegida se guarda sin ella', async () => {
    const user = userEvent.setup()
    let cuerpo = null
    server.use(
      http.get('/api/customer/preferences', () => HttpResponse.json({ categorias: ['cat-2'] })),
      http.put('/api/customer/preferences', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json(cuerpo)
      }),
    )
    await abrirPerfil('cliente@esibuy.test')
    const preferencias = await seccion()
    await waitFor(() => expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).toBeChecked())

    await user.click(within(preferencias).getByRole('checkbox', { name: 'Hogar' }))

    expect(await screen.findByText('Preferencias guardadas.')).toBeInTheDocument()
    expect(cuerpo).toEqual({ categorias: [] })
    expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).not.toBeChecked()
  })

  it('HU46-CP21: mientras se guarda, las casillas quedan deshabilitadas', async () => {
    const user = userEvent.setup()
    server.use(
      http.put('/api/customer/preferences', async () => {
        await delay('infinite')
      }),
    )
    await abrirPerfil('cliente@esibuy.test')
    const preferencias = await seccion()

    await user.click(within(preferencias).getByRole('checkbox', { name: 'Tecnología' }))

    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).toBeDisabled()
    expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).toBeDisabled()
  })

  it('HU46-CP21: al volver al perfil, las preferencias siguen guardadas', async () => {
    const user = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')
    await user.click(within(await seccion()).getByRole('checkbox', { name: 'Hogar' }))
    await screen.findByText('Preferencias guardadas.')
    cleanup()

    renderApp('/profile')

    const preferencias = await seccion()
    await waitFor(() => expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).toBeChecked())
  })

  it('HU46-CP22: si no se pueden guardar, lo avisa y la casilla vuelve a como estaba', async () => {
    const user = userEvent.setup()
    server.use(http.put('/api/customer/preferences', () => new HttpResponse(null, { status: 500 })))
    await abrirPerfil('cliente@esibuy.test')
    const preferencias = await seccion()

    await user.click(within(preferencias).getByRole('checkbox', { name: 'Tecnología' }))

    expect(
      await screen.findByText('No se han podido guardar tus preferencias. Inténtalo de nuevo.'),
    ).toBeInTheDocument()
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).toBeEnabled()
  })

  it('HU46-CP22: si no se pueden cargar, lo avisa sin mostrar las casillas y permite reintentar', async () => {
    const user = userEvent.setup()
    // Falla hasta que se pulsa Reintentar (la página puede pedirlas más de una vez al montarse)
    let falla = true
    server.use(
      http.get('/api/customer/preferences', () =>
        falla ? new HttpResponse(null, { status: 500 }) : HttpResponse.json({ categorias: ['cat-1'] }),
      ),
    )
    await abrirPerfil('cliente@esibuy.test')

    expect(await screen.findByText('No se han podido cargar tus preferencias. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
    falla = false
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    const preferencias = await seccion()
    await waitFor(() => expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).toBeChecked())
  })

  it('HU46-CP20: el vendedor no tiene preferencias', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(screen.queryByRole('group', { name: 'Mis preferencias' })).not.toBeInTheDocument()
  })
})

describe('ProfilePage (HU4): acceso', () => {
  it('HU4-CP10: sin sesión, redirige al login', async () => {
    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU4-CP10: el administrador no tiene perfil que consultar', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
    expect(screen.queryByRole('heading', { name: /mi perfil/i })).not.toBeInTheDocument()
  })
})

describe('ProfilePage (HU47): eliminar mi cuenta', () => {
  it('HU47-CP13: el vendedor tiene la opción de eliminar su cuenta en su perfil', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(screen.getByRole('region', { name: /^eliminar mi cuenta$/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^eliminar mi cuenta$/i })).toBeInTheDocument()
  })

  it('HU47-CP14: tras eliminar la cuenta se cierra la sesión y el login avisa de que se ha eliminado', async () => {
    server.use(http.delete('/api/perfil', () => new HttpResponse(null, { status: 204 })))
    const usuario = userEvent.setup()
    await abrirPerfil('vendedor@esibuy.test')

    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))
    const dialogo = await screen.findByRole('alertdialog', { name: /eliminar mi cuenta/i })
    await usuario.type(within(dialogo).getByLabelText(/^contraseña actual/i), 'Contraseña-de-prueba-1!')
    await usuario.click(within(dialogo).getByRole('button', { name: /^eliminar mi cuenta$/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
    expect(await screen.findByText('Tu cuenta se ha eliminado.')).toBeInTheDocument()
  })
})

describe('ProfilePage (HU25): eliminar mi cuenta de cliente', () => {
  it('HU25-CP07: el cliente tiene la opción de eliminar su cuenta en su perfil, igual que el vendedor', async () => {
    await abrirPerfil('cliente@esibuy.test')

    const seccion = screen.getByRole('region', { name: /^eliminar mi cuenta$/i })
    expect(within(seccion).getByRole('button', { name: /^eliminar mi cuenta$/i })).toBeInTheDocument()
    expect(seccion).toHaveTextContent(/pedidos y valoraciones se conservan/i)
  })

  it('HU25-CP07: la opción es del perfil propio: el administrador no la tiene, porque no tiene perfil', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
    expect(screen.queryByRole('region', { name: /^eliminar mi cuenta$/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^eliminar mi cuenta$/i })).not.toBeInTheDocument()
  })

  it('HU25-CP07: al pulsar el botón se abre la confirmación y al cancelarla no se elimina nada', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')

    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))
    const dialogo = await screen.findByRole('alertdialog', { name: /eliminar mi cuenta/i })
    await usuario.click(within(dialogo).getByRole('button', { name: /cancelar/i }))

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/profile$/)
  })
})

// Contraseña de las cuentas de ejemplo de los mocks: la que el mock da por buena al confirmar la eliminación
const CLAVE_DE_LOS_MOCKS = 'Mock-Clave-1!'

async function confirmarEliminacion(usuario, contrasena) {
  await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))
  const dialogo = await screen.findByRole('alertdialog', { name: /eliminar mi cuenta/i })
  await usuario.type(within(dialogo).getByLabelText(/^contraseña actual/i), contrasena)
  await usuario.click(within(dialogo).getByRole('button', { name: /^eliminar mi cuenta$/i }))
  return dialogo
}

describe('ProfilePage (HU25): confirmar la eliminación de la cuenta del cliente', () => {
  it('HU25-CP12: con la contraseña correcta se cierra la sesión, se va al login y se avisa de que la cuenta se ha eliminado', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')

    await confirmarEliminacion(usuario, CLAVE_DE_LOS_MOCKS)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
    expect(await screen.findByText('Tu cuenta se ha eliminado.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cerrar sesión/i })).not.toBeInTheDocument()
  })

  it('HU25-CP12: la zona del cliente deja de ser accesible tras eliminar la cuenta', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')
    await confirmarEliminacion(usuario, CLAVE_DE_LOS_MOCKS)
    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
    cleanup()

    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU25-CP12: con una contraseña incorrecta la sesión sigue abierta, se avisa en el campo y se queda en el perfil', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')

    const dialogo = await confirmarEliminacion(usuario, 'Otra-clave-1!')

    await waitFor(() =>
      expect(within(dialogo).getByLabelText(/^contraseña actual/i)).toHaveAccessibleDescription(
        'La contraseña actual no es correcta.',
      ),
    )
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/profile$/)
    expect(screen.getByRole('button', { name: /cerrar sesión/i, hidden: true })).toBeInTheDocument()
    expect(screen.queryByText('Tu cuenta se ha eliminado.')).not.toBeInTheDocument()
  })

  it('HU25-CP13: después de eliminarla no se puede iniciar sesión con esa cuenta, y las demás siguen entrando', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')
    await confirmarEliminacion(usuario, CLAVE_DE_LOS_MOCKS)
    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))

    await expect(login({ email: 'cliente@esibuy.test', password: CLAVE_DE_LOS_MOCKS })).rejects.toMatchObject({
      status: 401,
    })
    await expect(login({ email: 'vendedor@esibuy.test', password: CLAVE_DE_LOS_MOCKS })).resolves.toBeDefined()
  })

  it('HU25-CP13: eliminar la cuenta de un cliente no elimina la de otro', async () => {
    const usuario = userEvent.setup()
    await abrirPerfil('cliente@esibuy.test')
    await confirmarEliminacion(usuario, CLAVE_DE_LOS_MOCKS)
    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
    cleanup()

    await abrirPerfil('nuevo@esibuy.test')

    expect(screen.getByRole('region', { name: /^eliminar mi cuenta$/i })).toBeInTheDocument()
  })
})

// Edición del perfil propio (HU45, T103): en el propio panel del perfil, sin cambiar de página. Del mock: el PUT
// responde con las mismas reglas y códigos que el backend (PR #239) y guarda lo editado hasta que se reinician los mocks.
describe('ProfilePage (HU45): editar el perfil', () => {
  const editarPerfil = (user) => user.click(screen.getByRole('button', { name: /^editar perfil$/i }))
  const guardarCambios = (user) => user.click(screen.getByRole('button', { name: /^guardar cambios$/i }))
  const campoNombre = () => screen.getByLabelText(/^nombre(?! comercial)/i)
  const esDeSoloLectura = (etiqueta) => screen.getByLabelText(etiqueta).hasAttribute('readonly')

  async function abrirEdicion(email, user) {
    await abrirPerfil(email)
    await editarPerfil(user)
    await screen.findByRole('button', { name: /^guardar cambios$/i })
  }

  const escribir = async (user, campo, texto) => {
    await user.clear(campo)
    if (texto) await user.type(campo, texto)
  }

  // Guarda en `peticiones` el cuerpo de cada PUT que llega al backend
  const espiarPut = () => {
    const peticiones = []
    server.use(
      http.put('/api/perfil', async ({ request }) => {
        const cuerpo = await request.clone().json()
        peticiones.push(cuerpo)
        return HttpResponse.json({
          id: 'u-3',
          nombre: cuerpo.nombre,
          apellidos: cuerpo.apellidos,
          email: 'cliente@esibuy.test',
          rol: 'CLIENTE',
          dni: '87654321X',
          tipoCliente: 'PREMIUM',
          fechaAlta: '2026-04-10T17:40:00Z',
          ...cuerpo,
        })
      }),
    )
    return peticiones
  }

  it('HU45-CP11: el perfil tiene un botón para editarlo y, en lectura, no tiene el de guardar', async () => {
    await abrirPerfil('cliente@esibuy.test')

    expect(screen.getByRole('button', { name: /^editar perfil$/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /guardar cambios/i })).not.toBeInTheDocument()
  })

  it('HU45-CP11: el cliente edita en el propio panel sus datos y ve en solo lectura el email, el DNI, el tipo y el rol', async () => {
    const user = userEvent.setup()
    await abrirEdicion('cliente@esibuy.test', user)

    expect(screen.getByTestId('ruta-actual')).toHaveTextContent('/profile')
    expect(screen.getByRole('heading', { level: 1, name: /mi perfil/i })).toBeInTheDocument()
    expect(campoNombre()).toHaveValue('Carlos')
    expect(campoNombre()).not.toHaveAttribute('readonly')
    expect(screen.getByLabelText(/^teléfono/i)).toHaveValue('611222333')
    for (const etiqueta of ['Email', 'Rol', 'DNI', 'Tipo de cliente']) {
      expect(esDeSoloLectura(etiqueta)).toBe(true)
    }
    expect(screen.getByRole('button', { name: /^cancelar$/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^editar perfil$/i })).not.toBeInTheDocument()
  })

  it('HU45-CP11: el vendedor edita sus datos y su negocio y ve en solo lectura el email y el DNI', async () => {
    const user = userEvent.setup()
    await abrirEdicion('vendedor@esibuy.test', user)

    expect(screen.getByLabelText(/^nombre comercial/i)).toHaveValue('Tienda de Vera')
    expect(await screen.findByRole('combobox', { name: /^categoría principal/i })).toHaveValue('Tecnología')
    expect(esDeSoloLectura('Correo electrónico')).toBe(true)
    expect(esDeSoloLectura('DNI')).toBe(true)
  })

  it('HU45-CP12: al guardar envía todos los datos del cliente, vuelve a la lectura y avisa de que se ha guardado', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPut()
    await abrirEdicion('cliente@esibuy.test', user)

    await escribir(user, campoNombre(), 'Carla')
    await guardarCambios(user)

    expect(await screen.findByText('Tus cambios se han guardado.')).toBeInTheDocument()
    expect(peticiones).toEqual([
      {
        nombre: 'Carla',
        apellidos: 'Cliente',
        telefono: '611222333',
        fechaNacimiento: '1998-05-14',
        avatar: 'avatar1',
      },
    ])
    expect(screen.getByText('Tus cambios se han guardado.').closest('[role="status"]')).not.toBeNull()
    expect(screen.getByRole('heading', { level: 2, name: 'Carla Cliente' })).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveValue('Carla')
    expect(screen.getByLabelText('Nombre')).toHaveAttribute('readonly')
    expect(screen.queryByRole('button', { name: /guardar cambios/i })).not.toBeInTheDocument()
  })

  it('HU45-CP12: lo guardado es lo que muestra el perfil al volver a cargarlo', async () => {
    const user = userEvent.setup()
    await abrirEdicion('vendedor@esibuy.test', user)

    await escribir(user, screen.getByLabelText(/^nombre comercial/i), 'Tienda Nueva')
    await guardarCambios(user)
    await screen.findByText('Tus cambios se han guardado.')
    cleanup()
    await abrirPerfil('vendedor@esibuy.test')

    expect(valorDe('Nombre comercial')).toHaveValue('Tienda Nueva')
  })

  it('HU45-CP12: sin pulsar guardar no se envía nada, aunque se escriba en los campos', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPut()
    await abrirEdicion('cliente@esibuy.test', user)

    await escribir(user, campoNombre(), 'Carla')
    await escribir(user, screen.getByLabelText(/^teléfono/i), '699888777')

    expect(peticiones).toHaveLength(0)
    expect(screen.queryByText('Tus cambios se han guardado.')).not.toBeInTheDocument()
  })

  it('HU45-CP13: con un dato obligatorio vacío no se envía y el error sale en su campo', async () => {
    const user = userEvent.setup()
    const peticiones = espiarPut()
    await abrirEdicion('cliente@esibuy.test', user)

    await escribir(user, campoNombre(), '')
    await guardarCambios(user)

    expect(await screen.findByText('Este campo es obligatorio')).toBeInTheDocument()
    expect(peticiones).toHaveLength(0)
    expect(campoNombre()).toHaveFocus()
  })

  it('HU45-CP14: un nombre comercial repetido se avisa en su campo y se sigue editando', async () => {
    const user = userEvent.setup()
    await abrirEdicion('vendedor@esibuy.test', user)

    await escribir(user, screen.getByLabelText(/^nombre comercial/i), 'Casa Vicente')
    await guardarCambios(user)

    expect(await screen.findByText('Este nombre comercial ya está registrado')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^guardar cambios$/i })).toBeEnabled()
    expect(screen.queryByText('Tus cambios se han guardado.')).not.toBeInTheDocument()
  })

  it('HU45-CP14: si el servidor falla, avisa sin detalles internos y conserva lo escrito', async () => {
    server.use(
      http.put('/api/perfil', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 })),
    )
    const user = userEvent.setup()
    await abrirEdicion('cliente@esibuy.test', user)

    await escribir(user, campoNombre(), 'Carla')
    await guardarCambios(user)

    expect(await screen.findByText('No se han podido guardar los cambios. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(campoNombre()).toHaveValue('Carla')
  })
})
