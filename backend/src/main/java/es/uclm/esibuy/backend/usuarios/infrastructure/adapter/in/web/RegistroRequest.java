package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de la petición de registro de un cliente (contrato acordado con el frontend, T229).
 *
 * <p>Aquí solo se comprueba la forma de la entrada: que los campos obligatorios vengan y que el
 * email tenga formato. Las reglas de negocio (DNI, teléfono, mayoría de edad, política de
 * contraseñas...) las aplica el dominio a través del servicio.
 */
public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String dni,
        @NotBlank @Email String email,
        String telefono,
        @NotBlank String password,
        @NotBlank String repeticionPassword,
        @NotNull TipoCliente tipoCliente,
        String imagenPerfil,
        boolean aceptaPrivacidad) {
}
