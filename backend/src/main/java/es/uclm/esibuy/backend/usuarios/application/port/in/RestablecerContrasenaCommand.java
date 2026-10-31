package es.uclm.esibuy.backend.usuarios.application.port.in;

public record RestablecerContrasenaCommand(String email, String codigo, String password) {

    // Ni el código ni la contraseña deben acabar en los logs
    @Override
    public String toString() {
        return "RestablecerContrasenaCommand[email=" + email + ", codigo=****, password=****]";
    }
}
