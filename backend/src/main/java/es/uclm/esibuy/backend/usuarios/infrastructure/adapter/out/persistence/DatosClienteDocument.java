package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

/**
 * Subdocumento {@code cliente} de {@link UsuarioDocument}: datos propios del rol cliente.
 * Solo lo tienen los clientes (opción B del «Planteamiento inicial BBDD»: datos de rol embebidos).
 */
public record DatosClienteDocument(String tipoCliente, LocalDate fechaNacimiento) {
}
