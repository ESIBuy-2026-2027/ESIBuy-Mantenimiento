package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo JSON de la petición de registro.
 *
 * <p>Los nombres de los campos son los del contrato con el frontend ({@code email} y
 * {@code password}). El controlador los traduce a {@code RegistrarUsuarioCommand}, que usa
 * los términos del dominio ({@code correo} y {@code contrasena}).
 */
public record RegistroRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
