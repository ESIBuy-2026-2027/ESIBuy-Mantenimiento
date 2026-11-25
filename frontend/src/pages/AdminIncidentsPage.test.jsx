import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { closeIncident, openIncident, resolveIncident } from '../api/adminIncidents'
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

  it('HU49-CP18: cada tarjeta muestra título, descripción y estado; autor, fecha y respuesta van en el detalle', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    const filas = await incidencias()
    expect(filas[1]).toHaveTextContent('Cobro duplicado')
    expect(filas[1]).toHaveTextContent('Me han cobrado dos veces el mismo pedido.')
    expect(filas[1]).toHaveTextContent('Abierta')
    expect(filas[1]).not.toHaveTextContent('cliente@esibuy.test')
    expect(filas[1]).not.toHaveTextContent('Estamos revisando el cobro con el banco.')

    await user.click(accion(filas[1], 'Ver detalle'))
    const dialogo = screen.getByRole('dialog', { name: 'Detalle de la incidencia' })
    expect(dialogo).toHaveTextContent('cliente@esibuy.test (Cliente)')
    expect(dialogo).toHaveTextContent('Estamos revisando el cobro con el banco.')
  })

  it('HU49-CP18: las incidencias de un vendedor muestran su correo y el rol Vendedor en el detalle', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    expect(filas).toHaveLength(5)
    expect(filas[2]).toHaveTextContent('No puedo subir la imagen de un producto')

    await user.click(accion(filas[2], 'Ver detalle'))
    const dialogo = screen.getByRole('dialog', { name: 'Detalle de la incidencia' })
    expect(dialogo).toHaveTextContent('vendedor@esibuy.test (Vendedor)')
  })

  it('HU49-CP19: la incidencia de una cuenta eliminada dice Cuenta eliminada en el detalle', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const filas = await incidencias()
    await user.click(accion(filas[4], 'Ver detalle'))
    const dialogo = screen.getByRole('dialog', { name: 'Detalle de la incidencia' })
    expect(dialogo).toHaveTextContent('Cuenta eliminada')
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

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/seller/products$')))
  })

  it('HU49-CP22: sin sesión, la supervisión lleva al login', async () => {
    renderApp('/admin/incidents')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/login$')))
  })
})

// Apertura y cierre desde la misma vista (HU50, T305) contra el contrato del backend (PR #233). En la página 1 del
// mock, «Cobro duplicado» (la segunda) ya está abierta y «No me llega el pedido» (la primera), pendiente.
const accion = (fila, nombre) => within(fila).queryByRole('button', { name: new RegExp(`^${nombre}`) })
const AVISO_CAMBIADA = 'Otro administrador ya ha cambiado esa incidencia. Hemos actualizado la lista.'

describe('AdminIncidentsPage (HU50): acciones', () => {
  it('HU50-CP23: una incidencia pendiente se puede abrir y cerrar; una abierta no se vuelve a abrir', async () => {
    await abrirSupervision()

    const [pendiente, abierta] = await incidencias()
    expect(within(pendiente).getByRole('button', { name: /^Abrir incidencia/ })).toHaveTextContent('Abrir incidencia')
    expect(within(pendiente).getByRole('button', { name: /^Cerrar incidencia/ })).toHaveTextContent('Cerrar incidencia')
    expect(accion(abierta, 'Abrir')).not.toBeInTheDocument()
    expect(within(abierta).getByRole('button', { name: /^Cerrar incidencia/ })).toHaveTextContent('Cerrar incidencia')
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
    await waitFor(async () => expect((await incidencias())[0]).toHaveTextContent('Abierta'))
    const [abierta] = await incidencias()
    expect(accion(abierta, 'Abrir')).not.toBeInTheDocument()

    await user.click(accion(abierta, 'Ver detalle'))
    expect(screen.getByRole('dialog', { name: 'Detalle de la incidencia' })).toHaveTextContent('Lo estamos revisando')
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
    // Primero, que la lista ya esté cargada: si no, su primera petición contaría como la recarga
    await lista()
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
    // Primero, que la vista ya la muestre: si se cierra antes de cargar la lista, la fila [1] sería otra incidencia
    const filas = await incidencias()
    expect(filas[1]).toHaveTextContent('Cobro duplicado')
    // Otro administrador la cierra mientras esta vista sigue mostrándola
    await closeIncident('inc-seed-2')

    await user.click(accion(filas[1], 'Cerrar'))
    await user.click(screen.getByRole('button', { name: 'Sí, cerrar la incidencia' }))

    expect(await screen.findByText(AVISO_CAMBIADA)).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByText('Cobro duplicado')).not.toBeInTheDocument())
  })
})

