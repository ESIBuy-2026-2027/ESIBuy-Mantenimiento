import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const ADMIN = 'admin@esibuy.test'
const MENSAJE_ERROR = 'No se ha podido cargar el listado de usuarios. Inténtalo de nuevo.'

async function abrirListado() {
  await abrirSesionPorApi(ADMIN)
  renderApp('/admin/users')
  await screen.findByRole('heading', { level: 1, name: 'Usuarios' })
}

function filasDeDatos() {
  return within(screen.getByRole('table', { name: 'Usuarios' })).getAllByRole('row').slice(1)
}

function columna(indice) {
  return filasDeDatos().map((fila) => within(fila).getAllByRole('cell')[indice].textContent)
}

async function esperarFilas(cantidad) {
  await waitFor(() => expect(filasDeDatos()).toHaveLength(cantidad))
}

async function abrirEnLaSegundaPagina(user) {
  await abrirListado()
  await esperarFilas(10)
  await user.click(screen.getByRole('button', { name: /siguiente/i }))
  await screen.findByText(/página 2 de 3/i)
}

describe('AdminUsersPage (HU19)', () => {
  it('HU19-CP01: con sesión de admin muestra el título y los 10 primeros usuarios', async () => {
    await abrirListado()

    await esperarFilas(10)
    expect(within(filasDeDatos()[0]).getByText('Marcos Aguilar Bravo')).toBeInTheDocument()
    expect(screen.getByText(/mostrando 1–10 de 28 usuarios/i)).toBeInTheDocument()
  })

  it('HU19-CP02: al buscar "ana" muestra solo los usuarios que coinciden', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'ana')

    await esperarFilas(2)
    expect(columna(0).sort((a, b) => a.localeCompare(b))).toEqual(['Ana López García', 'Mariana Ruiz Pascual'])
  })

  it('HU19-CP03: al filtrar por Vendedor muestra solo vendedores', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Rol' }), 'Vendedor')

    await esperarFilas(7)
    expect(columna(2).every((rol) => rol === 'Vendedor')).toBe(true)
  })

  it('HU19-CP04: al filtrar por Pendiente muestra solo cuentas pendientes', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')

    // Los clientes y los vendedores nuevos quedan pendientes (HU1); los administradores nunca (HU22): en el
    // mock hay 2 vendedores y 2 clientes pendientes.
    await esperarFilas(4)
    expect(columna(3).every((estado) => estado === 'Pendiente')).toBe(true)
  })

  it('HU19-CP05: al pulsar "Siguiente" muestra la página 2 de 3', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    expect(await screen.findByText(/página 2 de 3/i)).toBeInTheDocument()
    expect(screen.getByText(/mostrando 11–20 de 28 usuarios/i)).toBeInTheDocument()
    await esperarFilas(10)
  })

  it('HU19-CP05: mientras llega la página siguiente la tabla se marca como ocupada', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    let liberarPeticion
    const peticionRetenida = new Promise((resolve) => {
      liberarPeticion = resolve
    })
    server.use(http.get('/api/admin/usuarios', () => peticionRetenida, { once: true }))
    const contenedorTabla = () => screen.getByRole('table', { name: 'Usuarios' }).closest('[aria-busy]')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => expect(contenedorTabla()).toHaveAttribute('aria-busy', 'true'))
    liberarPeticion()
    expect(await screen.findByText(/página 2 de 3/i)).toBeInTheDocument()
    expect(contenedorTabla()).toHaveAttribute('aria-busy', 'false')
  })

  it('HU19-CP06: estando en la página 2, al buscar vuelve a la página 1', async () => {
    const user = userEvent.setup()
    await abrirEnLaSegundaPagina(user)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'ana')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(2))
  })

  it('HU19-CP06: estando en la página 2, al filtrar vuelve a la página 1', async () => {
    const user = userEvent.setup()
    await abrirEnLaSegundaPagina(user)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(4))
  })

  it('HU19-CP07: sin coincidencias muestra que no se han encontrado usuarios', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'zzz')

    expect(await screen.findByRole('heading', { name: 'No se han encontrado usuarios' })).toBeInTheDocument()
    expect(screen.queryByRole('table', { name: 'Usuarios' })).not.toBeInTheDocument()
  })

  it('HU19-CP07: sin coincidencias, "Limpiar filtros" vuelve a mostrar todos los usuarios', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'zzz')
    await screen.findByRole('heading', { name: 'No se han encontrado usuarios' })

    await user.click(screen.getByRole('button', { name: 'Limpiar filtros' }))

    await waitFor(() => expect(screen.getByRole('table', { name: 'Usuarios' })).toBeInTheDocument())
    await esperarFilas(10)
    expect(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' })).toHaveValue('')
  })

  it('HU19-CP08: si la API responde 500 muestra un mensaje de error', async () => {
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 })))

    await abrirListado()

    expect(await screen.findByText(MENSAJE_ERROR)).toBeInTheDocument()
    expect(screen.queryByRole('table', { name: 'Usuarios' })).not.toBeInTheDocument()
    expect(screen.queryByText(/cargando usuarios/i)).not.toBeInTheDocument()
  })

  it('HU19-CP08: tras un error, "Reintentar" vuelve a cargar el listado', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirListado()
    await screen.findByText(MENSAJE_ERROR)

    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    await waitFor(() => expect(screen.getByRole('table', { name: 'Usuarios' })).toBeInTheDocument())
    await esperarFilas(10)
    expect(screen.queryByText(MENSAJE_ERROR)).not.toBeInTheDocument()
  })
})

