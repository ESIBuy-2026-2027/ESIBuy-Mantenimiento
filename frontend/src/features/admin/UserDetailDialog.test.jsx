import { describe, it, expect, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
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

async function abrirFicha(userId) {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onClose: vi.fn() }
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
    renderWithProviders(<UserDetailDialog userId={LUCIA} onClose={vi.fn()} />)
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

describe('UserDetailDialog (HU22): solo lectura', () => {
  it('userDetailDialog_noOfreceEditar_laEdicionSeHaceDesdeLaTabla', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(within(dialogo).queryByRole('button', { name: /editar/i })).not.toBeInTheDocument()
    expect(within(dialogo).getByText(/usa el botón editar de la tabla/i)).toBeInTheDocument()
  })

  it('userDetailDialog_soloOfreceCerrar', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(within(dialogo).queryByRole('button', { name: /guardar|cancelar|eliminar/i })).not.toBeInTheDocument()
    expect(within(dialogo).getByRole('button', { name: 'Cerrar' })).toBeInTheDocument()
  })

  it('userDetailDialog_muestraElRolDeLaCuenta', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(valorDe(dialogo, 'Rol')).toHaveValue('Cliente')
  })

  it('userDetailDialog_deUnAdministrador_muestraSuSedeYSuRol', async () => {
    const { dialogo } = await abrirFicha(DIEGO)

    expect(valorDe(dialogo, 'Rol')).toHaveValue('Administrador')
    expect(valorDe(dialogo, 'Sede')).toHaveAttribute('readonly')
  })
})
