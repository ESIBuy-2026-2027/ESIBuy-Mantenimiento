package es.uclm.esibuy.backend.usuarios.application.port.in;

public record CrearAdministradorCommand(
        String nombre,
        String apellidos,
        String email,
        String password
) {}