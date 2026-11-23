
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearAdministradorRequest(
                @NotBlank(message = "El nombre es obligatorio") @Size(min = 2, max = 30) @Pattern(regexp = FORMATO_NOMBRE) String nombre,

                @NotBlank(message = "Los apellidos son obligatorios") @Size(min = 2, max = 30) @Pattern(regexp = FORMATO_NOMBRE) String apellidos,

                @NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") String email,

                String sede,
                String avatar,

                @NotBlank(message = "La contraseña es obligatoria") String password) {

        private static final String FORMATO_NOMBRE = "^\\p{L}[\\p{L}\\p{M}]*+(?:[ '-][\\p{L}\\p{M}]++)*+$";
}
