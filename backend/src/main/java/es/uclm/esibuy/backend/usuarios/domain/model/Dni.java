package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record Dni(String valor) {
    public Dni {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException(MotivoRechazo.DNI_INVALIDO, "El DNI es obligatorio");
        }
    }
}
