package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio. Que sea un
 * cliente lo garantiza el RBAC del servicio.
 */
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
