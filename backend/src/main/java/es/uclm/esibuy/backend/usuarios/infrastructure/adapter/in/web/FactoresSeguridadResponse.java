
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.application.port.in.ConfiguracionSeguridadResponse;

public record FactoresSeguridadResponse(
        boolean emailOtpEnabled,
        boolean authenticatorEnabled,
        boolean authenticatorConfigured,
        Instant authenticatorVerifiedAt) {

    public static FactoresSeguridadResponse desde(ConfiguracionSeguridadResponse configuracion) {
        return new FactoresSeguridadResponse(
                configuracion.emailOtpEnabled(),
                configuracion.authenticatorEnabled(),
                configuracion.authenticatorConfigured(),
                configuracion.authenticatorVerifiedAt());
    }
}
