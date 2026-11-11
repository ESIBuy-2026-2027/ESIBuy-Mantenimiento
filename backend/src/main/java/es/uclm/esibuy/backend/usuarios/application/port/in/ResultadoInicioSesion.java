package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public record ResultadoInicioSesion(
        String email,
        PasoLogin siguientePaso,
        Usuario usuario) {

    public ResultadoInicioSesion(String email, PasoLogin siguientePaso) {
        this(email, siguientePaso, null);
    }
}