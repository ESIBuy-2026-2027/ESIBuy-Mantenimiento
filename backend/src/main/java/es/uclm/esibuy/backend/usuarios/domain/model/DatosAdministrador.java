package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

/**
 * Datos propios de un administrador: la sede a la que se vincula (opcional) y su fecha de
 * incorporación a la compañía (obligatoria; el MVP del Sprint 1 la marca como no editable).
 */
public record DatosAdministrador(String sede, LocalDate fechaIncorporacion) implements DatosRol {

    public DatosAdministrador {
        if (sede != null && sede.isBlank()) {
            throw new UsuarioInvalidoException("La sede no puede estar en blanco");
        }
        if (fechaIncorporacion == null) {
            throw new UsuarioInvalidoException("Un administrador debe tener fecha de incorporación");
        }
    }

    @Override
    public Rol rol() {
        return Rol.ADMINISTRADOR;
    }
}
