import { describe, it, expect, vi } from 'vitest'
import { fireEvent, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ApiError } from '../../api/client'
import { ROLES } from '../../auth/roles'
import { renderWithProviders } from '../../test/utils'
import ProfileEditor from './ProfileEditor'

const CLIENTE = {
  id: 'usr-3',
  nombre: 'Carlos',
  apellidos: 'Moreno Gil',
  email: 'carlos.moreno@esibuy.test',
  estado: 'ACTIVO',
  fechaAlta: '2026-02-03T11:30:00',
  dni: '10004119A',
  telefono: '620023757',
  fechaNacimiento: '1978-04-13',
  tipoCliente: 'PREMIUM',
  avatar: 'avatar1',
}

const ADMINISTRADOR = {
  id: 'usr-11',
  nombre: 'Diego',
  apellidos: 'Delgado Iglesias',
  email: 'diego.delgado@esibuy.test',
  estado: 'ACTIVO',
  fechaAlta: '2026-01-20T13:25:00',
  sede: 'Cuenca',
  fechaIncorporacion: '2026-01-20',
  avatar: 'avatar3',
}

const VENDEDOR = {
  id: 'u-2',
  nombre: 'Vera',
  apellidos: 'Vendedora',
  email: 'vendedor@esibuy.test',
  nombreComercial: 'TecnoVera',
  dni: '12345601V',
  categoriaPrincipal: 'Tecnología',
  categoriaPrincipalId: 'cat-1',
  estado: 'ACTIVO',
  fechaAlta: '2026-09-14',
}

const CATEGORIAS = [
  { id: 'cat-1', nombre: 'Tecnología' },
  { id: 'cat-2', nombre: 'Hogar' },
]

function renderizar({ rol = ROLES.CLIENTE, usuario = CLIENTE, save = vi.fn().mockResolvedValue({}), ...resto } = {}) {
  const props = { save, onSaved: vi.fn(), onCancel: vi.fn(), categories: CATEGORIAS, ...resto }
  renderWithProviders(<ProfileEditor rol={rol} usuario={usuario} {...props} />)
  return props
}

const guardar = (user) => user.click(screen.getByRole('button', { name: /^guardar$/i }))
const esDeSoloLectura = (etiqueta) => screen.getByLabelText(etiqueta).hasAttribute('readonly')

