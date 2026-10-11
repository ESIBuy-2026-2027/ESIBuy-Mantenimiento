package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearAdministradorRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") String apellidos, 
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Formato de correo inválido") String email,
        String sede,   // Opcional, lo envía el front
        String avatar, // Opcional, lo envía el front
        @NotBlank(message = "La contraseña es obligatoria") String password
) {}