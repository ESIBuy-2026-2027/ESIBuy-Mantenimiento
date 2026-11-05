package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edición de un usuario por el administrador ({@code PUT /api/admin/usuarios/{id}}, HU22, T42).
 * Sigue el contrato de {@code frontend/docs/hu22-editar-usuario.md}: la imagen se llama {@code avatar}.
 *
 * <p>Aquí solo se comprueba la forma de la entrada común a todos los roles; los datos de cliente y de vendedor
 * (DNI, teléfono, mayoría de edad, nombre comercial...) los valida el dominio a través del servicio, porque
 * dependen del rol.
 * Los campos que no están aquí (email, contraseña, estado, rol...) se ignoran aunque lleguen en el cuerpo:
 * el rol de una cuenta no cambia nunca, así que los datos se validan según el rol que ya tiene.
 *
 * <p>PENDIENTE: {@code sede} del administrador, cuando exista en el modelo (decisión del equipo: este sprint no).
 */
public record EditarUsuarioRequest(
        @NotBlank @Size(max = 30) String nombre,
        @NotBlank @Size(max = 30) String apellidos,
        String avatar,
        String dni,
        String telefono,
        LocalDate fechaNacimiento,
        TipoCliente tipoCliente,
        String nombreComercial,
        String categoriaPrincipalId) {

    EditarUsuarioCommand aComando() {
        return new EditarUsuarioCommand(nombre, apellidos, avatar, dni, telefono, fechaNacimiento, tipoCliente, nombreComercial,
                categoriaPrincipalId);
    }
}
