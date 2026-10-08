package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

/**
 * Subdocumento {@code vendedor} de {@link UsuarioDocument}: datos propios del rol vendedor.
 * Solo lo tienen los vendedores (opción B del «Planteamiento inicial BBDD»: datos de rol embebidos).
 */
public record DatosVendedorDocument(String nombreComercial, String categoriaPrincipalId) {
}
