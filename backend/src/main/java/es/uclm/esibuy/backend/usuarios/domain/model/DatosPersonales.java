package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

/**
 * Datos personales de un usuario. El DNI y el teléfono los tienen clientes y vendedores, no los
 * administradores (MVP del Sprint 1).
 *
 * <p>Todos son opcionales mientras el registro solo pida email y contraseña, pero si se informan
 * no pueden estar en blanco.
 */
public record DatosPersonales(String nombre, String apellidos, String dni, String telefono) {

    private static final DatosPersonales SIN_DATOS = new DatosPersonales(null, null, null, null);

    public DatosPersonales {
        exigirTextoSiExiste(nombre, "El nombre no puede estar en blanco");
        exigirTextoSiExiste(apellidos, "Los apellidos no pueden estar en blanco");
        exigirTextoSiExiste(dni, "El DNI no puede estar en blanco");
        exigirTextoSiExiste(telefono, "El teléfono no puede estar en blanco");
    }

    public static DatosPersonales sinDatos() {
        return SIN_DATOS;
    }

    private static void exigirTextoSiExiste(String valor, String mensaje) {
        if (valor != null && valor.isBlank()) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }
}
