package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.EditarPerfilCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edición del perfil propio ({@code PUT /api/perfil}, HU45, T101). Se envían todos los datos
 * modificables del rol: el PUT los sustituye todos. La imagen se llama {@code avatar}, como en
 * {@link PerfilResponse} y en la edición del administrador ({@link EditarUsuarioRequest}).
 *
 * <p>Aquí solo se comprueba la forma de lo común a los dos roles. Lo que depende del rol (teléfono, mayoría de edad
 * del cliente, nombre comercial del vendedor...) lo valida el dominio: el cliente usa {@code fechaNacimiento} y el
 * vendedor, {@code nombreComercial} y {@code categoriaPrincipalId}. El email, el DNI, el tipo de cliente, el rol y
 * la contraseña no se editan: no están aquí y se ignoran aunque lleguen en el cuerpo.
 */
public record EditarPerfilRequest(
        @NotBlank @Size(max = 30) String nombre,
        @NotBlank @Size(max = 30) String apellidos,
        String avatar,
        String telefono,
        LocalDate fechaNacimiento,
        String nombreComercial,
        String categoriaPrincipalId) {

    EditarPerfilCommand aComando() {
        return new EditarPerfilCommand(nombre, apellidos, avatar, telefono, fechaNacimiento, nombreComercial,
                categoriaPrincipalId);
    }
}
