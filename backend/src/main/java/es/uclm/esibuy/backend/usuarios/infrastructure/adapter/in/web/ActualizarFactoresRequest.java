
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.usuarios.application.port.in.ActualizarFactoresCommand;
import jakarta.validation.constraints.NotNull;

public record ActualizarFactoresRequest(
        @NotNull Boolean emailOtpEnabled,
        @NotNull Boolean authenticatorEnabled) {

    public ActualizarFactoresCommand aComando() {
        return new ActualizarFactoresCommand(
                emailOtpEnabled,
                authenticatorEnabled);
    }
}
