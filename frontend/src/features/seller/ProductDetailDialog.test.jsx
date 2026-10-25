import { describe, it, expect, vi } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import ProductDetailDialog from './ProductDetailDialog'

// p-1 (Auriculares inalámbricos) es del vendedor y en los datos de ejemplo tiene los dos descuentos activos.
const PRODUCTO_CON_DESCUENTOS = 'p-1'
// p-24 (Smartwatch deportivo) es de otro vendedor: el mock responde 404, como el backend.
const PRODUCTO_DE_OTRO_VENDEDOR = 'p-24'

async function abrirFicha(productId = PRODUCTO_CON_DESCUENTOS) {
  await abrirSesionPorApi('vendedor@esibuy.test')
  const onClose = vi.fn()
  render(
    <FluentProvider theme={webLightTheme}>
      <ProductDetailDialog productId={productId} onClose={onClose} />
    </FluentProvider>,
  )
  const dialogo = await screen.findByRole('dialog', { name: /información del producto/i })
  return { dialogo, onClose }
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
  // Editar y eliminar son de la HU34 y la HU35: en la ficha se ven, pero todavía no se pueden usar.
  it('HU33-CP08: muestra los botones Editar y Eliminar, deshabilitados', async () => {
    const { dialogo } = await abrirFicha()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).getByRole('button', { name: 'Editar' })).toBeDisabled()
    expect(within(dialogo).getByRole('button', { name: 'Eliminar' })).toBeDisabled()
  })
})

describe('ProductDetailDialog (HU33): producto no encontrado', () => {
  it('HU33-CP09: un producto de otro vendedor muestra que no se ha encontrado y no ofrece acciones', async () => {
    const { dialogo } = await abrirFicha(PRODUCTO_DE_OTRO_VENDEDOR)

    expect(await within(dialogo).findByText('No se ha encontrado el producto.')).toBeInTheDocument()
    expect(within(dialogo).queryByLabelText('Nombre')).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: 'Eliminar' })).not.toBeInTheDocument()
  })

  it.each([403, 404])('HU33-CP09: una respuesta %i muestra que no se ha encontrado el producto', async (status) => {
    server.use(http.get('/api/seller/products/:id', () => new HttpResponse(null, { status })))
    const { dialogo } = await abrirFicha()

    expect(await within(dialogo).findByText('No se ha encontrado el producto.')).toBeInTheDocument()
  })
})
