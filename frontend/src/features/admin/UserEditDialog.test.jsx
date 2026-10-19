import { describe, it, expect, vi } from 'vitest'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserEditDialog from './UserEditDialog'

// Del mock de usuarios: Carlos Moreno Gil (usr-3) es un cliente activo, Diego Delgado Iglesias (usr-11) un
// administrador bloqueado y Ada Admin (u-1) el administrador con el que se inicia sesión.
const CARLOS = 'usr-3'
const DIEGO = 'usr-11'
const ADA = 'u-1'
const ESPERA = { timeout: 3000 }

async function abrirEdicion(userId) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onClose: vi.fn(), onSaved: vi.fn() }
  renderWithProviders(<UserEditDialog userId={userId} {...handlers} />)
  const dialogo = await screen.findByRole('dialog', { name: /editar usuario/i })
  await within(dialogo).findByLabelText(/^nombre/i, undefined, ESPERA)
  return { dialogo, user: userEvent.setup(), ...handlers }
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

const valorDe = (dialogo, etiqueta) => within(dialogo).getByLabelText(etiqueta)
const esDeSoloLectura = (dialogo, etiqueta) => valorDe(dialogo, etiqueta).hasAttribute('readonly')
const guardar = (user, dialogo) => user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

describe('UserEditDialog (HU22): formulario', () => {
  it('userEditDialog_alAbrirse_muestraElModoEdicionConGuardarYCancelar', async () => {
    const { dialogo } = await abrirEdicion(CARLOS)

    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: /^guardar$/i })).toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: /^cancelar$/i })).toBeInTheDocument()
    expect(within(dialogo).queryByText('Modo solo lectura')).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: /bloquear|activar|eliminar/i })).not.toBeInTheDocument()
  })

  it('userEditDialog_deUnCliente_soloDejaModificarLoQueSePuede', async () => {
    const { dialogo } = await abrirEdicion(CARLOS)

    for (const etiqueta of [/^nombre/i, /^apellidos/i, /^dni/i, /^teléfono/i, /^fecha de nacimiento/i]) {
      expect(esDeSoloLectura(dialogo, etiqueta)).toBe(false)
    }
    expect(esDeSoloLectura(dialogo, 'Email')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Estado')).toBe(true)
    expect(within(dialogo).queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('userEditDialog_deUnAdministrador_noDejaModificarEmailNiFechaDeIncorporacion', async () => {
    const { dialogo } = await abrirEdicion(DIEGO)

    expect(esDeSoloLectura(dialogo, 'Email')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Fecha de incorporación')).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Fecha de alta')).toBe(true)
    expect(esDeSoloLectura(dialogo, /^sede/i)).toBe(false)
    expect(within(dialogo).queryByLabelText(/^dni/i)).not.toBeInTheDocument()
  })

  it('userEditDialog_nuncaMuestraLaContrasena', async () => {
    const { dialogo } = await abrirEdicion(CARLOS)

    expect(within(dialogo).queryByLabelText(/contraseña|password/i)).not.toBeInTheDocument()
  })

  it('userEditDialog_alPulsarCerrar_avisaDelCierre', async () => {
    const { dialogo, user, onClose } = await abrirEdicion(CARLOS)

    await user.click(within(dialogo).getByRole('button', { name: 'Cerrar' }))

    expect(onClose).toHaveBeenCalled()
  })

  it('userEditDialog_conErrorAlCargar_muestraMensajeYPermiteReintentar', async () => {
    server.use(http.get('/api/admin/usuarios/:id', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<UserEditDialog userId={CARLOS} onClose={vi.fn()} onSaved={vi.fn()} />)
    const dialogo = await screen.findByRole('dialog', { name: /editar usuario/i })

    expect(await within(dialogo).findByText(/no se ha podido cargar la información del usuario/i)).toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: /reintentar/i }))

    expect(await within(dialogo).findByLabelText(/^nombre/i)).toHaveValue('Carlos')
  })
})