describe('ProfileEditor (HU22): campos', () => {
  it('profileEditor_deUnCliente_dejaModificablesTodosLosCamposMenosElEmail', () => {
    renderizar()

    for (const etiqueta of ['Nombre', 'Apellidos', 'DNI', 'Teléfono', 'Fecha de nacimiento']) {
      expect(esDeSoloLectura(new RegExp(`^${etiqueta}`))).toBe(false)
    }
    expect(screen.getByLabelText(/^tipo de cliente/i)).not.toBeDisabled()
    expect(esDeSoloLectura('Email')).toBe(true)
    expect(esDeSoloLectura('Estado')).toBe(true)
  })

  it('profileEditor_deUnClienteOUnVendedor_noMuestraLaFechaDeAlta', () => {
    renderizar()
    expect(screen.queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('profileEditor_deUnCliente_muestraSusDatosActualesEnLosCampos', () => {
    renderizar()

    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Carlos')
    expect(screen.getByLabelText(/^dni/i)).toHaveValue('10004119A')
    expect(screen.getByLabelText(/^teléfono/i)).toHaveValue('620023757')
    expect(screen.getByLabelText(/^fecha de nacimiento/i)).toHaveValue('1978-04-13')
    expect(screen.getByLabelText(/^tipo de cliente/i)).toHaveValue('PREMIUM')
    expect(screen.getByRole('radio', { name: 'Avatar 1' })).toBeChecked()
  })

  it('profileEditor_deUnAdministrador_noPermiteModificarEmailNiFechaDeIncorporacion', () => {
    renderizar({ rol: ROLES.ADMIN, usuario: ADMINISTRADOR })

    expect(esDeSoloLectura('Email')).toBe(true)
    expect(esDeSoloLectura('Fecha de incorporación')).toBe(true)
    expect(esDeSoloLectura('Fecha de alta')).toBe(true)
    expect(esDeSoloLectura(/^sede/i)).toBe(false)
    expect(esDeSoloLectura(/^nombre/i)).toBe(false)
    expect(screen.queryByLabelText(/^dni/i)).not.toBeInTheDocument()
  })

  it('profileEditor_deUnVendedor_dejaModificableTodoMenosElEmailYMuestraLasCategorias', () => {
    renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR })

    for (const etiqueta of [/^nombre(?! comercial)/i, /^apellidos/i, /^nombre comercial/i, /^dni/i]) {
      expect(esDeSoloLectura(etiqueta)).toBe(false)
    }
    expect(esDeSoloLectura(/^correo electrónico/i)).toBe(true)
    expect(esDeSoloLectura('Estado')).toBe(true)
    expect(screen.queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: /^categoría principal/i })).toHaveValue('Tecnología')
  })

  it('profileEditor_deUnVendedor_laCategoriaPrincipalSeEligeEnUnDesplegableConLasCategorias', async () => {
    const user = userEvent.setup()
    renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR })

    await user.click(screen.getByRole('combobox', { name: /^categoría principal/i }))
    expect(screen.getAllByRole('option').map((opcion) => opcion.textContent)).toEqual(['Tecnología', 'Hogar'])
    await user.click(screen.getByRole('option', { name: 'Hogar' }))

    expect(screen.getByRole('combobox', { name: /^categoría principal/i })).toHaveValue('Hogar')
  })

  it('profileEditor_ningunTipoDeUsuario_muestraCamposDeContrasena', () => {
    for (const [rol, usuario] of [
      [ROLES.CLIENTE, CLIENTE],
      [ROLES.ADMIN, ADMINISTRADOR],
      [ROLES.VENDEDOR, VENDEDOR],
    ]) {
      const { unmount } = renderWithProviders(
        <ProfileEditor rol={rol} usuario={usuario} save={vi.fn()} onSaved={vi.fn()} onCancel={vi.fn()} categories={CATEGORIAS} />,
      )

      expect(screen.queryByLabelText(/contraseña|password/i)).not.toBeInTheDocument()
      unmount()
    }
  })

  it('profileEditor_sinCategorias_noFallaYMuestraElSelectorVacio', async () => {
    const user = userEvent.setup()
    renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR, categories: undefined })
    const categoria = screen.getByRole('combobox', { name: /^categoría principal/i })

    await user.click(categoria)

    expect(categoria).toHaveValue('')
    expect(screen.queryAllByRole('option')).toHaveLength(0)
  })
})

describe('ProfileEditor (HU22): validación', () => {
  it('profileEditor_antesDelPrimerIntento_noMuestraErrores', async () => {
    const user = userEvent.setup()
    renderizar()

    await user.clear(screen.getByLabelText(/^nombre/i))

    expect(screen.queryByText(/este campo es obligatorio/i)).not.toBeInTheDocument()
  })

  it('profileEditor_alGuardarConObligatoriosVacios_muestraTodosLosErroresALaVezYNoGuarda', async () => {
    const user = userEvent.setup()
    const { save } = renderizar()

    await user.clear(screen.getByLabelText(/^nombre/i))
    await user.clear(screen.getByLabelText(/^dni/i))
    await guardar(user)

    expect(screen.getAllByText(/este campo es obligatorio/i)).toHaveLength(2)
    expect(save).not.toHaveBeenCalled()
  })

  it('profileEditor_conUnMenorDeEdad_noPermiteGuardar', async () => {
    const user = userEvent.setup()
    const { save } = renderizar()
    const hoy = new Date()

    fireEvent.change(screen.getByLabelText(/^fecha de nacimiento/i), {
      target: { value: `${hoy.getFullYear() - 10}-01-01` },
    })
    await guardar(user)

    expect(screen.getByText(/debe ser mayor de edad/i)).toBeInTheDocument()
    expect(save).not.toHaveBeenCalled()
  })

  it('profileEditor_conUnDniIncorrecto_muestraElErrorDelCampo', async () => {
    const user = userEvent.setup()
    renderizar()

    await user.clear(screen.getByLabelText(/^dni/i))
    await user.type(screen.getByLabelText(/^dni/i), '123')
    await guardar(user)

    expect(screen.getByText(/introduce un dni válido/i)).toBeInTheDocument()
  })

  it('profileEditor_alCorregirUnCampoTrasUnIntentoFallido_loValidaEnVivo', async () => {
    const user = userEvent.setup()
    renderizar()
    await user.clear(screen.getByLabelText(/^nombre/i))
    await guardar(user)
    expect(screen.getByText(/este campo es obligatorio/i)).toBeInTheDocument()

    await user.type(screen.getByLabelText(/^nombre/i), 'Carla')

    expect(screen.queryByText(/este campo es obligatorio/i)).not.toBeInTheDocument()
  })
})

