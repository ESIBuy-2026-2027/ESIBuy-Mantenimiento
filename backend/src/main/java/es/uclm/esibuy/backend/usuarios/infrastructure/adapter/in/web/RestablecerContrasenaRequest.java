package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Cuerpo de POST /api/auth/password-recovery/confirm (HU3)
public record RestablecerContrasenaRequest(
        @NotBlank @Size(max = 100) String email,
        @NotBlank String code,
        @NotBlank @Size(max = 64) String password) {

    // Ni el código ni la contraseña deben acabar en los logs (HU3-CP17)
    @Override
    public String toString() {
        return "RestablecerContrasenaRequest[email=" + email + ", code=****, password=****]";
    }
}
