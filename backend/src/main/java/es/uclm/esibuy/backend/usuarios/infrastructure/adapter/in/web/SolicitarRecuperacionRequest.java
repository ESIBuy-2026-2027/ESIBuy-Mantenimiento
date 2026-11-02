package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Cuerpo de POST /api/auth/password-recovery (HU3)
public record SolicitarRecuperacionRequest(@NotBlank @Size(max = 100) String email) {
}
