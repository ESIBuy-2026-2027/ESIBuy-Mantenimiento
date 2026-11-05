import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import RoleUsersList from './RoleUsersList'

// El mock tiene 17 clientes (dos páginas): 2 pendientes, 8 activos y 7 bloqueados. Por defecto se
// ordenan por nombre ascendente: la primera página va de Álvaro Campos Serrano a Marcos Aguilar Bravo.
const ESPERA = { timeout: 3000 }

async function renderClientes(currentUserId) {
  await abrirSesionPorApi('admin@esibuy.test')
  renderWithProviders(
    <RoleUsersList
      role="CLIENTE"
      itemLabel="clientes"
      errorMessage="No se ha podido cargar el listado de clientes."
      loadingLabel="Cargando clientes…"
      currentUserId={currentUserId}
    />,
  )
  await screen.findByText('carlos.moreno@esibuy.test')
}

// Las filas se buscan por texto: no dependen de que Fluent marque la página con aria-hidden
// mientras se abre o se cierra la confirmación.
const filaDe = (email) => screen.getByText(email).closest('tr')
const filasDeDatos = () => within(screen.getByRole('table')).getAllByRole('row').slice(1)
const primerNombre = () => within(filasDeDatos()[0]).getAllByRole('cell')[0].textContent

// La confirmación se busca por texto: mientras se abre o se cierra, Fluent marca partes de la página
// con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function cambiarEstadoDesdeLaFila(user, accion, nombre) {
  await user.click(screen.getByRole('button', { name: `${accion} a ${nombre}` }))
  const pregunta = await screen.findByText(new RegExp(`seguro que quieres ${accion.toLowerCase()} a ${nombre}`, 'i'))
  await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText(accion, { selector: 'button' }))
}

describe('RoleUsersList (HU24)', () => {
  it('roleUsersList_muestraSoloLosUsuariosDelRolPedido', async () => {
    await renderClientes()

    expect(filasDeDatos()).toHaveLength(10)
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Cliente')).toBeInTheDocument())
    expect(screen.getByText(/mostrando 1–10 de 17 clientes/i)).toBeInTheDocument()
  })

  it('roleUsersList_elFiltroDeEstadoOfrecePendienteYNoHayFiltroDeRol', async () => {
    await renderClientes()

    const filtro = screen.getByRole('combobox', { name: 'Estado' })
    const opciones = within(filtro).getAllByRole('option').map((opcion) => opcion.textContent)

    expect(opciones).toEqual(['Todos los estados', 'Pendiente', 'Activo', 'Bloqueado'])
    expect(screen.queryByRole('combobox', { name: 'Rol' })).not.toBeInTheDocument()
  })

  it('roleUsersList_alFiltrarPorPendiente_muestraSoloEsasCuentas', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(2))
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Pendiente')).toBeInTheDocument())
  })

  it('roleUsersList_alFiltrarPorBloqueado_muestraSoloEsasCuentas', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Bloqueado')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(7))
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Bloqueado')).toBeInTheDocument())
  })

  it('roleUsersList_porDefecto_ordenaPorNombreAscendente', async () => {
    await renderClientes()

    expect(screen.getByRole('combobox', { name: 'Ordenar por' })).toHaveValue('name')
    expect(primerNombre()).toBe('Álvaro Campos Serrano')
  })

  it('roleUsersList_alCambiarElSentido_ordenaPorNombreDescendente', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.click(screen.getByRole('button', { name: 'Orden ascendente' }))

    await waitFor(() => expect(primerNombre()).toBe('Sofía Cabrera Soler'))
  })

  it('roleUsersList_alOrdenarPorEstado_muestraPrimeroLosPendientesYLuegoLosActivos', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Ordenar por' }), 'Estado')

    await waitFor(() => expect(primerNombre()).toBe('Gonzalo Ibáñez Roca'))
    const filas = filasDeDatos()
    filas.slice(0, 2).forEach((fila) => expect(within(fila).getByText('Pendiente')).toBeInTheDocument())
    filas.slice(2, 10).forEach((fila) => expect(within(fila).getByText('Activo')).toBeInTheDocument())
  })

  it('roleUsersList_buscaSoloPorNombreYNoPorEmail', async () => {
    const user = userEvent.setup()
    await renderClientes()
    const buscador = screen.getByRole('searchbox', { name: 'Buscar por nombre' })

    await user.type(buscador, 'torres')
    await waitFor(() => expect(filasDeDatos()).toHaveLength(1))
    expect(screen.getByText('lucia.torres@esibuy.test')).toBeInTheDocument()

    await user.clear(buscador)
    await user.type(buscador, 'esibuy')
    expect(await screen.findByText(/no se han encontrado clientes/i)).toBeInTheDocument()
  })

  it('roleUsersList_conBusquedaSinCoincidencias_muestraEstadoVacioYPermiteLimpiar', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre' }), 'zzz')

    expect(await screen.findByText(/no se han encontrado clientes/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))
    expect(await screen.findByText('carlos.moreno@esibuy.test')).toBeInTheDocument()
  })

  it('roleUsersList_conErrorDeServidor_muestraMensajeYPermiteReintentar', async () => {
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(
      <RoleUsersList
        role="CLIENTE"
        itemLabel="clientes"
        errorMessage="No se ha podido cargar el listado de clientes."
        loadingLabel="Cargando clientes…"
      />,
    )

    expect(await screen.findByText('No se ha podido cargar el listado de clientes.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByText('carlos.moreno@esibuy.test')).toBeInTheDocument()
  })


  it('roleUsersList_cadaFilaTieneLupaYBotonDeBloqueo', async () => {
    await renderClientes()

    expect(within(filaDe('carlos.moreno@esibuy.test')).getByRole('button', { name: 'Ver información de Carlos Moreno Gil' })).toBeInTheDocument()
    expect(within(filaDe('carlos.moreno@esibuy.test')).getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })).toBeInTheDocument()
    expect(within(filaDe('lucia.torres@esibuy.test')).getByRole('button', { name: 'Desbloquear a Lucía Torres Vega' })).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /^ver información de/i })).toHaveLength(10)
    expect(screen.getAllByRole('button', { name: /^(bloquear|desbloquear) a /i })).toHaveLength(10)
    expect(screen.queryByRole('button', { name: /desactivar|^activar/i })).not.toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /^editar a /i })).toHaveLength(10)
    // Solo se eliminan administradores (HU37): los clientes no tienen papelera.
    expect(screen.queryByRole('button', { name: /^eliminar a /i })).not.toBeInTheDocument()
  })

  it('roleUsersList_alPulsarLaLupa_abreLaFichaDeEseUsuario', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.click(screen.getByRole('button', { name: 'Ver información de Lucía Torres Vega' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    expect(await within(dialogo).findByLabelText('Email')).toHaveValue('lucia.torres@esibuy.test')
  })

  it('roleUsersList_conElUsuarioEnSesion_suFilaNoPermiteBloquearse', async () => {
    await renderClientes('usr-3')

    expect(screen.getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Bloquear a Claudia Peña Calvo' })).toBeEnabled()
  })

  it('roleUsersList_alDesbloquearDesdeLaFila_actualizaElListadoYAvisa', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await cambiarEstadoDesdeLaFila(user, 'Desbloquear', 'Lucía Torres Vega')

    await waitFor(() => expect(within(filaDe('lucia.torres@esibuy.test')).getByText('Activo')).toBeInTheDocument(), ESPERA)
    expect(await screen.findByText('Lucía Torres Vega ya está activo y puede usar la plataforma.')).toBeInTheDocument()
  })

  it('roleUsersList_alBloquearDesdeLaFila_actualizaElListadoYAvisa', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await cambiarEstadoDesdeLaFila(user, 'Bloquear', 'Carlos Moreno Gil')

    await waitFor(() => expect(within(filaDe('carlos.moreno@esibuy.test')).getByText('Bloqueado')).toBeInTheDocument(), ESPERA)
    expect(await screen.findByText('Carlos Moreno Gil está bloqueado y ya no puede iniciar sesión.')).toBeInTheDocument()
  })

  it('roleUsersList_deClientes_noPideCuantosAdministradoresActivosQuedan', async () => {
    const roles = []
    server.use(
      http.get('/api/admin/usuarios', ({ request }) => {
        roles.push(new URL(request.url).searchParams.get('role'))
      }),
    )

    await renderClientes()

    expect(roles.length).toBeGreaterThan(0)
    expect(roles.every((rol) => rol === 'CLIENTE')).toBe(true)
  })

  it('roleUsersList_siFallaElBloqueo_muestraElErrorYNoCambiaLaFila', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    await renderClientes()

    await cambiarEstadoDesdeLaFila(user, 'Bloquear', 'Carlos Moreno Gil')

    expect(await screen.findByText('No se ha podido bloquear a Carlos Moreno Gil. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(within(filaDe('carlos.moreno@esibuy.test')).getByText('Activo')).toBeInTheDocument()
  })
})

