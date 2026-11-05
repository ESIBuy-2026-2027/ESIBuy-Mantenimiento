package es.uclm.esibuy.backend.usuarios.domain.exception;

public class ContrasenaInvalidaException extends RuntimeException {

    private final MotivoRechazo motivo;

    public ContrasenaInvalidaException(MotivoRechazo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    // Sin motivo concreto: un dato inválido genérico (usos anteriores a los códigos de la demo del Sprint 1).
    public ContrasenaInvalidaException(String mensaje) {
        this(MotivoRechazo.DATO_INVALIDO, mensaje);
    }

    public MotivoRechazo getMotivo() {
        return motivo;
    }
}
