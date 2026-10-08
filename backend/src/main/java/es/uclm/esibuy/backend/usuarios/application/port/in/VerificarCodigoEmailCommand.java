package es.uclm.esibuy.backend.usuarios.application.port.in;

public record VerificarCodigoEmailCommand(String email, String codigo) {

}
