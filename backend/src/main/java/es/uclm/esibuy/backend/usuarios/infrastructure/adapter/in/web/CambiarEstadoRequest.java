package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para cambiar el estado de una cuenta ({@code PATCH /api/admin/usuarios/{id}/status}).
 *
 * <p>De momento solo se admite {@code ACTIVO} (activar, HU24); cualquier otro estado es un 400.
 * La HU23 añadirá {@code DESACTIVADO} con sus reglas.
 */
public record CambiarEstadoRequest(
        @NotBlank
        @Pattern(regexp = "ACTIVO", message = "Estado no admitido")
        String estado) {
}