describe('UserEditDialog (HU22): guardar', () => {
  it('userEditDialog_alGuardarUnCliente_enviaSusDatosSinElEmailNiElEstado', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

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

  it('userEditDialog_tras_guardar_avisaUnaSolaVez', async () => {
    const { dialogo, user, onSaved } = await abrirEdicion(CARLOS)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Carla')
    await guardar(user, dialogo)

    await waitFor(() => expect(onSaved).toHaveBeenCalledTimes(1), ESPERA)
  })

  it('userEditDialog_alCambiarElTipoDeCliente_loEnvia', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

    await user.selectOptions(within(dialogo).getByLabelText(/^tipo de cliente/i), 'ESTANDAR')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.tipoCliente).toBe('ESTANDAR'), ESPERA)
  })

  it('userEditDialog_alCambiarLaFechaDeNacimientoAOtraDeUnMayor_laGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

    fireEvent.change(within(dialogo).getByLabelText(/^fecha de nacimiento/i), { target: { value: '1990-05-20' } })
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.fechaNacimiento).toBe('1990-05-20'), ESPERA)
  })

  it('userEditDialog_conUnaFechaDeNacimientoDeUnMenor_muestraElErrorYNoGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

    fireEvent.change(within(dialogo).getByLabelText(/^fecha de nacimiento/i), {
      target: { value: `${new Date().getFullYear() - 10}-01-01` },
    })
    await guardar(user, dialogo)

    expect(within(dialogo).getByText(/debe ser mayor de edad/i)).toBeInTheDocument()
    expect(envio.llamadas).toBe(0)
  })

  it('userEditDialog_conCamposObligatoriosVacios_losMuestraTodosALaVezYNoGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.clear(within(dialogo).getByLabelText(/^apellidos/i))
    await user.clear(within(dialogo).getByLabelText(/^dni/i))
    await guardar(user, dialogo)

    expect(within(dialogo).getAllByText(/este campo es obligatorio/i)).toHaveLength(3)
    expect(envio.llamadas).toBe(0)
  })

  it('userEditDialog_alPulsarCancelar_cierraSinGuardarNada', async () => {
    const envio = capturarGuardado()
    const { dialogo, user, onClose, onSaved } = await abrirEdicion(CARLOS)

    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'XYZ')
    await user.click(within(dialogo).getByRole('button', { name: /^cancelar$/i }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(onSaved).not.toHaveBeenCalled()
    expect(envio.llamadas).toBe(0)
  })

  it('userEditDialog_conErrorDelServidor_muestraElMensajeYSigueEnEdicion', async () => {
    server.use(http.put('/api/admin/usuarios/:id', () => HttpResponse.json({ message: 'Mongo' }, { status: 500 })))
    const { dialogo, user, onSaved } = await abrirEdicion(CARLOS)

    await guardar(user, dialogo)

    expect(await within(dialogo).findByText(/no se han podido guardar los cambios/i, undefined, ESPERA)).toBeInTheDocument()
    expect(within(dialogo).queryByText(/mongo/i)).not.toBeInTheDocument()
    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('userEditDialog_alEditarLaSedeDeUnAdministrador_laEnvia', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(DIEGO)

    await user.clear(within(dialogo).getByLabelText(/^sede/i))
    await user.type(within(dialogo).getByLabelText(/^sede/i), 'Albacete')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(Object.keys(envio.cuerpo)).toEqual(['nombre', 'apellidos', 'sede', 'avatar'])
    expect(envio.cuerpo.sede).toBe('Albacete')
  })

  it('userEditDialog_laSedeDeUnAdministradorPuedeQuedarVacia', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(DIEGO)

    await user.clear(within(dialogo).getByLabelText(/^sede/i))
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.sede).toBe(''), ESPERA)
  })

  it('userEditDialog_unAdministradorPuedeEditarSusPropiosDatos', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(ADA)

    await user.clear(within(dialogo).getByLabelText(/^nombre/i))
    await user.type(within(dialogo).getByLabelText(/^nombre/i), 'Adela')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.cuerpo?.nombre).toBe('Adela'), ESPERA)
    expect(envio.id).toBe(ADA)
  })
})

describe('UserEditDialog (HU22): el rol no se puede cambiar', () => {
  it.each([
    ['un cliente', CARLOS, 'Cliente'],
    ['otro administrador', DIEGO, 'Administrador'],
    ['el propio administrador', ADA, 'Administrador'],
  ])('userEditDialog_elRolEsDeSoloLectura_enLaEdicionDe %s', async (_caso, userId, texto) => {
    const { dialogo } = await abrirEdicion(userId)

    expect(valorDe(dialogo, 'Rol')).toHaveValue(texto)
    expect(valorDe(dialogo, 'Rol')).toHaveAttribute('readonly')
  })

  it('userEditDialog_alGuardar_noEnviaElRolNiElEmail', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await abrirEdicion(CARLOS)

    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(envio.cuerpo).not.toHaveProperty('rol')
    expect(envio.cuerpo).not.toHaveProperty('email')
  })
})
