package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio. Que sea un
 * cliente o un vendedor lo garantiza el RBAC del servicio.
 */
public class AutorEnSesionAdapter implements AutorEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public AutorEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }
}