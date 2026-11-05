import { describe, it, expect, vi } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { MemoryRouter } from 'react-router-dom'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import ProductDetailDialog from './ProductDetailDialog'

// p-1 (Auriculares inalámbricos) es del vendedor y en los datos de ejemplo tiene los dos descuentos activos.
const PRODUCTO_CON_DESCUENTOS = 'p-1'
// p-24 (Smartwatch deportivo) es de otro vendedor: el mock responde 404, como el backend.
const PRODUCTO_DE_OTRO_VENDEDOR = 'p-24'
// p-6 (Webcam Full HD) es del vendedor y no está disponible.
const PRODUCTO_NO_DISPONIBLE = 'p-6'

async function abrirFicha(productId = PRODUCTO_CON_DESCUENTOS) {
  await abrirSesionPorApi('vendedor@esibuy.test')
  const onClose = vi.fn()
  const onToggleAvailability = vi.fn()
  const onDelete = vi.fn()
  render(
    <MemoryRouter>
      <FluentProvider theme={webLightTheme}>
        <ProductDetailDialog
          productId={productId}
          onClose={onClose}
          onToggleAvailability={onToggleAvailability}
          onDelete={onDelete}
        />
      </FluentProvider>
    </MemoryRouter>,
  )
  const dialogo = await screen.findByRole('dialog', { name: /información del producto/i })
  return { dialogo, onClose, onToggleAvailability, onDelete }
}

describe('ProductDetailDialog (HU33): datos', () => {
  it('HU33-CP07: muestra todos los datos del producto, con el nombre de su categoría', async () => {
    const { dialogo } = await abrirFicha()

    expect(await within(dialogo).findByLabelText('Nombre')).toHaveValue('Auriculares inalámbricos')
    expect(within(dialogo).getByLabelText('Descripción')).toHaveValue('Descripción de Auriculares inalámbricos.')
    // Intl separa el importe del símbolo con un espacio irrompible
    expect(within(dialogo).getByLabelText('Precio').value).toMatch(/^59,90\s€$/)
    expect(within(dialogo).getByLabelText('Stock')).toHaveValue('34')
    expect(await within(dialogo).findByDisplayValue('Tecnología')).toBe(within(dialogo).getByLabelText('Categoría'))
    expect(within(dialogo).getByLabelText('Imagen')).toHaveValue('Sin imagen')
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Disponible')
    expect(within(dialogo).getByLabelText('Descuento')).toHaveValue('10 %')
    expect(within(dialogo).getByLabelText('Descuento premium')).toHaveValue('15 %')
  })

  it('HU33-CP07: todos los campos son de solo lectura', async () => {
    const { dialogo } = await abrirFicha()
    await within(dialogo).findByLabelText('Nombre')

    const campos = within(dialogo).getAllByRole('textbox')
    expect(campos).toHaveLength(9)
    campos.forEach((campo) => expect(campo).toHaveAttribute('readonly'))
  })
})

describe('ProductDetailDialog (HU33): acciones', () => {
  // Editar lleva al formulario de edición (HU34) y Eliminar pide la confirmación del listado (HU35).
  it('HU33-CP08: muestra las acciones Editar, que lleva al formulario de edición, y Eliminar, que avisa con el producto', async () => {
    const user = userEvent.setup()
    const { dialogo, onDelete } = await abrirFicha()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).getByRole('link', { name: 'Editar' })).toHaveAttribute('href', '/seller/products/p-1/edit')
    await user.click(within(dialogo).getByRole('button', { name: 'Eliminar' }))
    expect(onDelete).toHaveBeenCalledWith(expect.objectContaining({ id: 'p-1', nombre: 'Auriculares inalámbricos' }))
  })
})

describe('ProductDetailDialog (HU16): activar y desactivar', () => {
  it('HU16-CP05: un producto disponible ofrece Desactivar, que avisa con el producto', async () => {
    const user = userEvent.setup()
    const { dialogo, onToggleAvailability } = await abrirFicha()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: 'Activar' })).not.toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: 'Desactivar' }))
    expect(onToggleAvailability).toHaveBeenCalledWith(expect.objectContaining({ id: 'p-1', disponible: true }))
  })

  it('HU16-CP05: un producto no disponible ofrece Activar, que avisa con el producto', async () => {
    const user = userEvent.setup()
    const { dialogo, onToggleAvailability } = await abrirFicha(PRODUCTO_NO_DISPONIBLE)
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: 'Desactivar' })).not.toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: 'Activar' }))
    expect(onToggleAvailability).toHaveBeenCalledWith(expect.objectContaining({ id: 'p-6', disponible: false }))
  })
})

describe('ProductDetailDialog (HU33): producto no encontrado', () => {
  it('HU33-CP09: un producto de otro vendedor muestra que no se ha encontrado y no ofrece acciones', async () => {
    const { dialogo } = await abrirFicha(PRODUCTO_DE_OTRO_VENDEDOR)

    expect(await within(dialogo).findByText('No se ha encontrado el producto.')).toBeInTheDocument()
    expect(within(dialogo).queryByLabelText('Nombre')).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('link', { name: 'Editar' })).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: 'Eliminar' })).not.toBeInTheDocument()
  })

  it.each([403, 404])('HU33-CP09: una respuesta %i muestra que no se ha encontrado el producto', async (status) => {
    server.use(http.get('/api/seller/products/:id', () => new HttpResponse(null, { status })))
    const { dialogo } = await abrirFicha()

    expect(await within(dialogo).findByText('No se ha encontrado el producto.')).toBeInTheDocument()
  })
})

describe('ProductDetailDialog (HU33): error de carga', () => {
  it('HU33-CP10: si la carga falla por otro motivo, muestra un error genérico y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/seller/products/:id', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const { dialogo } = await abrirFicha()

    expect(await within(dialogo).findByText('No se ha podido cargar el producto.')).toBeInTheDocument()
    expect(within(dialogo).queryByText('No se ha encontrado el producto.')).not.toBeInTheDocument()

    await user.click(within(dialogo).getByRole('button', { name: 'Reintentar' }))

    expect(await within(dialogo).findByLabelText('Nombre')).toHaveValue('Auriculares inalámbricos')
  })
})
