package es.uclm.esibuy.backend.usuarios.application.port.in;

public record IniciarSesionCommand(String email, String password) {

    @Override
    public String toString() {
        return "IniciarSesionCommand[email=" + email + ", password=****]";
    }

}
