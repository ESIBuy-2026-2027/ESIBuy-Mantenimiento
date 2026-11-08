import { useState } from 'react'
import { describe, it, expect } from 'vitest'
import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { RouterProvider, createMemoryRouter } from 'react-router-dom'
import { useUnsavedChangesGuard } from './useUnsavedChangesGuard'

function Pantalla({ inicialmenteConCambios }) {
  const [conCambios, setConCambios] = useState(inicialmenteConCambios)
  const { blocked, proceed, reset } = useUnsavedChangesGuard(conCambios)

  return (
    <>
      <output data-testid="bloqueado">{String(blocked)}</output>
      <button type="button" onClick={proceed}>
        salir
      </button>
      <button type="button" onClick={reset}>
        quedarse
      </button>
      <button type="button" onClick={() => setConCambios(false)}>
        sin cambios
      </button>
    </>
  )
}

function renderizar(inicialmenteConCambios) {
  const router = createMemoryRouter([
    { path: '/', element: <Pantalla inicialmenteConCambios={inicialmenteConCambios} /> },
    { path: '/otra', element: <p>otra pantalla</p> },
  ])
  render(<RouterProvider router={router} />)
  return router
}

const irAOtraPantalla = (router) => act(() => router.navigate('/otra'))

// El evento que lanza el navegador al cerrar o recargar la pestaña; si se cancela, el navegador pregunta antes
const cerrarLaPestana = () => {
  const evento = new Event('beforeunload', { cancelable: true })
  window.dispatchEvent(evento)
  return evento
}

describe('useUnsavedChangesGuard (HU45): salir de la pantalla', () => {
  it('HU45-CP16: con cambios sin guardar, navegar a otra pantalla se bloquea y se avisa', async () => {
    const router = renderizar(true)

    await irAOtraPantalla(router)

    expect(router.state.location.pathname).toBe('/')
    expect(screen.getByTestId('bloqueado')).toHaveTextContent('true')
  })

  it('HU45-CP16: sin cambios se navega a otra pantalla sin avisar', async () => {
    const router = renderizar(false)

    await irAOtraPantalla(router)

    expect(router.state.location.pathname).toBe('/otra')
  })

  it('HU45-CP16: al decidir salir se completa la navegación que estaba bloqueada', async () => {
    const user = userEvent.setup()
    const router = renderizar(true)
    await irAOtraPantalla(router)

    await user.click(screen.getByRole('button', { name: 'salir' }))

    expect(router.state.location.pathname).toBe('/otra')
  })

  it('HU45-CP16: al decidir quedarse se cancela la navegación y se sigue en la pantalla', async () => {
    const user = userEvent.setup()
    const router = renderizar(true)
    await irAOtraPantalla(router)

    await user.click(screen.getByRole('button', { name: 'quedarse' }))

    expect(router.state.location.pathname).toBe('/')
    expect(screen.getByTestId('bloqueado')).toHaveTextContent('false')
  })

  it('HU45-CP16: cuando deja de haber cambios ya no se bloquea la navegación', async () => {
    const user = userEvent.setup()
    const router = renderizar(true)

    await user.click(screen.getByRole('button', { name: 'sin cambios' }))
    await irAOtraPantalla(router)

    expect(router.state.location.pathname).toBe('/otra')
  })
})

describe('useUnsavedChangesGuard (HU45): cerrar o recargar la pestaña', () => {
  it('HU45-CP16: con cambios sin guardar, cerrar o recargar la pestaña pide confirmación al navegador', () => {
    renderizar(true)

    expect(cerrarLaPestana().defaultPrevented).toBe(true)
  })

  it('HU45-CP16: sin cambios, cerrar o recargar la pestaña no pregunta nada', () => {
    renderizar(false)

    expect(cerrarLaPestana().defaultPrevented).toBe(false)
  })

  it('HU45-CP16: cuando deja de haber cambios ya no se pide confirmación al cerrar', async () => {
    const user = userEvent.setup()
    renderizar(true)

    await user.click(screen.getByRole('button', { name: 'sin cambios' }))

    expect(cerrarLaPestana().defaultPrevented).toBe(false)
  })

  it('HU45-CP16: al desmontar la pantalla se deja de escuchar el cierre de la pestaña', () => {
    const router = createMemoryRouter([{ path: '/', element: <Pantalla inicialmenteConCambios /> }])
    const { unmount } = render(<RouterProvider router={router} />)

    unmount()

    expect(cerrarLaPestana().defaultPrevented).toBe(false)
  })
})
