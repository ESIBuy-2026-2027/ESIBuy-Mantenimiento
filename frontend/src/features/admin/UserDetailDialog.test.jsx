import { describe, it, expect, vi } from 'vitest'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserDetailDialog from './UserDetailDialog'

// Del mock de usuarios: Lucía Torres Vega (usr-4) es clienta bloqueada, Carlos Moreno Gil (usr-3) cliente
// activo, Diego Delgado Iglesias (usr-11) administrador bloqueado y Gonzalo Ibáñez Roca (usr-25) cliente
// pendiente de verificación (HU1).
const LUCIA = 'usr-4'
const CARLOS = 'usr-3'
const DIEGO = 'usr-11'
const GONZALO = 'usr-25'
const ESPERA = { timeout: 3000 }

async function abrirFicha(userId) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onClose: vi.fn(), onStatusChange: vi.fn() }
  renderWithProviders(<UserDetailDialog userId={userId} {...handlers} />)
  const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
  await within(dialogo).findByLabelText('Nombre')
  return { dialogo, ...handlers }
}

const valorDe = (dialogo, etiqueta) => within(dialogo).getByLabelText(etiqueta)
const botonDeEstado = (dialogo) =>
  within(dialogo).queryByRole('button', { name: /bloquear|desbloquear|activar|desactivar/i })

describe('UserDetailDialog (HU20): datos', () => {
  it('userDetailDialog_deUnCliente_muestraSusDatosEnSoloLectura', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    expect(valorDe(dialogo, 'Nombre')).toHaveValue('Lucía')
    expect(valorDe(dialogo, 'Apellidos')).toHaveValue('Torres Vega')
    expect(valorDe(dialogo, 'Email')).toHaveValue('lucia.torres@esibuy.test')
    expect(valorDe(dialogo, 'DNI').value).toMatch(/^\d{8}[A-Z]$/)
    expect(valorDe(dialogo, 'Teléfono').value).toMatch(/^6\d{8}$/)
    expect(valorDe(dialogo, 'Fecha de nacimiento').value).toMatch(/^\d{2}\/\d{2}\/\d{4}$/)
    expect(valorDe(dialogo, 'Tipo de cliente')).toHaveValue('Estándar')
    expect(valorDe(dialogo, 'Imagen de perfil')).toHaveValue('Avatar 2')
    expect(valorDe(dialogo, 'Estado')).toHaveValue('Bloqueado')
  })

  it('userDetailDialog_deUnCliente_noMuestraLaFechaDeAlta', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    expect(within(dialogo).queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnAdministrador_muestraSedeYFechaDeIncorporacion', async () => {
    const { dialogo } = await abrirFicha(DIEGO)

    expect(valorDe(dialogo, 'Nombre')).toHaveValue('Diego')
    expect(valorDe(dialogo, 'Sede')).toHaveValue('Cuenca')
    expect(valorDe(dialogo, 'Fecha de incorporación')).toHaveValue('20/01/2026')
    expect(valorDe(dialogo, 'Fecha de alta')).toHaveValue('20/01/2026')
    expect(within(dialogo).queryByLabelText('DNI')).not.toBeInTheDocument()
    expect(within(dialogo).queryByLabelText('Tipo de cliente')).not.toBeInTheDocument()
  })

  it('userDetailDialog_noMuestraLaContrasena', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    expect(within(dialogo).queryByLabelText(/password|contraseña/i)).not.toBeInTheDocument()
    expect(within(dialogo).queryByText(/password|contraseña/i)).not.toBeInTheDocument()
  })

  it('userDetailDialog_todosLosCamposSonDeSoloLecturaYAvisaDeEllo', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    within(dialogo)
      .getAllByRole('textbox')
      .forEach((campo) => expect(campo).toHaveAttribute('readonly'))
    expect(within(dialogo).getByText(/modo solo lectura/i)).toBeInTheDocument()
  })

  it('userDetailDialog_alPulsarCerrar_avisaDelCierre', async () => {
    const user = userEvent.setup()
    const { dialogo, onClose } = await abrirFicha(LUCIA)

    await user.click(within(dialogo).getByRole('button', { name: /cerrar/i }))

    expect(onClose).toHaveBeenCalled()
  })

  it('userDetailDialog_conErrorAlCargar_muestraMensajeYPermiteReintentar', async () => {
    server.use(http.get('/api/admin/usuarios/:id', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<UserDetailDialog userId={LUCIA} onClose={vi.fn()} onStatusChange={vi.fn()} />)
    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })

    expect(await within(dialogo).findByText(/no se ha podido cargar la información del usuario/i)).toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: /reintentar/i }))

    expect(await within(dialogo).findByLabelText('Nombre')).toHaveValue('Lucía')
  })
})

