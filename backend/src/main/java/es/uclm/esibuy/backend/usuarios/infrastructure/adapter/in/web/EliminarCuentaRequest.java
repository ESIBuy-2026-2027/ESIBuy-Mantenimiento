package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

// Cuerpo de DELETE /api/perfil (HU25): la contraseña actual confirma la eliminación de la cuenta
public record EliminarCuentaRequest(@NotBlank String password) {

    // La contraseña nunca sale en los logs, como en LoginRequest
    @Override
    public String toString() {
        return "EliminarCuentaRequest[password=****]";
    }
}