describe('AdminUsersPage (HU22): los administradores no están pendientes', () => {
  const opcionesDeEstado = () =>
    within(screen.getByRole('combobox', { name: 'Estado' }))
      .getAllByRole('option')
      .map((opcion) => opcion.textContent)

  it('adminUsersPage_alFiltrarPorAdministrador_elFiltroDeEstadoNoOfrecePendiente', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    expect(opcionesDeEstado()).toContain('Pendiente')

    await user.selectOptions(screen.getByRole('combobox', { name: 'Rol' }), 'Administrador')

    expect(opcionesDeEstado()).not.toContain('Pendiente')
    expect(opcionesDeEstado()).toEqual(['Todos los estados', 'Activo', 'Bloqueado'])
  })

  it('adminUsersPage_alElegirAdministradorConEstadoPendiente_quitaElEstadoPendiente', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)
    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Pendiente')
    await esperarFilas(4)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Rol' }), 'Administrador')

    await waitFor(() => expect(screen.getByRole('combobox', { name: 'Estado' })).toHaveValue(''))
    await esperarFilas(4)
  })

  it('adminUsersPage_alFiltrarPorCliente_elFiltroDeEstadoSigueOfreciendoPendiente', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: 'Rol' }), 'Cliente')

    expect(opcionesDeEstado()).toContain('Pendiente')
  })
})