describe('UserDetailDialog (HU19): la ficha no cambia el estado', () => {
  it('userDetailDialog_deUnaCuentaActiva_noOfreceBloquear', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(botonDeEstado(dialogo)).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnaCuentaBloqueada_noOfreceDesbloquearNiActivar', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    expect(valorDe(dialogo, 'Estado')).toHaveValue('Bloqueado')
    expect(botonDeEstado(dialogo)).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnaCuentaPendiente_noOfreceActivar', async () => {
    const { dialogo } = await abrirFicha(GONZALO)

    expect(valorDe(dialogo, 'Estado')).toHaveValue('Pendiente')
    expect(botonDeEstado(dialogo)).not.toBeInTheDocument()
  })
})

describe('UserDetailDialog (HU22): edición', () => {
  const ADA = 'u-1'

  async function entrarEnEdicion(userId, currentUserId) {
    const ficha = await abrirFicha(userId, currentUserId)
    const user = userEvent.setup()
    await user.click(within(ficha.dialogo).getByRole('button', { name: 'Editar' }))
    await within(ficha.dialogo).findByText('Modo edición')
    return { ...ficha, user }
  }

  function capturarGuardado(estado = 200) {
    const envio = { cuerpo: null, llamadas: 0, id: null }
    server.use(
      http.put('/api/admin/usuarios/:id', async ({ params, request }) => {
        envio.llamadas += 1
        envio.id = params.id
        envio.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id }, { status: estado })
      }),
    )
    return envio
  }

  const esDeSoloLectura = (dialogo, etiqueta) => within(dialogo).getByLabelText(etiqueta).hasAttribute('readonly')
  const guardar = (user, dialogo) => user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

  it('userDetailDialog_alPulsarEditar_pasaAlModoEdicionSinLosBotonesDeLaFicha', async () => {
    const { dialogo } = await entrarEnEdicion(CARLOS)

    expect(within(dialogo).getByRole('button', { name: /^guardar$/i })).toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: /^cancelar$/i })).toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
    expect(botonDeEstado(dialogo)).toBeNull()
    expect(within(dialogo).queryByText('Modo solo lectura')).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnCliente_enEdicionSoloDejaModificarLoQueSePuede', async () => {
    const { dialogo } = await entrarEnEdicion(CARLOS)

    for (const etiqueta of [/^nombre/i, /^apellidos/i, /^dni/i, /^teléfono/i, /^fecha de nacimiento/i]) {
      expect(esDeSoloLectura(dialogo, etiqueta)).toBe(false)
    }
    expect(esDeSoloLectura(dialogo, 'Email')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Estado')).toBe(true)
    expect(within(dialogo).queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnAdministrador_enEdicionNoDejaModificarEmailNiFechaDeIncorporacion', async () => {
    const { dialogo } = await entrarEnEdicion(DIEGO)

    expect(esDeSoloLectura(dialogo, 'Email')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Fecha de incorporación')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Fecha de alta')).toBe(true)
    expect(esDeSoloLectura(dialogo, /^sede/i)).toBe(false)
    expect(within(dialogo).queryByLabelText(/^dni/i)).not.toBeInTheDocument()
  })

  it('userDetailDialog_enEdicion_nuncaMuestraLaContrasena', async () => {
    const { dialogo } = await entrarEnEdicion(CARLOS)

    expect(within(dialogo).queryByLabelText(/contraseña|password/i)).not.toBeInTheDocument()
  })

  it('userDetailDialog_alGuardarUnCliente_enviaSusDatosSinElEmailNiElEstado', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Carla')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(envio.id).toBe(CARLOS)
    expect(Object.keys(envio.cuerpo)).toEqual([
      'nombre',
      'apellidos',
      'dni',
      'telefono',
      'fechaNacimiento',
      'tipoCliente',
      'avatar',
    ])
    expect(envio.cuerpo).toMatchObject({ nombre: 'Carla', apellidos: 'Moreno Gil', tipoCliente: 'PREMIUM' })
  })

  it('userDetailDialog_tras_guardar_vuelveALaFichaConLosDatosNuevosYAvisaAlListado', async () => {
    const { dialogo, user, onStatusChange } = await entrarEnEdicion(CARLOS)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Carla')
    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Los cambios se han guardado.', undefined, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(valorDe(dialogo, 'Nombre')).toHaveValue('Carla'), ESPERA)
    expect(within(dialogo).getByText('Modo solo lectura')).toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: 'Editar' })).toBeInTheDocument()
    expect(onStatusChange).toHaveBeenCalled()
  })

  it('userDetailDialog_alCambiarElTipoDeCliente_laFichaMuestraElNuevoTipo', async () => {
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await user.selectOptions(within(dialogo).getByLabelText(/^tipo de cliente/i), 'ESTANDAR')
    await guardar(user, dialogo)

    await waitFor(() => expect(valorDe(dialogo, 'Tipo de cliente')).toHaveValue('Estándar'), ESPERA)
  })

  it('userDetailDialog_alCambiarLaFechaDeNacimientoAOtraDeUnMayor_laGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    fireEvent.change(within(dialogo).getByLabelText(/^fecha de nacimiento/i), { target: { value: '1990-05-20' } })
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.fechaNacimiento).toBe('1990-05-20'), ESPERA)
  })

  it('userDetailDialog_conUnaFechaDeNacimientoDeUnMenor_muestraElErrorYNoGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    fireEvent.change(within(dialogo).getByLabelText(/^fecha de nacimiento/i), {
      target: { value: `${new Date().getFullYear() - 10}-01-01` },
    })
    await guardar(user, dialogo)

    expect(within(dialogo).getByText(/debe ser mayor de edad/i)).toBeInTheDocument()
    expect(envio.llamadas).toBe(0)
  })

  it('userDetailDialog_conCamposObligatoriosVacios_losMuestraTodosALaVezYNoGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.clear(within(dialogo).getByLabelText(/^apellidos/i))
    await user.clear(within(dialogo).getByLabelText(/^dni/i))
    await guardar(user, dialogo)

    expect(within(dialogo).getAllByText(/este campo es obligatorio/i)).toHaveLength(3)
    expect(envio.llamadas).toBe(0)
  })

  it('userDetailDialog_alPulsarCancelar_vuelveALaFichaSinGuardarNada', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'XYZ')
    await user.click(within(dialogo).getByRole('button', { name: /^cancelar$/i }))

    expect(await within(dialogo).findByText('Modo solo lectura')).toBeInTheDocument()
    expect(valorDe(dialogo, 'Nombre')).toHaveValue('Carlos')
    expect(envio.llamadas).toBe(0)
  })

  it('userDetailDialog_conErrorDelServidor_muestraElMensajeYSigueEnEdicion', async () => {
    server.use(http.put('/api/admin/usuarios/:id', () => HttpResponse.json({ message: 'Mongo' }, { status: 500 })))
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await guardar(user, dialogo)

    expect(await within(dialogo).findByText(/no se han podido guardar los cambios/i, undefined, ESPERA)).toBeInTheDocument()
    expect(within(dialogo).queryByText(/mongo/i)).not.toBeInTheDocument()
    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
  })

  it('userDetailDialog_alEditarLaSedeDeUnAdministrador_laFichaMuestraLaNueva', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(DIEGO)

    await user.clear(within(dialogo).getByLabelText(/^sede/i))
    await user.type(within(dialogo).getByLabelText(/^sede/i), 'Albacete')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(Object.keys(envio.cuerpo)).toEqual(['nombre', 'apellidos', 'sede', 'avatar'])
    expect(envio.cuerpo.sede).toBe('Albacete')
  })

  it('userDetailDialog_laSedeDeUnAdministradorPuedeQuedarVacia', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(DIEGO)

    await user.clear(within(dialogo).getByLabelText(/^sede/i))
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.sede).toBe(''), ESPERA)
  })

  it('userDetailDialog_unAdministradorPuedeEditarSusPropiosDatos', async () => {
    const { dialogo, user } = await entrarEnEdicion(ADA, ADA)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Adela')
    await guardar(user, dialogo)

    await waitFor(() => expect(valorDe(dialogo, 'Nombre')).toHaveValue('Adela'), ESPERA)
  })
})

