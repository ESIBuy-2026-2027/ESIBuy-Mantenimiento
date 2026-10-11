package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public interface AutenticacionUseCase {
    ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando);
    ResultadoInicioSesion verificarCodigoEmail(VerificarCodigoEmailCommand comando);
    Usuario consultarUsuarioEnSesion(String email);

    // Solo el id del usuario en sesión, para otros módulos (p. ej. productos, HU32): así no ven el dominio de usuarios.
    String consultarIdUsuarioEnSesion(String email);
}
