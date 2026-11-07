import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { closeIncident } from '../api/adminIncidents'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Supervisión de incidencias del administrador (HU49, T304) contra el contrato del backend (PR #232):
// GET /api/admin/incidencias devuelve las incidencias de todos, de las más antiguas a las más recientes, por páginas
// (page desde 1), cada una con su autor ({email, rol}, o null si la cuenta se ha eliminado). Del mock de incidencias:
//   15 incidencias en total, así que dos páginas de 10.
//   Página 1: las 10 más antiguas, todas de Carlos (cliente@esibuy.test).
//   Página 2: dos de Carlos, dos de Vera (vendedor@esibuy.test) y una de una cuenta eliminada, la más reciente.

const URL_INCIDENCIAS = '/api/admin/incidencias'
const ADMIN = 'admin@esibuy.test'
const PAGINA_VACIA = { items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 }

async function abrirSupervision() {
  await abrirSesionPorApi(ADMIN)
  renderApp('/admin/incidents')
  await screen.findByRole('heading', { level: 1, name: 'Incidencias' })
}

const lista = () => screen.findByRole('list', { name: 'Incidencias de los usuarios' })
const incidencias = async () => within(await lista()).getAllByRole('listitem')

async function irALaSegundaPagina(user) {
  await lista()
  await user.click(screen.getByRole('button', { name: /siguiente/i }))
  await screen.findByText('Página 2 de 2')
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('AdminIncidentsPage (HU49): lista', () => {
  it('HU49-CP17: abre en la primera página, la de las incidencias más antiguas', async () => {
    await abrirSupervision()

    const filas = await incidencias()
    expect(filas).toHaveLength(10)
    expect(filas[0]).toHaveTextContent('No me llega el pedido')
    expect(filas[9]).toHaveTextContent('Imagen que no carga')
    expect(screen.getByText('Página 1 de 2')).toBeInTheDocument()
    expect(screen.getByText(/Mostrando 1–10 de 15 incidencias/)).toBeInTheDocument()
  })

  it('HU49-CP17: pide una sola vez la primera página de tamaño 10', async () => {
    const paginas = []
    server.events.on('request:start', ({ request }) => {
      const url = new URL(request.url)
      if (url.pathname === URL_INCIDENCIAS) paginas.push(`${url.searchParams.get('page')}/${url.searchParams.get('size')}`)
    })

    await abrirSupervision()
    await lista()

    expect(paginas).toEqual(['1/10'])
    server.events.removeAllListeners()
  })

  it('HU49-CP17: el título y el subtítulo explican la vista', async () => {
    await abrirSupervision()

    expect(screen.getByText(/las incidencias que reportan los clientes y los vendedores/i)).toBeInTheDocument()
  })

  it('HU49-CP18: cada incidencia muestra su autor, su estado y su respuesta', async () => {
    await abrirSupervision()

    const filas = await incidencias()
    expect(filas[1]).toHaveTextContent('Cobro duplicado')
    expect(filas[1]).toHaveTextContent('cliente@esibuy.test')
    expect(filas[1]).toHaveTextContent('Cliente')
    expect(filas[1]).toHaveTextContent('Abierta')
    expect(filas[1]).toHaveTextContent('Estamos revisando el cobro con el banco.')
  })

  it('HU49-CP18: las incidencias de un vendedor muestran su correo y el rol Vendedor', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    expect(filas).toHaveLength(5)
    expect(filas[2]).toHaveTextContent('No puedo subir la imagen de un producto')
    expect(filas[2]).toHaveTextContent('vendedor@esibuy.test')
    expect(filas[2]).toHaveTextContent('Vendedor')
  })

  it('HU49-CP19: la incidencia de una cuenta eliminada dice Cuenta eliminada', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    expect(filas[4]).toHaveTextContent('Cuenta eliminada')
    expect(filas[3]).not.toHaveTextContent('Cuenta eliminada')
  })

  it('HU49-CP20: la paginación lleva a la página siguiente y vuelve a la anterior', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    expect(screen.getByText(/Mostrando 11–15 de 15 incidencias/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /anterior/i }))

    await screen.findByText('Página 1 de 2')
    expect(await incidencias()).toHaveLength(10)
  })

  it('HU49-CP20: el administrador no tiene botones para crear incidencias', async () => {
    await abrirSupervision()
    await lista()

    expect(screen.queryByRole('link', { name: /nueva incidencia/i })).not.toBeInTheDocument()
  })
})

