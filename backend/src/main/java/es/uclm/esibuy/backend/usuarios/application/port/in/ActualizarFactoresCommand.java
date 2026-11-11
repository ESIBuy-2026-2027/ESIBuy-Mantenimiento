package es.uclm.esibuy.backend.usuarios.application.port.in;

public record ActualizarFactoresCommand(
        boolean emailOtpEnabled,
        boolean authenticatorEnabled) {
}
