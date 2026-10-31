package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Adaptador de salida que pregunta al módulo de usuarios por su puerto de entrada, sin usar su
 * dominio (comunicación entre módulos del CLAUDE.md).
 */
public class UsuarioEnSesionAdapter implements UsuarioEnSesionPort {

    private final AutenticacionUseCase autenticacion;

    public UsuarioEnSesionAdapter(AutenticacionUseCase autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Override
    public String idPorCorreo(String email) {
        return autenticacion.consultarIdUsuarioEnSesion(email);
    }

    @Override
    public boolean esClientePremium(String email) {
        return autenticacion.esClientePremium(email);
    }
}