describe('RoleUsersList (HU22): los administradores no están pendientes', () => {
  it('roleUsersList_deAdministradores_elFiltroDeEstadoNoOfrecePendiente', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(
      <RoleUsersList
        role="ADMINISTRADOR"
        itemLabel="administradores"
        errorMessage="No se ha podido cargar el listado de administradores."
        loadingLabel="Cargando administradores…"
      />,
    )
    await screen.findByText('ana.lopez@esibuy.test')

    const opciones = within(screen.getByRole('combobox', { name: 'Estado' }))
      .getAllByRole('option')
      .map((opcion) => opcion.textContent)

    expect(opciones).toEqual(['Todos los estados', 'Activo', 'Bloqueado'])
  })

  it('roleUsersList_deAdministradores_ningunoApareceComoPendiente', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(
      <RoleUsersList
        role="ADMINISTRADOR"
        itemLabel="administradores"
        errorMessage="No se ha podido cargar el listado de administradores."
        loadingLabel="Cargando administradores…"
      />,
    )
    await screen.findByText('ana.lopez@esibuy.test')

    expect(screen.queryByText('Pendiente')).not.toBeInTheDocument()
  })
})

describe('RoleUsersList (HU37): eliminar administradores', () => {
  async function renderAdministradores(currentUserId) {
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(
      <RoleUsersList
        role="ADMINISTRADOR"
        itemLabel="administradores"
        errorMessage="No se ha podido cargar el listado de administradores."
        loadingLabel="Cargando administradores…"
        currentUserId={currentUserId}
      />,
    )
    await screen.findByText('ana.lopez@esibuy.test')
  }

  it('HU37-CP12: al eliminar a un administrador desde la fila, desaparece de la lista y avisa', async () => {
    const user = userEvent.setup()
    await renderAdministradores('u-1')

    await cambiarEstadoDesdeLaFila(user, 'Eliminar', 'Ana López García')

    expect(await screen.findByText('Ana López García se ha eliminado.')).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByText('ana.lopez@esibuy.test')).not.toBeInTheDocument(), ESPERA)
  })

  it('HU37-CP15: la fila del administrador en sesión tiene la papelera deshabilitada y la de otro, no', async () => {
    await renderAdministradores('usr-1')

    expect(screen.getByRole('button', { name: 'Eliminar a Ana López García' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Eliminar a Diego Delgado Iglesias' })).toBeEnabled()
  })
})
