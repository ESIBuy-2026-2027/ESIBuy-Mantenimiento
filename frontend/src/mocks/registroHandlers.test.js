import { describe, it, expect } from 'vitest'
import { registrarCliente, registrarVendedor } from '../api/auth'

// Como el backend real: un correo es único entre todos los roles, también si lo conoce otro mock (los
// listados de administración) o llega con otras mayúsculas o espacios.
const conflictoDeEmail = { status: 409, code: 'EMAIL_ALREADY_REGISTERED' }

describe('registroHandlers', () => {
  it('registroHandlers_clienteConElEmailDeUnVendedorDelPanel_responde409', async () => {
    await expect(registrarCliente({ email: 'marta@esibuy.test' })).rejects.toMatchObject(conflictoDeEmail)
  })

  it('registroHandlers_clienteConElEmailDeUnUsuarioDelListadoDeAdministracion_responde409', async () => {
    await expect(registrarCliente({ email: 'ana.lopez@esibuy.test' })).rejects.toMatchObject(conflictoDeEmail)
  })

  it('registroHandlers_vendedorConElEmailDeUnClienteEnMayusculasYConEspacios_responde409', async () => {
    await expect(registrarVendedor({ email: '  CLIENTE@esibuy.test ', nombreComercial: 'Tienda Nueva' }))
      .rejects.toMatchObject(conflictoDeEmail)
  })

  it('registroHandlers_conUnEmailNuevo_registraLaCuenta', async () => {
    await expect(registrarCliente({ email: 'alguien.nuevo@esibuy.test' })).resolves.toBeNull()
  })
})
