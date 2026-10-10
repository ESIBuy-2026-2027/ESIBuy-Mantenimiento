import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { ROLES } from '../../auth/roles'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import EditableProfile from './EditableProfile'

const VENDEDOR = {
  id: 'u-2',
  nombre: 'Vera',
  apellidos: 'Vendedora',
  email: 'vendedor@esibuy.test',
  nombreComercial: 'TecnoVera',
  nif: 'B12345601',
  categoriaPrincipal: 'Tecnología',
  categoriaPrincipalId: 'cat-1',
  estado: 'ACTIVO',
}

function renderizar(props = {}) {
  renderWithProviders(
    <EditableProfile
      rol={ROLES.VENDEDOR}
      usuario={VENDEDOR}
      save={vi.fn()}
      onSaved={vi.fn()}
      onCancel={vi.fn()}
      {...props}
    />,
  )
}

describe('EditableProfile (HU22)', () => {
  it('editableProfile_mientrasCargaLasCategorias_muestraElIndicadorDeCarga', () => {
    renderizar()

    expect(screen.getByText(/cargando el formulario/i)).toBeInTheDocument()
  })

  it('editableProfile_cuandoLlegan_muestraElFormularioConLasCategoriasDelSistema', async () => {
    renderizar()

    const categoria = await screen.findByLabelText(/^categoría principal/i)

    expect(Array.from(categoria.options).map((opcion) => opcion.textContent)).toEqual(['Tecnología', 'Hogar'])
    expect(categoria).toHaveValue('cat-1')
  })

  it('editableProfile_siNoCarganLasCategorias_avisaYPermiteReintentar', async () => {
    server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    renderizar()

    expect(await screen.findByText(/no se han podido cargar las categorías/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByLabelText(/^categoría principal/i)).toBeInTheDocument()
  })

  it('editableProfile_conLockRol_deshabilitaElSelectorDeRol', async () => {
    renderizar({ rol: ROLES.ADMIN, usuario: { id: 'u-1', nombre: 'Ada', apellidos: 'Admin', estado: 'ACTIVO' }, lockRol: true })

    expect(await screen.findByLabelText(/^rol/i)).toBeDisabled()
  })
})