describe('ProfileEditor (HU22): guardado', () => {
  it('profileEditor_alGuardar_envia_soloLosCamposModificablesYAvisa', async () => {
    const user = userEvent.setup()
    const { save, onSaved } = renderizar()

    await user.clear(screen.getByLabelText(/^nombre/i))
    await user.type(screen.getByLabelText(/^nombre/i), 'Carla')
    await guardar(user)

    await waitFor(() => expect(onSaved).toHaveBeenCalled())
    expect(save).toHaveBeenCalledWith({
      nombre: 'Carla',
      apellidos: 'Moreno Gil',
      dni: '10004119A',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      tipoCliente: 'PREMIUM',
      avatar: 'avatar1',
    })
  })

  it('profileEditor_alElegirOtroAvatar_loEnvia', async () => {
    const user = userEvent.setup()
    const { save } = renderizar()

    await user.click(screen.getByRole('radio', { name: 'Avatar 3' }))
    await guardar(user)

    await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining({ avatar: 'avatar3' })))
  })

  it('profileEditor_conUnTelefonoVacio_loAcepta', async () => {
    const user = userEvent.setup()
    const { save } = renderizar()

    await user.clear(screen.getByLabelText(/^teléfono/i))
    await guardar(user)

    await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining({ telefono: '' })))
  })

  it('profileEditor_conNombreComercialRepetido_loMuestraEnElCampoYNoSale', async () => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(409, 'conflicto', 'COMMERCIAL_NAME_ALREADY_REGISTERED'))
    const { onSaved } = renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR, save })

    await guardar(user)

    expect(await screen.findByText('Este nombre comercial ya está registrado')).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('profileEditor_alEditarElCampoConError409_quitaElErrorDelServidor', async () => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(409, 'conflicto', 'COMMERCIAL_NAME_ALREADY_REGISTERED'))
    renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR, save })
    await guardar(user)
    await screen.findByText('Este nombre comercial ya está registrado')

    await user.type(screen.getByLabelText(/^nombre comercial/i), '2')

    expect(screen.queryByText('Este nombre comercial ya está registrado')).not.toBeInTheDocument()
  })

  it('profileEditor_conErrorDelServidor_muestraMensajeGenericoYConservaLoEscrito', async () => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(500, 'Error 500'))
    const { onSaved } = renderizar({ save })

    await user.clear(screen.getByLabelText(/^nombre/i))
    await user.type(screen.getByLabelText(/^nombre/i), 'Carla')
    await guardar(user)

    expect(await screen.findByText(/no se han podido guardar los cambios/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Carla')
    expect(onSaved).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: /^guardar$/i })).toBeEnabled()
  })

  it('profileEditor_mientrasGuarda_deshabilitaLosBotonesYNoEnviaDosVeces', async () => {
    const user = userEvent.setup()
    let terminar
    const save = vi.fn(() => new Promise((resolver) => (terminar = resolver)))
    renderizar({ save })

    await guardar(user)
    await user.dblClick(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByRole('button', { name: /^guardar$/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /^cancelar$/i })).toBeDisabled()
    expect(save).toHaveBeenCalledTimes(1)
    terminar({})
  })

  it('profileEditor_alPulsarCancelar_avisaSinGuardar', async () => {
    const user = userEvent.setup()
    const { save, onCancel } = renderizar()

    await user.type(screen.getByLabelText(/^nombre/i), 'X')
    await user.click(screen.getByRole('button', { name: /^cancelar$/i }))

    expect(onCancel).toHaveBeenCalled()
    expect(save).not.toHaveBeenCalled()
  })
})

