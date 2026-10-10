package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición de registro de un cliente (contrato acordado con el frontend, T229).
 *
 * <p>Aquí solo se comprueba la forma de la entrada: que los campos obligatorios vengan, que el
 * email tenga formato y las longitudes máximas del contrato (30 nombre y apellidos, 100 email, 64 contraseña). Las reglas de negocio (DNI, teléfono, mayoría de edad, política de
 * contraseñas...) las aplica el dominio a través del servicio.
 */
public record RegistroRequest(
        @NotBlank @Size(max = 30) String nombre,
        @NotBlank @Size(max = 30) String apellidos,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String dni,
        @NotBlank @Email @Size(max = 100) String email,
        String telefono,
        @NotBlank @Size(max = 64) String password,
        @NotBlank @Size(max = 64) String repeticionPassword,
        @NotNull TipoCliente tipoCliente,
        String imagenPerfil,
        boolean aceptaPrivacidad) {
}
