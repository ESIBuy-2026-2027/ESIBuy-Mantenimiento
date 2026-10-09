package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import java.time.LocalDate;

public record DatosCliente (TipoCliente tipoCliente, Dni dni, LocalDate fechaNacimiento, Telefono telefono) {
    public DatosCliente {
        exigirNoNulo(tipoCliente, "El tipo de cliente es obligatorio");
        exigirNoNulo(dni, "El DNI es obligatorio");
        exigirNoNulo(fechaNacimiento, "La fecha de nacimiento es obligatoria");
    }

    private static void exigirNoNulo(Object valor, String mensaje) {
        if (valor == null) {
            throw new UsuarioInvalidoException(mensaje); 
        }
    }
}