describe('AdminIncidentsPage (HU49): estados', () => {
  it('HU49-CP21: mientras carga muestra un indicador de carga y no la lista', async () => {
    server.use(
      http.get(URL_INCIDENCIAS, async () => {
        await delay(150)
        return HttpResponse.json(PAGINA_VACIA)
      }),
    )
    await abrirSupervision()

    expect(screen.getByText('Cargando incidencias…')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()
    await screen.findByText('No hay incidencias')
    expect(screen.queryByText('Cargando incidencias…')).not.toBeInTheDocument()
  })

  it('HU49-CP21: sin incidencias lo dice, sin lista ni paginación', async () => {
    server.use(http.get(URL_INCIDENCIAS, () => HttpResponse.json(PAGINA_VACIA)))
    await abrirSupervision()

    expect(await screen.findByText('No hay incidencias')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument()
  })

  it('HU49-CP21: si no se pueden cargar lo avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get(URL_INCIDENCIAS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirSupervision()

    expect(await screen.findByText('No se han podido cargar las incidencias. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Incidencias de los usuarios' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await lista()
    expect(screen.queryByText(/no se han podido cargar las incidencias/i)).not.toBeInTheDocument()
  })
})

describe('AdminIncidentsPage (HU49): acceso', () => {
  it('HU49-CP22: un cliente que abre la supervisión vuelve a su inicio', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/admin/incidents')

    await esperarRuta('/products')
    expect(screen.queryByRole('heading', { level: 1, name: 'Incidencias' })).not.toBeInTheDocument()
  })

  it('HU49-CP22: un vendedor que abre la supervisión vuelve a su inicio', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/admin/incidents')

    await esperarRuta('/seller/products')
  })

  it('HU49-CP22: sin sesión, la supervisión lleva al login', async () => {
    renderApp('/admin/incidents')

    await esperarRuta('/login')
  })
})

// Apertura y cierre desde la misma vista (HU50, T305) contra el contrato del backend (PR #233). En la página 1 del
// mock, «Cobro duplicado» (la segunda) ya está abierta y «No me llega el pedido» (la primera), pendiente.
const accion = (fila, nombre) => within(fila).queryByRole('button', { name: new RegExp(`^${nombre}`) })
const AVISO_CAMBIADA = 'Otro administrador ya ha cambiado esa incidencia. Hemos actualizado la lista.'

describe('AdminIncidentsPage (HU50): acciones', () => {
  it('HU50-CP23: una incidencia pendiente se puede abrir y cerrar; una abierta, solo cerrar', async () => {
    await abrirSupervision()

    const [pendiente, abierta] = await incidencias()
    expect(accion(pendiente, 'Abrir')).toBeInTheDocument()
    expect(accion(pendiente, 'Cerrar')).toBeInTheDocument()
    expect(accion(abierta, 'Abrir')).not.toBeInTheDocument()
    expect(accion(abierta, 'Cerrar')).toBeInTheDocument()
  })

  it('HU50-CP24: al abrir una pendiente con una respuesta queda abierta con ella y lo confirma', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    await user.click(accion((await incidencias())[0], 'Abrir'))
    const dialogo = screen.getByRole('dialog', { name: 'Abrir incidencia' })
    await user.type(within(dialogo).getByRole('textbox', { name: /respuesta/i }), 'Lo estamos revisando')
    await user.click(within(dialogo).getByRole('button', { name: 'Abrir incidencia' }))

    expect(await screen.findByText('Incidencia abierta.')).toBeInTheDocument()
    expect(screen.queryByRole('dialog', { name: 'Abrir incidencia' })).not.toBeInTheDocument()
    await waitFor(async () => expect((await incidencias())[0]).toHaveTextContent('Lo estamos revisando'))
    const [abierta] = await incidencias()
    expect(abierta).toHaveTextContent('Abierta')
    expect(accion(abierta, 'Abrir')).not.toBeInTheDocument()
  })

  it('HU50-CP25: al cerrar una incidencia desaparece de la lista y lo confirma', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    await user.click(accion((await incidencias())[1], 'Cerrar'))
    await user.click(screen.getByRole('button', { name: 'Sí, cerrar la incidencia' }))

    expect(await screen.findByText('Incidencia cerrada.')).toBeInTheDocument()
    await screen.findByText(/Mostrando 1–10 de 14 incidencias/)
    expect(screen.queryByText('Cobro duplicado')).not.toBeInTheDocument()
    expect(screen.queryByRole('alertdialog', { name: 'Cerrar incidencia' })).not.toBeInTheDocument()
  })

  it('HU50-CP25: al cerrar la única incidencia de la última página vuelve a la anterior', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi(ADMIN)
    // Deja la página 2 con una sola incidencia: la de la cuenta eliminada
    for (const n of [11, 12, 13, 14]) await closeIncident(`inc-seed-${n}`)
    renderApp('/admin/incidents')
    await irALaSegundaPagina(user)

    const [unica] = await incidencias()
    await user.click(accion(unica, 'Cerrar'))
    await user.click(screen.getByRole('button', { name: 'Sí, cerrar la incidencia' }))

    // Con una sola página la paginación ya no se muestra
    await waitFor(() => expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument())
    const filas = await incidencias()
    expect(filas).toHaveLength(10)
    expect(filas[0]).toHaveTextContent('No me llega el pedido')
    expect(screen.getByText('Incidencia cerrada.')).toBeInTheDocument()
  })

  it('HU50-CP25: al cancelar la confirmación la incidencia sigue en la lista', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    await user.click(accion((await incidencias())[1], 'Cerrar'))
    await user.click(screen.getByRole('button', { name: 'Volver' }))

    expect(screen.queryByRole('alertdialog', { name: 'Cerrar incidencia' })).not.toBeInTheDocument()
    expect((await incidencias())[1]).toHaveTextContent('Cobro duplicado')
    expect(screen.getByText(/Mostrando 1–10 de 15 incidencias/)).toBeInTheDocument()
  })
})

