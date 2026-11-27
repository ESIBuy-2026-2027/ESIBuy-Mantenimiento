package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.Instant;

public record UsuarioResumen(
        String id,
        String nombre,
        String apellidos,
        String email,
        String rol,
        String estado,
        Instant fechaAlta
) {
}