// El rol de una cuenta no se puede cambiar ("el que nace cliente muere cliente", decisión del equipo).
describe('ProfileEditor (HU22): el rol no se puede cambiar', () => {
  it.each([
    [ROLES.CLIENTE, CLIENTE, 'Cliente'],
    [ROLES.ADMIN, ADMINISTRADOR, 'Administrador'],
    [ROLES.VENDEDOR, VENDEDOR, 'Vendedor'],
  ])('profileEditor_de_%s_muestraElRolEnSoloLectura', (rol, usuario, texto) => {
    renderizar({ rol, usuario })

    expect(screen.getByLabelText('Rol')).toHaveValue(texto)
    expect(esDeSoloLectura('Rol')).toBe(true)
  })

  it('profileEditor_alGuardar_noEnviaElRol', async () => {
    const user = userEvent.setup()
    const { save } = renderizar({ rol: ROLES.ADMIN, usuario: ADMINISTRADOR })

    await guardar(user)

    await waitFor(() => expect(save).toHaveBeenCalled())
    expect(save.mock.calls[0][0]).not.toHaveProperty('rol')
  })

  // Fallos de la demo del Sprint 1: un DNI repetido se marca en el DNI, no como error genérico (PR #104)
  it('HU22-CP16: un DNI ya registrado se marca en el DNI', async () => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(409, 'conflicto', 'DNI_ALREADY_REGISTERED'))
    const { onSaved } = renderizar({ save })

    await guardar(user)

    expect(await screen.findByText('Este DNI ya está registrado')).toBeInTheDocument()
    expect(screen.queryByText(/no se han podido guardar los cambios/i)).not.toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })
})

