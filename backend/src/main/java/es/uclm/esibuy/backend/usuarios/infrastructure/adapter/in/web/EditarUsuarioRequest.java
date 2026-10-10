package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edición de un usuario por el administrador ({@code PUT /api/admin/usuarios/{id}}, HU22, T42).
 * Sigue el contrato de {@code frontend/docs/hu22-editar-usuario.md}: la imagen se llama {@code avatar}.
 *
 * <p>Aquí solo se comprueba la forma de la entrada común a todos los roles; los datos de cliente (DNI,
 * teléfono, mayoría de edad...) los valida el dominio a través del servicio, porque dependen del rol.
 * Los campos que no están aquí (email, contraseña, estado...) se ignoran aunque lleguen en el cuerpo.
 *
 * <p>PENDIENTE: {@code sede} del administrador y {@code nombreComercial}, {@code nif} y
 * {@code categoriaPrincipalId} del vendedor, cuando existan en el modelo. {@code fechaIncorporacion} del
 * administrador, cuando exista, NO se añade aquí: es de solo lectura (decisión del equipo).
 */
public record EditarUsuarioRequest(
        @NotNull Rol rol,
        @NotBlank @Size(max = 30) String nombre,
        @NotBlank @Size(max = 30) String apellidos,
        String avatar,
        String dni,
        String telefono,
        LocalDate fechaNacimiento,
        TipoCliente tipoCliente) {

    EditarUsuarioCommand aComando() {
        return new EditarUsuarioCommand(rol, nombre, apellidos, avatar, dni, telefono, fechaNacimiento, tipoCliente);
    }
}
