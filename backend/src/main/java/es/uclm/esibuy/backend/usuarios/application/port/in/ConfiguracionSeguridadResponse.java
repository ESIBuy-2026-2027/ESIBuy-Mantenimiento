
package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.time.Instant;

public record ConfiguracionSeguridadResponse(
        boolean emailOtpEnabled,
        boolean authenticatorEnabled,
        boolean authenticatorConfigured,
        Instant authenticatorVerifiedAt) {
}
