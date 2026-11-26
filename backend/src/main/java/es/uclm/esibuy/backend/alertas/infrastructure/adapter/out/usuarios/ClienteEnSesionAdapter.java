package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/** Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio. */
public class ClienteEnSesionAdapter implements ClienteEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public ClienteEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }
}