describe('AdminUsersPage (HU19): acciones en la tabla', () => {
  async function buscar(user, texto, cantidad = 1) {
    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), texto)
    await esperarFilas(cantidad)
  }

  it('adminUsersPage_laTablaTieneUnaColumnaAcciones_conLupaYBloqueoEnCadaFila', async () => {
    await abrirListado()
    await esperarFilas(10)

    expect(screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)).toEqual([
      'Nombre',
      'Email',
      'Rol',
      'Estado',
      'Fecha de alta',
      'Acciones',
    ])
    expect(screen.getAllByRole('button', { name: /^ver información de/i })).toHaveLength(10)
    expect(screen.getAllByRole('button', { name: /^(bloquear|desbloquear) a /i })).toHaveLength(10)
    expect(screen.getAllByRole('button', { name: /^editar a /i })).toHaveLength(10)
    // La papelera solo está en las filas de administradores (HU37).
    const papeleras = screen.getAllByRole('button', { name: /^eliminar a /i })
    expect(papeleras.length).toBeGreaterThan(0)
    expect(papeleras.length).toBeLessThan(10)
  })

  it('adminUsersPage_alBloquearDesdeLaTabla_laFilaPasaABloqueadoYAvisa', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'carlos')

    await user.click(screen.getByRole('button', { name: 'Bloquear a Carlos Moreno Gil' }))
    const pregunta = await screen.findByText(/seguro que quieres bloquear a carlos moreno gil/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Bloquear', { selector: 'button' }))

    expect(await screen.findByText('Carlos Moreno Gil está bloqueado y ya no puede iniciar sesión.', {}, { timeout: 3000 })).toBeInTheDocument()
    await waitFor(() => expect(columna(3)).toEqual(['Bloqueado']), { timeout: 3000 })
  })

  it('adminUsersPage_alDesbloquearUnaCuentaPendiente_laAcepta', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'gonzalo')

    await user.click(screen.getByRole('button', { name: 'Desbloquear a Gonzalo Ibáñez Roca' }))
    const pregunta = await screen.findByText(/seguro que quieres desbloquear a gonzalo ibáñez roca/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Desbloquear', { selector: 'button' }))

    await waitFor(() => expect(columna(3)).toEqual(['Activo']), { timeout: 3000 })
  })

  it('adminUsersPage_laFilaDelAdministradorEnSesion_noPermiteBloquearse', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'admin@esibuy')

    expect(screen.getByRole('button', { name: 'Bloquear a Ada Admin' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Eliminar a Ada Admin' })).toBeDisabled()
  })

  it('adminUsersPage_siSoloQuedaUnAdministradorActivo_nadieMasPuedeBloquearloNiEliminarlo', async () => {
    server.use(
      http.get('/api/admin/usuarios', ({ request }) => {
        const params = new URL(request.url).searchParams
        if (params.get('role') !== 'ADMINISTRADOR' || params.get('status') !== 'ACTIVO') return undefined
        return HttpResponse.json({
          items: [{ id: 'usr-1', nombre: 'Ana', apellidos: 'López García', rol: 'ADMINISTRADOR', estado: 'ACTIVO' }],
          page: 1,
          size: 1,
          totalItems: 1,
          totalPages: 1,
        })
      }),
    )
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'ana.lopez')

    await waitFor(() => expect(screen.getByRole('button', { name: 'Bloquear a Ana López García' })).toBeDisabled(), { timeout: 3000 })
    expect(screen.getByRole('button', { name: 'Eliminar a Ana López García' })).toBeDisabled()
  })

  it('adminUsersPage_alPulsarLaLupa_abreLaFichaSinAccionesDeEstado', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'carlos')

    await user.click(screen.getByRole('button', { name: 'Ver información de Carlos Moreno Gil' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    expect(await within(dialogo).findByLabelText('Email')).toHaveValue('carlos.moreno@esibuy.test')
    expect(within(dialogo).queryByRole('button', { name: /bloquear|activar/i })).not.toBeInTheDocument()
  })

  it('adminUsersPage_alEditarDesdeLaTabla_laFilaMuestraElNombreNuevo', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'carlos.moreno')

    await user.click(screen.getByRole('button', { name: 'Editar a Carlos Moreno Gil' }))
    const edicion = await screen.findByRole('dialog', { name: /editar usuario/i })
    await user.clear(await within(edicion).findByLabelText(/^nombre/i))
    await user.type(within(edicion).getByLabelText(/^nombre/i), 'Carla')
    await user.click(within(edicion).getByRole('button', { name: /^guardar$/i }))

    expect(await screen.findByText('Los cambios se han guardado.', undefined, { timeout: 3000 })).toBeInTheDocument()
    // Mientras se cierra el formulario, Fluent marca la tabla como oculta: se busca por texto y no por rol.
    expect(await screen.findByText('Carla Moreno Gil', {}, { timeout: 3000 })).toBeInTheDocument()
  })

  it('HU37-CP12: al eliminar a un administrador desde la tabla, la fila desaparece y avisa', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'diego.delgado')

    await user.click(await screen.findByRole('button', { name: 'Eliminar a Diego Delgado Iglesias' }))
    const pregunta = await screen.findByText(/seguro que quieres eliminar a diego delgado iglesias/i)
    await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Eliminar', { selector: 'button' }))

    expect(await screen.findByText('Diego Delgado Iglesias se ha eliminado.', {}, { timeout: 3000 })).toBeInTheDocument()
    expect(await screen.findByText('No se han encontrado usuarios', {}, { timeout: 3000 })).toBeInTheDocument()
  })

  it('HU37-CP18: la fila de un cliente no tiene papelera y la de un administrador sí', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await buscar(user, 'carlos.moreno')

    expect(await screen.findByRole('button', { name: 'Bloquear a Carlos Moreno Gil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Eliminar a Carlos Moreno Gil' })).not.toBeInTheDocument()
  })
})
