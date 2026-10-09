import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import UserDetailDialog from './UserDetailDialog'

// Del mock de usuarios: Lucía Torres Vega (usr-4) es clienta desactivada, Carlos Moreno Gil (usr-3) cliente
// activo y Diego Delgado Iglesias (usr-11) administrador desactivado.
const LUCIA = 'usr-4'
const CARLOS = 'usr-3'
const DIEGO = 'usr-11'
const ESPERA = { timeout: 3000 }

async function abrirFicha(userId, currentUserId = 'u-1') {
  await abrirSesionPorApi('admin@esibuy.test')
  const handlers = { onClose: vi.fn(), onStatusChange: vi.fn() }
  renderWithProviders(<UserDetailDialog userId={userId} currentUserId={currentUserId} {...handlers} />)
  const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
  await within(dialogo).findByLabelText('Nombre')
  return { dialogo, ...handlers }
}

const valorDe = (dialogo, etiqueta) => within(dialogo).getByLabelText(etiqueta)
const botonDeEstado = (dialogo, texto) => within(dialogo).queryByText(texto, { selector: 'button' })

// La confirmación se busca por texto: mientras se abre o se cierra, Fluent marca partes de la página
// con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function confirmar(user, dialogo, accion, pregunta) {
  await user.click(botonDeEstado(dialogo, accion))
  const confirmacion = (await screen.findByText(pregunta)).closest('[role="alertdialog"]')
  await user.click(within(confirmacion).getByText(accion, { selector: 'button' }))
}

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
    expect(valorDe(dialogo, 'Estado')).toHaveValue('Desactivado')
    expect(valorDe(dialogo, 'Fecha de alta')).toHaveValue('28/09/2026')
  })

  it('userDetailDialog_deUnAdministrador_muestraSedeYFechaDeIncorporacion', async () => {
    const { dialogo } = await abrirFicha(DIEGO)

    expect(valorDe(dialogo, 'Nombre')).toHaveValue('Diego')
    expect(valorDe(dialogo, 'Sede')).toHaveValue('Cuenca')
    expect(valorDe(dialogo, 'Fecha de incorporación')).toHaveValue('20/01/2026')
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

describe('UserDetailDialog (HU20): activar y desactivar desde la ficha', () => {
  it('userDetailDialog_deUnaCuentaDesactivada_ofreceActivar', async () => {
    const { dialogo } = await abrirFicha(LUCIA)

    expect(botonDeEstado(dialogo, 'Activar')).toBeInTheDocument()
    expect(botonDeEstado(dialogo, 'Desactivar')).not.toBeInTheDocument()
  })

  it('userDetailDialog_deUnaCuentaActiva_ofreceDesactivar', async () => {
    const { dialogo } = await abrirFicha(CARLOS)

    expect(botonDeEstado(dialogo, 'Desactivar')).toBeInTheDocument()
    expect(botonDeEstado(dialogo, 'Activar')).not.toBeInTheDocument()
  })

  it('userDetailDialog_deLaPropiaCuenta_noOfreceDesactivar', async () => {
    const { dialogo } = await abrirFicha(CARLOS, CARLOS)

    expect(botonDeEstado(dialogo, 'Desactivar')).not.toBeInTheDocument()
  })

  it('userDetailDialog_alActivar_muestraActivoAvisaYAvisaAlListado', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirFicha(LUCIA)

    await confirmar(user, dialogo, 'Activar', /seguro que quieres activar a lucía torres vega/i)

    expect(await within(dialogo).findByText(/lucía torres vega ya está activo/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(valorDe(dialogo, 'Estado')).toHaveValue('Activo'), ESPERA)
    expect(botonDeEstado(dialogo, 'Desactivar')).toBeInTheDocument()
    expect(onStatusChange).toHaveBeenCalledTimes(1)
  })

  it('userDetailDialog_alDesactivar_muestraDesactivadoYAvisa', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirFicha(CARLOS)

    await confirmar(user, dialogo, 'Desactivar', /seguro que quieres desactivar a carlos moreno gil/i)

    expect(await within(dialogo).findByText(/carlos moreno gil está desactivado/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(valorDe(dialogo, 'Estado')).toHaveValue('Desactivado'), ESPERA)
    expect(onStatusChange).toHaveBeenCalledTimes(1)
  })

  it('userDetailDialog_siEsElUltimoAdministradorActivo_muestraElMotivo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'LAST_ACTIVE_ADMIN' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirFicha(CARLOS)

    await confirmar(user, dialogo, 'Desactivar', /seguro que quieres desactivar/i)

    expect(
      await within(dialogo).findByText('No se puede desactivar al último administrador activo.', {}, ESPERA),
    ).toBeInTheDocument()
    expect(onStatusChange).not.toHaveBeenCalled()
  })

  it('userDetailDialog_siFallaElCambio_muestraUnErrorGenericoYNoCambiaElEstado', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    const { dialogo } = await abrirFicha(LUCIA)

    await confirmar(user, dialogo, 'Activar', /seguro que quieres activar/i)

    expect(await within(dialogo).findByText(/no se ha podido activar a lucía torres vega/i, {}, ESPERA)).toBeInTheDocument()
    expect(valorDe(dialogo, 'Estado')).toHaveValue('Desactivado')
  })
})
