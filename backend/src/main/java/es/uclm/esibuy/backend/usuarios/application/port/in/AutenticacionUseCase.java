package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public interface AutenticacionUseCase {
    ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando);
    ResultadoInicioSesion verificarCodigoEmail(VerificarCodigoEmailCommand comando);
    Usuario consultarUsuarioEnSesion(String email);
}
