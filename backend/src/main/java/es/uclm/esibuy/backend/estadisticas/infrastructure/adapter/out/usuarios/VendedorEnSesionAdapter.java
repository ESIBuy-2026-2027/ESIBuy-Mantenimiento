package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su dominio. Que sea un
 * vendedor lo garantiza el RBAC del endpoint.
 */
public class VendedorEnSesionAdapter implements VendedorEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public VendedorEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }
}
