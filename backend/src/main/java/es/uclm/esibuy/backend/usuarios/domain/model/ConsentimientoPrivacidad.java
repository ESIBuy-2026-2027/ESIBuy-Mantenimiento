package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Instant;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record ConsentimientoPrivacidad(Instant aceptadaEn, String version) {

    public ConsentimientoPrivacidad {
        if (aceptadaEn == null) {
            throw new UsuarioInvalidoException("La fecha de aceptación de la política de privacidad es obligatoria");
        }
        if (version == null || version.isBlank()) {
            throw new UsuarioInvalidoException("La versión de la política de privacidad es obligatoria");
        }
    }
}