describe('AdminIncidentsPage (HU50): cambios de otro administrador', () => {
  it('HU50-CP26: si otro administrador ya la ha abierto, lo avisa y vuelve a cargar la lista', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    server.use(
      http.patch(`${URL_INCIDENCIAS}/:id/abrir`, () =>
        HttpResponse.json({ mensaje: 'La incidencia ya está abierta', code: 'INCIDENT_ALREADY_OPEN' }, { status: 409 }),
      ),
    )
    const peticiones = []
    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET' && new URL(request.url).pathname === URL_INCIDENCIAS) peticiones.push(request.url)
    })

    await user.click(accion((await incidencias())[0], 'Abrir'))
    await user.type(screen.getByRole('textbox', { name: /respuesta/i }), 'Lo estamos revisando')
    await user.click(screen.getByRole('button', { name: 'Abrir incidencia' }))

    expect(await screen.findByText(AVISO_CAMBIADA)).toBeInTheDocument()
    expect(screen.queryByRole('dialog', { name: 'Abrir incidencia' })).not.toBeInTheDocument()
    await waitFor(() => expect(peticiones).toHaveLength(1))
    server.events.removeAllListeners()
  })

  it('HU50-CP26: si otro administrador ya la ha cerrado, lo avisa y la lista recargada ya no la muestra', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    // Otro administrador la cierra mientras esta vista sigue mostrándola
    await closeIncident('inc-seed-2')

    await user.click(accion((await incidencias())[1], 'Cerrar'))
    await user.click(screen.getByRole('button', { name: 'Sí, cerrar la incidencia' }))

    expect(await screen.findByText(AVISO_CAMBIADA)).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByText('Cobro duplicado')).not.toBeInTheDocument())
  })
})
