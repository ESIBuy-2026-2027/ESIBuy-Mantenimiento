package es.uclm.esibuy.backend.usuarios.application.port.out;

public interface EnvioEmailPort {
    void enviarCodigoVerificacion(String email, String codigo);

    void enviarCodigoRecuperacion(String email, String codigo);
}
