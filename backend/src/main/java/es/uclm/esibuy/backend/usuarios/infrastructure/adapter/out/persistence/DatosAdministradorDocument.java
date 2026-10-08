package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

/**
 * Subdocumento {@code administrador} de {@link UsuarioDocument}: datos propios del rol administrador.
 * Solo lo tienen los administradores (opción B del «Planteamiento inicial BBDD»: datos de rol embebidos).
 */
public record DatosAdministradorDocument(String sede, LocalDate fechaIncorporacion) {
}
