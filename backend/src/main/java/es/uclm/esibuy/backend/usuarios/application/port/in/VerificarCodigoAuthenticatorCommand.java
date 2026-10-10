package es.uclm.esibuy.backend.usuarios.application.port.in;

public record VerificarCodigoAuthenticatorCommand(String email, String codigo) {

    @Override
    public String toString() {
        return "VerificarCodigoAuthenticatorCommand[email=" + email + ", codigo=****]";
    }
}