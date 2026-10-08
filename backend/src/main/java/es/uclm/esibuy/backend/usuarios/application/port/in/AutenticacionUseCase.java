package es.uclm.esibuy.backend.usuarios.application.port.in;

public interface AutenticacionUseCase {
    ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando);
    ResultadoInicioSesion verificarCodigoEmail(VerificarCodigoEmailCommand comando);
}
