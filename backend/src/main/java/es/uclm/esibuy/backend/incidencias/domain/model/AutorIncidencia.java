package es.uclm.esibuy.backend.incidencias.domain.model;

/**
 * Quien reportó la incidencia, tal como lo ve el administrador (HU49): su correo y su rol como texto
 * ({@code "CLIENTE"}, {@code "VENDEDOR"}), sin depender del dominio de usuarios.
 */
public record AutorIncidencia(String email, String rol) {
}
