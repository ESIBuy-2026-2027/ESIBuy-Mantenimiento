import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Fallos de la demo del Sprint 1: con la sesión iniciada se podía abrir el formulario de registro
describe('GuestOnlyRoute', () => {
  it('HU1-CP25: con la sesión iniciada /register lleva a la página de inicio del rol', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/register')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
    expect(screen.queryByRole('heading', { name: /crear cuenta/i })).not.toBeInTheDocument()
  })

  it('HU1-CP25: sin sesión /register muestra el formulario de registro', async () => {
    renderApp('/register')

    expect(await screen.findByRole('button', { name: /cliente/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/register$/)
  })
})
