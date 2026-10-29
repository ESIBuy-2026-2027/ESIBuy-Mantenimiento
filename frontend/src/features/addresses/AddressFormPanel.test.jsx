import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import AddressFormPanel from './AddressFormPanel'

// Panel para añadir o editar una dirección de envío (HU5).

const URL_DIRECCIONES = '/api/customer/addresses'

const GUARDADA = {
  id: 'd-1',
  destinatario: 'Carlos Cliente',
  calle: 'Calle Mayor 1',
  detalles: null,
  codigoPostal: '13071',
  ciudad: 'Ciudad Real',
  provincia: 'Ciudad Real',
  telefono: '612345678',
  principal: true,
}

// Guarda las peticiones que llegan al backend: método, id y cuerpo
function registrarPeticiones() {
  const peticiones = []
  const responder = async ({ request, params }) => {
    const cuerpo = await request.json()
    peticiones.push({ metodo: request.method, id: params.id, cuerpo })
    return HttpResponse.json({ id: params.id ?? 'd-9', ...cuerpo, principal: false }, { status: params.id ? 200 : 201 })
  }
  server.use(http.post(URL_DIRECCIONES, responder), http.put(`${URL_DIRECCIONES}/:id`, responder))
  return peticiones
}

function abrirPanel(direccion) {
  const onSaved = vi.fn()
  const onCancel = vi.fn()
  renderWithProviders(<AddressFormPanel direccion={direccion} onSaved={onSaved} onCancel={onCancel} />)
  return { onSaved, onCancel }
}

async function rellenar(usuario, valores) {
  const etiquetas = {
    destinatario: /destinatario/i,
    calle: /calle y número/i,
    detalles: /piso, puerta/i,
    codigoPostal: /código postal/i,
    ciudad: /ciudad/i,
    provincia: /provincia/i,
    telefono: /teléfono/i,
  }
  for (const [campo, valor] of Object.entries(valores)) {
    const input = screen.getByLabelText(etiquetas[campo])
    await usuario.clear(input)
    if (valor) await usuario.type(input, valor)
  }
}

const VALIDA = {
  destinatario: 'Ana López',
  calle: 'Calle Ancha 3',
  detalles: '1º A',
  codigoPostal: '45001',
  ciudad: 'Toledo',
  provincia: 'Toledo',
  telefono: '698765432',
}

describe('AddressFormPanel (HU5): validación', () => {
  it('HU5-CP22: no envía una dirección sin los campos obligatorios y marca cada uno', async () => {
    const peticiones = registrarPeticiones()
    const usuario = userEvent.setup()
    abrirPanel()

    // Los detalles son lo único opcional
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    expect(screen.getByText('Escribe el nombre de quien recibe el pedido.')).toBeInTheDocument()
    expect(screen.getByText('Escribe la calle y el número.')).toBeInTheDocument()
    expect(screen.getByText('Escribe la ciudad.')).toBeInTheDocument()
    expect(screen.getByText('Escribe la provincia.')).toBeInTheDocument()
    expect(screen.getByText('El código postal tiene 5 dígitos.')).toBeInTheDocument()
    expect(screen.getByText('El teléfono tiene 9 dígitos.')).toBeInTheDocument()
    expect(peticiones).toHaveLength(0)
  })

  it('HU5-CP22: no envía un código postal, un teléfono o un texto con formato o longitud incorrectos', async () => {
    const peticiones = registrarPeticiones()
    const usuario = userEvent.setup()
    abrirPanel()

    // Mismos límites que el backend: 100 caracteres (150 en la calle), 5 dígitos y 9 dígitos
    await rellenar(usuario, {
      ...VALIDA,
      destinatario: 'a'.repeat(101),
      calle: 'a'.repeat(151),
      codigoPostal: '4500',
      telefono: '69876543A',
    })
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    expect(screen.getByText('El destinatario tiene como máximo 100 caracteres.')).toBeInTheDocument()
    expect(screen.getByText('La calle tiene como máximo 150 caracteres.')).toBeInTheDocument()
    expect(screen.getByText('El código postal tiene 5 dígitos.')).toBeInTheDocument()
    expect(screen.getByText('El teléfono tiene 9 dígitos.')).toBeInTheDocument()
    expect(peticiones).toHaveLength(0)
  })
})

