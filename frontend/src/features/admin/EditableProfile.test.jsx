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
  dni: '12345601V',
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

    const user = userEvent.setup()
    const categoria = await screen.findByRole('combobox', { name: /^categoría principal/i })
    expect(categoria).toHaveValue('Tecnología')

    await user.click(categoria)

    expect(screen.getAllByRole('option').map((opcion) => opcion.textContent)).toEqual(['Tecnología', 'Hogar'])
  })

  it('editableProfile_siNoCarganLasCategorias_avisaYPermiteReintentar', async () => {
    server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    renderizar()

    expect(await screen.findByText(/no se han podido cargar las categorías/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByLabelText(/^categoría principal/i)).toBeInTheDocument()
  })

  // Las categorías solo hacen falta para elegir la principal de un vendedor: el rol de una cuenta no cambia.
  it.each([ROLES.CLIENTE, ROLES.ADMIN])('editableProfile_de_%s_muestraElFormularioSinCargarLasCategorias', (rol) => {
    server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 })))
    renderizar({ rol, usuario: { id: 'u-1', nombre: 'Ada', apellidos: 'Admin', estado: 'ACTIVO' } })

    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Ada')
    expect(screen.queryByText(/no se han podido cargar las categorías/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/cargando el formulario/i)).not.toBeInTheDocument()
  })
})
