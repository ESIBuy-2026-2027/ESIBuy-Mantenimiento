package es.uclm.esibuy.backend.usuarios.application.port.in;

public interface RecuperacionContrasenaUseCase {

    void solicitarRecuperacion(String email);

    void restablecerContrasena(RestablecerContrasenaCommand comando);
}
