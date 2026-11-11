
import { describe, it, expect } from 'vitest'
import { cleanup, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

async function abrirSeguridad(email, ruta) {
    await abrirSesionPorApi(email)
    renderApp(ruta)

    expect(await screen.findByRole('heading', { name: 'Seguridad de la cuenta' })).toBeInTheDocument()
}

describe('SecurityFactors: configuración de seguridad', () => {
    it('muestra los dos factores desactivados inicialmente', async () => {
        await abrirSeguridad('cliente@esibuy.test', '/profile')

        expect(screen.getByRole('checkbox', { name: /código de verificación por correo/i })).not.toBeChecked()
        expect(screen.getByRole('checkbox', { name: /microsoft authenticator/i })).not.toBeChecked()
    })

    it('permite activar el código por correo y guardar la configuración', async () => {
        const user = userEvent.setup()
        let cuerpo

        server.use(
            http.patch('/api/seguridad/factores', async ({ request }) => {
                cuerpo = await request.json()
                return HttpResponse.json({
                    ...cuerpo,
                    authenticatorConfigured: false,
                    authenticatorVerifiedAt: null,
                })
            }),
        )

        await abrirSeguridad('cliente@esibuy.test', '/profile')

        const correo = screen.getByRole('checkbox', { name: /código de verificación por correo/i })
        await user.click(correo)
        await user.click(screen.getByRole('button', { name: /guardar configuración/i }))

        expect(await screen.findByText(/configuración de seguridad se ha guardado/i)).toBeInTheDocument()
        expect(cuerpo).toEqual({
            emailOtpEnabled: true,
            authenticatorEnabled: false,
        })
    })

    it('no permite activar Authenticator si no está configurado', async () => {
        await abrirSeguridad('cliente@esibuy.test', '/profile')

        expect(screen.getByRole('checkbox', { name: /microsoft authenticator/i })).toBeDisabled()
        expect(screen.getByText(/primero tienes que configurar y verificar/i)).toBeInTheDocument()
    })

    it('permite activar ambos factores cuando Authenticator está configurado', async () => {
        const user = userEvent.setup()
        const configuracion = {
            emailOtpEnabled: false,
            authenticatorEnabled: false,
            authenticatorConfigured: true,
            authenticatorVerifiedAt: '2026-11-11T10:00:00Z',
        }

        server.use(
            http.get('/api/seguridad/factores', () => HttpResponse.json(configuracion)),
            http.patch('/api/seguridad/factores', async ({ request }) => {
                const cuerpo = await request.json()
                return HttpResponse.json({ ...configuracion, ...cuerpo })
            }),
        )

        await abrirSeguridad('cliente@esibuy.test', '/profile')

        await user.click(screen.getByRole('checkbox', { name: /código de verificación por correo/i }))
        await user.click(screen.getByRole('checkbox', { name: /microsoft authenticator/i }))
        await user.click(screen.getByRole('button', { name: /guardar configuración/i }))

        expect(await screen.findByText(/configuración de seguridad se ha guardado/i)).toBeInTheDocument()
        expect(screen.getByRole('checkbox', { name: /código de verificación por correo/i })).toBeChecked()
        expect(screen.getByRole('checkbox', { name: /microsoft authenticator/i })).toBeChecked()
    })

    it('permite descartar cambios sin enviarlos', async () => {
        const user = userEvent.setup()
        let peticiones = 0

        server.use(
            http.patch('/api/seguridad/factores', () => {
                peticiones += 1
                return HttpResponse.json({})
            }),
        )

        await abrirSeguridad('cliente@esibuy.test', '/profile')

        const correo = screen.getByRole('checkbox', { name: /código de verificación por correo/i })
        await user.click(correo)
        await user.click(screen.getByRole('button', { name: /descartar/i }))

        expect(correo).not.toBeChecked()
        expect(peticiones).toBe(0)
    })

    it('muestra un error si falla la actualización', async () => {
        const user = userEvent.setup()

        server.use(
            http.patch('/api/seguridad/factores', () => new HttpResponse(null, { status: 500 })),
        )

        await abrirSeguridad('cliente@esibuy.test', '/profile')

        await user.click(screen.getByRole('checkbox', { name: /código de verificación por correo/i }))
        await user.click(screen.getByRole('button', { name: /guardar configuración/i }))

        expect(await screen.findByText(/no se ha podido guardar la configuración de seguridad/i)).toBeInTheDocument()
    })

    it('muestra la sección de seguridad en el perfil del vendedor', async () => {
        await abrirSeguridad('vendedor@esibuy.test', '/profile')

        expect(screen.getByRole('region', { name: /seguridad de la cuenta/i })).toBeInTheDocument()
    })

    it('muestra la sección de seguridad en el panel del administrador', async () => {
        await abrirSeguridad('admin@esibuy.test', '/admin')

        expect(screen.getByRole('region', { name: /seguridad de la cuenta/i })).toBeInTheDocument()
    })

    it('mantiene las configuraciones independientes entre usuarios', async () => {
        const user = userEvent.setup()

        await abrirSeguridad('cliente@esibuy.test', '/profile')
        await user.click(screen.getByRole('checkbox', { name: /código de verificación por correo/i }))
        await user.click(screen.getByRole('button', { name: /guardar configuración/i }))
        await screen.findByText(/configuración de seguridad se ha guardado/i)
        cleanup()
        await abrirSesionPorApi('vendedor@esibuy.test')
        renderApp('/profile')

        expect(await screen.findByRole('checkbox', { name: /código de verificación por correo/i })).not.toBeChecked()
    })
})
