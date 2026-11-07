package es.uclm.esibuy.backend.incidencias.application.port.in;

/** Lo que envía el usuario al reportar una incidencia: el asunto y la descripción. El autor sale de la sesión. */
public record CrearIncidenciaCommand(String email, String asunto, String descripcion) {
}