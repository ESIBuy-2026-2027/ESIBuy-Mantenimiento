package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

/**
 * Datos propios de un cliente. La fecha de nacimiento es opcional hasta que el registro la pida.
 */
public record DatosCliente(TipoCliente tipoCliente, LocalDate fechaNacimiento) implements DatosRol {

    public DatosCliente {
        if (tipoCliente == null) {
            throw new UsuarioInvalidoException("Un cliente debe tener tipo de cliente");
        }
    }

    @Override
    public Rol rol() {
        return Rol.CLIENTE;
    }
}
