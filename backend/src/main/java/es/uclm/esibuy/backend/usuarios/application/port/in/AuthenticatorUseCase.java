package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public interface AuthenticatorUseCase {
    AltaAuthenticator prepararAlta(String email);
    Usuario verificarCodigo(VerificarCodigoAuthenticatorCommand comando);
}
