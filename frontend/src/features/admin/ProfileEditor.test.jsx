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
    const categoria = screen.getByLabelText(/^categoría principal/i)
    expect(categoria).toHaveValue('cat-1')
    expect(Array.from(categoria.options).map((opcion) => opcion.textContent)).toEqual(['Tecnología', 'Hogar'])
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

  it('profileEditor_sinCategorias_noFallaYMuestraElSelectorVacio', () => {
    renderizar({ rol: ROLES.VENDEDOR, usuario: VENDEDOR, categories: undefined })

    expect(screen.getByLabelText(/^categoría principal/i).options).toHaveLength(0)
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
