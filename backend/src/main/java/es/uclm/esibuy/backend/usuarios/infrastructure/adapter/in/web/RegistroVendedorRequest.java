
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/auth/register/vendedor} (T241), con los campos del
 * formulario del frontend.
 * Como el del cliente, solo valida forma y presencia; las reglas de negocio
 * (DNI, teléfono, contraseña,
 * duplicados) las aplica el dominio. Sin fecha de nacimiento ni tipo de
 * cliente; una única categoría principal.
 */
public record RegistroVendedorRequest(
                @NotBlank @Size(min = 2, max = 30) @Pattern(regexp = FORMATO_NOMBRE) String nombre,

                @NotBlank @Size(min = 2, max = 30) @Pattern(regexp = FORMATO_NOMBRE) String apellidos,

                @NotBlank @Size(max = 30) String nombreComercial,
                @NotBlank String categoriaPrincipalId,
                @NotBlank String dni,
                @NotBlank @Email @Size(max = 100) String email,
                String telefono,
                @NotBlank @Size(max = 64) String password,
                @NotBlank @Size(max = 64) String repeticionPassword,
                String avatar,
                boolean aceptaPrivacidad) {

        private static final String FORMATO_NOMBRE = "^\\p{L}[\\p{L}\\p{M}]*+(?:[ '-][\\p{L}\\p{M}]++)*+$";
}
