package es.uclm.esibuy.backend.usuarios.application.port.in;

/**
 * El correo y el rol de un usuario, para que otros módulos (la supervisión de incidencias, HU49) no vean el dominio
 * de usuarios. El rol va como texto ({@code "CLIENTE"}, {@code "VENDEDOR"}...) y no como el enum {@code Rol}.
 */
public record ContactoUsuario(String email, String rol) {
}
