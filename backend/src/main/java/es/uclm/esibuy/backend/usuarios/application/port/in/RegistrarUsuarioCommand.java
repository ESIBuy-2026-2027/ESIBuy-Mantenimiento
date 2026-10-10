package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;

/**
 * Datos de entrada del caso de uso de registro de un cliente (T229).
 *
 * <p>Es un record porque solo transporta datos del adaptador web al servicio. Lleva texto plano
 * (DNI, teléfono...) y no los value objects del dominio: es el servicio quien los construye y, con
 * ellos, quien aplica las reglas. {@code fechaNacimiento} y {@code tipoCliente} sí llegan tipados
 * porque el adaptador web ya los ha convertido desde el JSON.
 *
 * <p>El {@code toString} oculta las dos contraseñas para que no acaben en un log.
 */
public record RegistrarUsuarioCommand(
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String dni,
        String email,
        String telefono,
        String password,
        String repeticionPassword,
        TipoCliente tipoCliente,
        String imagenPerfil,
        boolean aceptaPrivacidad) {

    @Override
    public String toString() {
        return "RegistrarUsuarioCommand[nombre=" + nombre + ", apellidos=" + apellidos
                + ", fechaNacimiento=" + fechaNacimiento + ", dni=" + dni + ", email=" + email
                + ", telefono=" + telefono + ", password=****, repeticionPassword=****"
                + ", tipoCliente=" + tipoCliente + ", imagenPerfil=" + imagenPerfil
                + ", aceptaPrivacidad=" + aceptaPrivacidad + "]";
    }
}