describe('UserDetailDialog (HU22): el rol no se puede cambiar', () => {
  const ADA = 'u-1'

  async function entrarEnEdicion(userId, currentUserId = ADA) {
    const ficha = await abrirFicha(userId, currentUserId)
    const user = userEvent.setup()
    await user.click(within(ficha.dialogo).getByRole('button', { name: 'Editar' }))
    await within(ficha.dialogo).findByLabelText(/^rol/i, undefined, ESPERA)
    return { ...ficha, user }
  }

  const guardar = (user, dialogo) => user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

  function capturarGuardado() {
    const envio = { cuerpo: null, llamadas: 0 }
    server.use(
      http.put('/api/admin/usuarios/:id', async ({ request }) => {
        envio.llamadas += 1
        envio.cuerpo = await request.json()
        return HttpResponse.json({ id: 'x' })
      }),
    )
    return envio
  }

  it('userDetailDialog_enModoLectura_muestraElRolDeLaCuenta', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(valorDe(dialogo, 'Rol')).toHaveValue('Cliente')
  })

  it.each([
    ['un cliente', CARLOS, 'Cliente'],
    ['otro administrador', DIEGO, 'Administrador'],
    ['el propio administrador', ADA, 'Administrador'],
  ])('userDetailDialog_elRolEsDeSoloLectura_enLaFichaDe %s', async (_caso, userId, texto) => {
    const { dialogo } = await entrarEnEdicion(userId)

    expect(valorDe(dialogo, 'Rol')).toHaveValue(texto)
    expect(valorDe(dialogo, 'Rol')).toHaveAttribute('readonly')
  })

  it('userDetailDialog_alGuardar_noEnviaElRolNiElEmail', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion(CARLOS)

    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(envio.cuerpo).not.toHaveProperty('rol')
    expect(envio.cuerpo).not.toHaveProperty('email')
  })
})