describe('AddressFormPanel (HU5): alta', () => {
  it('HU5-CP23: empieza vacío y guarda la dirección nueva con un POST, sin espacios sobrantes', async () => {
    const peticiones = registrarPeticiones()
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel()

    expect(screen.getByRole('heading', { level: 2, name: /nueva dirección/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/destinatario/i)).toHaveValue('')
    await rellenar(usuario, { ...VALIDA, ciudad: '  Toledo  ' })
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    await waitFor(() => expect(onSaved).toHaveBeenCalled())
    expect(peticiones).toEqual([{ metodo: 'POST', id: undefined, cuerpo: VALIDA }])
  })

  it('HU5-CP23: al cancelar no se envía nada', async () => {
    const peticiones = registrarPeticiones()
    const usuario = userEvent.setup()
    const { onSaved, onCancel } = abrirPanel()

    await rellenar(usuario, VALIDA)
    await usuario.click(screen.getByRole('button', { name: /cancelar/i }))

    expect(onCancel).toHaveBeenCalled()
    expect(onSaved).not.toHaveBeenCalled()
    expect(peticiones).toHaveLength(0)
  })
})

describe('AddressFormPanel (HU5): edición', () => {
  it('HU5-CP24: al editar empieza con los datos de la dirección y guarda los cambios con un PUT', async () => {
    const peticiones = registrarPeticiones()
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel(GUARDADA)

    expect(screen.getByRole('heading', { level: 2, name: /editar dirección/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/calle y número/i)).toHaveValue('Calle Mayor 1')
    expect(screen.getByLabelText(/código postal/i)).toHaveValue('13071')
    expect(screen.getByLabelText(/piso, puerta/i)).toHaveValue('')
    await rellenar(usuario, { calle: 'Calle Mayor 3' })
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    await waitFor(() => expect(onSaved).toHaveBeenCalled())
    // La principal no se envía (editar no la cambia) y unos detalles vacíos van como ausentes
    expect(peticiones).toEqual([
      {
        metodo: 'PUT',
        id: 'd-1',
        cuerpo: {
          destinatario: 'Carlos Cliente',
          calle: 'Calle Mayor 3',
          detalles: null,
          codigoPostal: '13071',
          ciudad: 'Ciudad Real',
          provincia: 'Ciudad Real',
          telefono: '612345678',
        },
      },
    ])
  })

  it('HU5-CP24: si la dirección que se edita ya no existe, lo avisa', async () => {
    server.use(
      http.put(`${URL_DIRECCIONES}/:id`, () => HttpResponse.json({ code: 'ADDRESS_NOT_FOUND' }, { status: 404 })),
    )
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel(GUARDADA)

    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    expect(await screen.findByText(/esa dirección ya no existe/i)).toBeInTheDocument()
    expect(onSaved).not.toHaveBeenCalled()
  })
})

describe('AddressFormPanel (HU5): rechazo del backend', () => {
  it('HU5-CP25: una dirección rechazada por el backend muestra un aviso sin repetir los datos enviados', async () => {
    server.use(
      http.post(URL_DIRECCIONES, () =>
        HttpResponse.json({ mensaje: 'La dirección de envío es inválida', code: 'VALIDATION_ERROR' }, { status: 400 }),
      ),
    )
    const usuario = userEvent.setup()
    const { onSaved } = abrirPanel()

    await rellenar(usuario, VALIDA)
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    const aviso = await screen.findByText(/no se ha podido guardar la dirección/i)
    expect(aviso).not.toHaveTextContent('698765432')
    expect(aviso).not.toHaveTextContent('inválida')
    expect(onSaved).not.toHaveBeenCalled()
  })

  it('HU5-CP25: al superar el límite de direcciones avisa de que ya hay 5', async () => {
    server.use(
      http.post(URL_DIRECCIONES, () => HttpResponse.json({ code: 'ADDRESS_LIMIT_REACHED' }, { status: 409 })),
    )
    const usuario = userEvent.setup()
    abrirPanel()

    await rellenar(usuario, VALIDA)
    await usuario.click(screen.getByRole('button', { name: /guardar dirección/i }))

    expect(await screen.findByText(/ya tienes 5 direcciones/i)).toBeInTheDocument()
  })
})