describe('AdminIncidentsPage (PBI 239): filtros, ordenación y detalle', () => {
  it('PBI239-CP06: permite filtrar por estado y volver a mostrar todas', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await irALaSegundaPagina(user)

    const selector = screen.getByRole('combobox', { name: 'Estado' })
    await user.selectOptions(selector, 'ABIERTA')

    await waitFor(async () => expect(await incidencias()).toHaveLength(4))
    expect((await incidencias())[0]).toHaveTextContent('Cobro duplicado')

    await user.selectOptions(selector, '')
    expect(await screen.findByText('Mostrando 1–10 de 15 incidencias')).toBeInTheDocument()
  })

  it('PBI239-CP07: al pulsar Ver detalle abre el diálogo sin botones de abrir o cerrar incidencia', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    await user.click(accion((await incidencias())[0], 'Ver detalle'))

    const dialogo = screen.getByRole('dialog', { name: 'Detalle de la incidencia' })
    expect(dialogo).toHaveTextContent('No me llega el pedido')
    expect(dialogo).toHaveTextContent('Pendiente')
    expect(dialogo).toHaveTextContent('cliente@esibuy.test (Cliente)')
    expect(within(dialogo).queryByRole('button', { name: /abrir incidencia/i })).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: /cerrar incidencia/i })).not.toBeInTheDocument()

    await user.click(within(dialogo).getByRole('button', { name: 'Volver' }))
    expect(screen.queryByRole('dialog', { name: 'Detalle de la incidencia' })).not.toBeInTheDocument()
  })

  it('PBI239-CP08: permite buscar por nombre, ordenar por nombre y cambiar a descendente', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await lista()

    await user.type(screen.getByPlaceholderText('Escribe un nombre'), 'Estadísticas')
    await waitFor(async () => expect(await incidencias()).toHaveLength(1))
    expect((await incidencias())[0]).toHaveTextContent('Estadísticas sin datos')

    await user.clear(screen.getByPlaceholderText('Escribe un nombre'))
    await waitFor(async () => expect(await incidencias()).toHaveLength(10))

    await user.selectOptions(screen.getByRole('combobox', { name: 'Ordenar por' }), 'asunto')
    await waitFor(async () => expect((await incidencias())[0]).toHaveTextContent('Aviso de stock'))

    await user.click(screen.getByRole('button', { name: 'Orden ascendente' }))
    await waitFor(async () => expect((await incidencias())[0]).toHaveTextContent('Valoración que no se publica'))
  })
})

describe('AdminIncidentsPage (HU192): avisos', () => {
  it('HU192-CP27: los avisos se anuncian en una región viva que ya está en la página', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    await lista()
    const region = screen.getByRole('main').querySelector('[aria-live="polite"]')
    expect(region).toBeEmptyDOMElement()

    await user.click(accion((await incidencias())[0], 'Abrir'))
    await user.type(screen.getByRole('textbox', { name: /respuesta/i }), 'Lo estamos revisando')
    await user.click(screen.getByRole('button', { name: 'Abrir incidencia' }))

    await waitFor(() => expect(region).toHaveTextContent('Incidencia abierta.'))
    expect(screen.getByText('Incidencia abierta.').closest('[aria-live]')).toBe(region)
  })
})

// Resolución desde la supervisión (HU192, T511). En la página 1 del mock, «No me llega el pedido» (la primera) está
// pendiente y «Cobro duplicado» (la segunda), abierta. Hay cuatro abiertas en total.
const DIALOGO_RESOLVER = 'Marcar incidencia como resuelta'
const AVISO_RESUELTA = 'Incidencia marcada como resuelta.'
const AVISO_YA_RESUELTA = 'Esa incidencia ya estaba resuelta. Hemos actualizado la lista.'
const botonResolver = (fila) => accion(fila, 'Marcar como resuelta')
const confirmarResolucion = () => screen.getByRole('button', { name: 'Sí, marcar como resuelta' })
const regionDeAvisos = () => screen.getByRole('main').querySelector('[aria-live="polite"]')

