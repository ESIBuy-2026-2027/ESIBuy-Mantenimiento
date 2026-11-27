package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.application.port.in.UsuarioResumen;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * DTO de salida del listado administrativo. No expone el hash de contraseña ni otros datos sensibles.
 */
public record UsuarioListadoResponse(
        String id,
        String nombre,
        String apellidos,
        String email,
        String rol,
        String estado,
        Instant fechaAlta
) {

    static UsuarioListadoResponse desde(Usuario usuario) {
        return new UsuarioListadoResponse(
                usuario.getId(),
                usuario.getDatosPersonales().nombre(),
                usuario.getDatosPersonales().apellidos(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                usuario.getEstado(),
                usuario.getFechaAlta()
        );
    }

    static UsuarioListadoResponse desde(UsuarioResumen usuario) {
        return new UsuarioListadoResponse(
                usuario.id(),
                usuario.nombre(),
                usuario.apellidos(),
                usuario.email(),
                usuario.rol(),
                usuario.estado(),
                usuario.fechaAlta()
        );
    }
}
