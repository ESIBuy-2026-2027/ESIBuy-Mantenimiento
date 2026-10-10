package es.uclm.esibuy.backend.usuarios.application.port.in;

public interface AuthenticatorUseCase {
    AltaAuthenticator prepararAlta(String email);
}
