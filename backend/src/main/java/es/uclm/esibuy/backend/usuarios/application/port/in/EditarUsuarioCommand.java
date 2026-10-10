package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;

/**
 * Datos de entrada del caso de uso de edición de un usuario por el administrador (HU22, T42).
 *
 * <p>Como {@link RegistrarUsuarioCommand}, lleva texto plano y es el servicio quien construye los value
 * objects del dominio. Trae los campos de los tres roles; solo se usan los del rol del usuario
 * ({@code dni}, {@code telefono}, {@code fechaNacimiento} y {@code tipoCliente} para un cliente;
 * {@code email} para un vendedor).
 *
 * <p>No lleva contraseña, estado ni fecha de incorporación: no se pueden cambiar por esta vía.
 *
 * <p>PENDIENTE: la sede del administrador y el nombre comercial, el NIF y la categoría del vendedor,
 * cuando existan en el modelo (ver {@code UsuarioDetalleResponse} y {@code VendedorResponse}).
 */
public record EditarUsuarioCommand(
        Rol rol,
        String nombre,
        String apellidos,
        String imagenPerfil,
        String email,
        String dni,
        String telefono,
        LocalDate fechaNacimiento,
        TipoCliente tipoCliente) {
}
