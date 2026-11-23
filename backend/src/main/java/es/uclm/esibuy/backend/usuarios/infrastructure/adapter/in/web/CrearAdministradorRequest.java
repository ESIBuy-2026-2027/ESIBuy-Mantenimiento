package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearAdministradorRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(min = 2, max = 30) @Pattern(regexp = "^\\p{L}[\\p{L}\\p{M}]*(?:[ '-][\\p{L}\\p{M}]+)*$") String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(min = 2, max = 30) @Pattern(regexp = "^\\p{L}[\\p{L}\\p{M}]*(?:[ '-][\\p{L}\\p{M}]+)*$") String apellidos, 
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") String email,
        String sede,   // Opcional, lo envía el front; aún no se guarda
        String avatar, // Opcional: se guarda como imagen de perfil
        @NotBlank(message = "La contraseña es obligatoria") String password
) {}