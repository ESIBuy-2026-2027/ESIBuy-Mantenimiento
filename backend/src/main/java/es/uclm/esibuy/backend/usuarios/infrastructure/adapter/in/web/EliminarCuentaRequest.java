package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record EliminarCuentaRequest(@NotBlank(message = "La contraseña es obligatoria") String password) {
}