describe('ProfileEditor (HU45): perfil propio', () => {
  const guardarCambios = (user) => user.click(screen.getByRole('button', { name: /^guardar cambios$/i }))
  const renderizarPropio = (opciones = {}) => renderizar({ own: true, ...opciones })
  const escribir = async (user, etiqueta, texto) => {
    const campo = screen.getByLabelText(etiqueta)
    await user.clear(campo)
    if (texto) await user.type(campo, texto)
  }

  it('HU45-CP11: el cliente modifica sus datos personales y ve en solo lectura el email, el DNI, el tipo y el rol', () => {
    renderizarPropio()

    for (const etiqueta of [/^nombre/i, /^apellidos/i, /^teléfono/i, /^fecha de nacimiento/i]) {
      expect(esDeSoloLectura(etiqueta)).toBe(false)
    }
    for (const etiqueta of ['Email', 'Rol', 'DNI', 'Tipo de cliente']) {
      expect(esDeSoloLectura(etiqueta)).toBe(true)
    }
    expect(screen.getByLabelText('DNI')).toHaveValue('10004119A')
    expect(screen.getByLabelText('Tipo de cliente')).toHaveValue('Premium')
    expect(screen.queryByLabelText('Estado')).not.toBeInTheDocument()
  })

  it('HU45-CP11: el vendedor modifica sus datos y su negocio y ve en solo lectura el email, el DNI y el rol', () => {
    renderizarPropio({ rol: ROLES.VENDEDOR, usuario: VENDEDOR })

    for (const etiqueta of [/^nombre(?! comercial)/i, /^apellidos/i, /^nombre comercial/i]) {
      expect(esDeSoloLectura(etiqueta)).toBe(false)
    }
    for (const etiqueta of ['Correo electrónico', 'Rol', 'DNI']) {
      expect(esDeSoloLectura(etiqueta)).toBe(true)
    }
    expect(screen.getByRole('combobox', { name: /^categoría principal/i })).toHaveValue('Tecnología')
    expect(screen.queryByLabelText('Estado')).not.toBeInTheDocument()
  })

  it('HU45-CP12: al guardar envía todos los datos modificables del cliente y avisa con la respuesta del servidor', async () => {
    const user = userEvent.setup()
    const respuesta = { id: 'usr-3', nombre: 'Carla' }
    const { save, onSaved } = renderizarPropio({ save: vi.fn().mockResolvedValue(respuesta) })

    await escribir(user, /^nombre/i, 'Carla')
    await guardarCambios(user)

    const enviado = {
      nombre: 'Carla',
      apellidos: 'Moreno Gil',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      avatar: 'avatar1',
    }
    await waitFor(() => expect(save).toHaveBeenCalledWith(enviado))
    expect(onSaved).toHaveBeenCalledWith(enviado, respuesta)
  })

  it('HU45-CP12: mientras se guarda, el botón queda deshabilitado y no se envía dos veces', async () => {
    const user = userEvent.setup()
    let terminar
    const save = vi.fn(() => new Promise((resolve) => (terminar = resolve)))
    renderizarPropio({ save })

    await guardarCambios(user)
    await waitFor(() => expect(screen.getByRole('button', { name: /^guardar cambios$/i })).toBeDisabled())
    await guardarCambios(user)

    expect(save).toHaveBeenCalledTimes(1)
    terminar({})
  })

  it('HU45-CP12: sin pulsar guardar no se envía nada aunque se modifiquen los datos', async () => {
    const user = userEvent.setup()
    const { save, onSaved } = renderizarPropio()

    await escribir(user, /^nombre/i, 'Carla')
    await escribir(user, /^teléfono/i, '699888777')

    expect(save).not.toHaveBeenCalled()
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('HU45-CP13: con datos incorrectos no se envía, cada error sale en su campo y el foco va al primero', async () => {
    const user = userEvent.setup()
    const { save } = renderizarPropio()

    await escribir(user, /^nombre/i, '')
    await escribir(user, /^teléfono/i, '123')
    await guardarCambios(user)

    expect(save).not.toHaveBeenCalled()
    expect(await screen.findByText('Este campo es obligatorio')).toBeInTheDocument()
    expect(screen.getByText('Introduce un teléfono válido (9 números)')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveFocus())
  })

  it.each([
    ['INVALID_PHONE', 400, ROLES.CLIENTE, CLIENTE, 'El teléfono debe tener 9 números'],
    ['UNDERAGE', 400, ROLES.CLIENTE, CLIENTE, 'Debes ser mayor de edad'],
    ['COMMERCIAL_NAME_ALREADY_REGISTERED', 409, ROLES.VENDEDOR, VENDEDOR, 'Este nombre comercial ya está registrado'],
  ])('HU45-CP14: el code %s del backend sale en su campo con su propio mensaje', async (code, estado, rol, usuario, mensaje) => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(estado, 'Texto del backend que no se muestra', code))
    const { onSaved } = renderizarPropio({ rol, usuario, save })

    await guardarCambios(user)

    expect(await screen.findByText(mensaje)).toBeInTheDocument()
    expect(screen.queryByText('Texto del backend que no se muestra')).not.toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: /^guardar cambios$/i })).toBeEnabled()
  })

  it('HU45-CP14: un VALIDATION_ERROR o un fallo del servidor dan un aviso general y conservan lo escrito', async () => {
    const user = userEvent.setup()
    const save = vi.fn().mockRejectedValue(new ApiError(400, 'inválido', 'VALIDATION_ERROR'))
    renderizarPropio({ save })

    await escribir(user, /^nombre/i, 'Carla')
    await guardarCambios(user)

    expect(await screen.findByText('No se han podido guardar los cambios. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Carla')
  })

  it('HU45-CP15: avisa de si hay cambios sin guardar cada vez que cambia', async () => {
    const user = userEvent.setup()
    const onDirtyChange = vi.fn()
    renderizarPropio({ onDirtyChange })

    expect(onDirtyChange).toHaveBeenLastCalledWith(false)
    await escribir(user, /^nombre/i, 'Carla')
    expect(onDirtyChange).toHaveBeenLastCalledWith(true)
    await escribir(user, /^nombre/i, 'Carlos')
    expect(onDirtyChange).toHaveBeenLastCalledWith(false)
  })

  it('HU45-CP15: el botón Cancelar avisa al pulsarlo', async () => {
    const user = userEvent.setup()
    const { onCancel } = renderizarPropio()

    await user.click(screen.getByRole('button', { name: /^cancelar$/i }))

    expect(onCancel).toHaveBeenCalledTimes(1)
  })
})
