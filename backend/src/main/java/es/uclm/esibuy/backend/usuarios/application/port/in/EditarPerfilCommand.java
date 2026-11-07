package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.LocalDate;

/**
 * Datos de entrada de la edición del perfil propio por un cliente o un vendedor (HU45, T298).
 *
 * <p>Solo lleva los datos modificables. Todos los usuarios editan {@code nombre}, {@code apellidos},
 * {@code imagenPerfil} y {@code telefono}. El cliente edita además {@code fechaNacimiento}, y el vendedor
 * {@code nombreComercial} y {@code categoriaPrincipalId}. Los campos del otro rol se ignoran.
 *
 * <p>No lleva email, DNI, tipo de cliente, rol, contraseña ni estado: no se cambian desde el perfil propio
 * (decisión del equipo, 05/11/2026). El servicio conserva los que tiene guardados el usuario.
 */
public record EditarPerfilCommand(
        String nombre,
        String apellidos,
        String imagenPerfil,
        String telefono,
        LocalDate fechaNacimiento,
        String nombreComercial,
        String categoriaPrincipalId) {
}
