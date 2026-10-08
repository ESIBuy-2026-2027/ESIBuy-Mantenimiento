import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Pagination from './Pagination'

function renderPagination(props = {}) {
  const onPageChange = vi.fn()
  render(
    <Pagination page={2} totalPages={3} totalItems={23} pageSize={10} onPageChange={onPageChange} {...props} />,
  )
  return onPageChange
}

describe('Pagination', () => {
  it('pagination_enUnaPaginaIntermedia_muestraLaPaginaYElRangoDeElementos', () => {
    renderPagination()

    expect(screen.getByText(/página 2 de 3/i)).toBeInTheDocument()
    expect(screen.getByText(/mostrando 11–20 de 23/i)).toBeInTheDocument()
  })

  it('pagination_enLaUltimaPagina_ajustaElRangoAlTotal', () => {
    renderPagination({ page: 3 })

    expect(screen.getByText(/mostrando 21–23 de 23/i)).toBeInTheDocument()
  })

  it('pagination_enLaPrimeraPagina_deshabilitaAnterior', () => {
    renderPagination({ page: 1 })

    expect(screen.getByRole('button', { name: /anterior/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /siguiente/i })).toBeEnabled()
  })

  it('pagination_enLaUltimaPagina_deshabilitaSiguiente', () => {
    renderPagination({ page: 3 })

    expect(screen.getByRole('button', { name: /siguiente/i })).toBeDisabled()
    expect(screen.getByRole('button', { name: /anterior/i })).toBeEnabled()
  })

  it('pagination_alPulsarSiguiente_pideLaPaginaSiguiente', async () => {
    const user = userEvent.setup()
    const onPageChange = renderPagination()

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    expect(onPageChange).toHaveBeenCalledWith(3)
  })

  it('pagination_alPulsarAnterior_pideLaPaginaAnterior', async () => {
    const user = userEvent.setup()
    const onPageChange = renderPagination()

    await user.click(screen.getByRole('button', { name: /anterior/i }))

    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('pagination_conUnaSolaPagina_noSeMuestra', () => {
    renderPagination({ page: 1, totalPages: 1, totalItems: 4 })

    expect(screen.queryByRole('navigation', { name: /paginación/i })).not.toBeInTheDocument()
  })
})
