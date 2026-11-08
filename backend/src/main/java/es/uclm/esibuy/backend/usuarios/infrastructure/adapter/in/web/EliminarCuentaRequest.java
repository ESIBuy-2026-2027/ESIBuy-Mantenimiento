package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

// Cuerpo de DELETE /api/perfil: la contraseña actual confirma la eliminación de la cuenta propia
public record EliminarCuentaRequest(@NotBlank(message = "La contraseña es obligatoria") String password) {

    // La contraseña nunca sale en los logs, como en LoginRequest
    @Override
    public String toString() {
        return "EliminarCuentaRequest[password=****]";
    }
}