describe('AdminIncidentsPage (HU192): resolver', () => {
  it('HU192-CP28: solo las incidencias abiertas tienen el botón Marcar como resuelta', async () => {
    await abrirSupervision()

    const [pendiente, abierta] = await incidencias()
    expect(botonResolver(pendiente)).not.toBeInTheDocument()
    expect(botonResolver(abierta)).toHaveTextContent('Marcar como resuelta')
    expect(botonResolver(abierta)).toHaveAccessibleName('Marcar como resuelta la incidencia «Cobro duplicado»')
  })

  it('HU192-CP29: al resolver, la lista y el detalle se actualizan sin recargar y lo anuncia', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    const listaInicial = await lista()

    await user.click(botonResolver((await incidencias())[1]))
    const dialogo = screen.getByRole('alertdialog', { name: DIALOGO_RESOLVER })
    await user.click(within(dialogo).getByRole('button', { name: 'Sí, marcar como resuelta' }))

    await waitFor(() => expect(regionDeAvisos()).toHaveTextContent(AVISO_RESUELTA))
    expect(screen.queryByRole('alertdialog', { name: DIALOGO_RESOLVER })).not.toBeInTheDocument()
    await waitFor(async () => expect((await incidencias())[1]).toHaveTextContent('Resuelta'))
    const resuelta = (await incidencias())[1]
    expect(botonResolver(resuelta)).not.toBeInTheDocument()
    expect(await lista()).toBe(listaInicial)

    await user.click(accion(resuelta, 'Ver detalle'))
    const detalle = screen.getByRole('dialog', { name: 'Detalle de la incidencia' })
    expect(detalle).toHaveTextContent('Resuelta')
    expect(detalle).toHaveTextContent(/Fecha de resolución: \d{2}\/\d{2}\/\d{4}, \d{2}:\d{2}/)
  })

  it('HU192-CP29: al cancelar la confirmación la incidencia sigue abierta', async () => {
    const user = userEvent.setup()
    await abrirSupervision()

    await user.click(botonResolver((await incidencias())[1]))
    await user.click(screen.getByRole('button', { name: 'Volver' }))

    expect(screen.queryByRole('alertdialog', { name: DIALOGO_RESOLVER })).not.toBeInTheDocument()
    expect((await incidencias())[1]).toHaveTextContent('Abierta')
    expect(screen.queryByText(AVISO_RESUELTA)).not.toBeInTheDocument()
  })

  it('HU192-CP30: si otro administrador ya la ha resuelto, lo avisa y la lista recargada la muestra resuelta', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    const filas = await incidencias()
    await resolveIncident('inc-seed-2')

    await user.click(botonResolver(filas[1]))
    await user.click(confirmarResolucion())

    await waitFor(() => expect(regionDeAvisos()).toHaveTextContent(AVISO_YA_RESUELTA))
    await waitFor(async () => expect((await incidencias())[1]).toHaveTextContent('Resuelta'))
  })

  it('HU192-CP30: si otro administrador ya la ha cerrado, lo avisa y la lista recargada ya no la muestra', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    const filas = await incidencias()
    await closeIncident('inc-seed-2')

    await user.click(botonResolver(filas[1]))
    await user.click(confirmarResolucion())

    await waitFor(() => expect(regionDeAvisos()).toHaveTextContent(AVISO_CAMBIADA))
    await waitFor(() => expect(screen.queryByText('Cobro duplicado')).not.toBeInTheDocument())
  })

  it('HU192-CP31: el filtro de estado permite ver solo las resueltas', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi(ADMIN)
    await resolveIncident('inc-seed-4')
    renderApp('/admin/incidents')
    await lista()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'RESUELTA')

    await waitFor(async () => expect(await incidencias()).toHaveLength(1))
    expect((await incidencias())[0]).toHaveTextContent('Producto dañado')
  })

  it('HU192-CP31: al resolver la única abierta de la última página filtrada vuelve a la anterior', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi(ADMIN)
    // Abre siete pendientes para que haya once abiertas: dos páginas, la segunda con una sola
    for (const n of [1, 3, 5, 6, 7, 8, 10]) await openIncident(`inc-seed-${n}`, 'Respuesta')
    renderApp('/admin/incidents')
    await lista()
    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'ABIERTA')
    await screen.findByText('Mostrando 1–10 de 11 incidencias')
    await irALaSegundaPagina(user)
    await waitFor(async () => expect(await incidencias()).toHaveLength(1))

    await user.click(botonResolver((await incidencias())[0]))
    await user.click(confirmarResolucion())

    await waitFor(() => expect(screen.queryByRole('navigation', { name: 'Paginación' })).not.toBeInTheDocument())
    await waitFor(async () => expect(await incidencias()).toHaveLength(10))
    expect(screen.getByText(AVISO_RESUELTA)).toBeInTheDocument()
  })

  it('HU192-CP32: se resuelve solo con el teclado y el foco no se pierde', async () => {
    const user = userEvent.setup()
    await abrirSupervision()
    const boton = botonResolver((await incidencias())[1])

    boton.focus()
    await user.keyboard('{Enter}')
    await user.keyboard('{Escape}')

    expect(screen.queryByRole('alertdialog', { name: DIALOGO_RESOLVER })).not.toBeInTheDocument()
    await waitFor(() => expect(boton).toHaveFocus())

    await user.keyboard('{Enter}')
    confirmarResolucion().focus()
    await user.keyboard('{Enter}')

    await waitFor(() => expect(regionDeAvisos()).toHaveTextContent(AVISO_RESUELTA))
    await waitFor(() => expect(regionDeAvisos()).toHaveFocus())
  })
})
