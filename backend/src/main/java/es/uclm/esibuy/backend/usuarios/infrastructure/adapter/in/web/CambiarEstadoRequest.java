package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para cambiar el estado de una cuenta ({@code PATCH /api/admin/usuarios/{id}/status}).
 *
 * <p>Se admiten {@code ACTIVO} (activar, HU24) y {@code DESACTIVADO} (desactivar, HU23);
 * cualquier otro estado es un 400.
 */
public record CambiarEstadoRequest(
        @NotBlank
        @Pattern(regexp = "ACTIVO|DESACTIVADO", message = "Estado no admitido")
        String estado) {
}
