package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Subdocumento {@code vendedor} de {@link UsuarioDocument}: datos propios del rol vendedor.
 * Solo lo tienen los vendedores (opción B del «Planteamiento inicial BBDD»: datos de rol embebidos).
 */
public record DatosVendedorDocument(
        // «Planteamiento inicial BBDD»: nombreComercial → único. Spring crea el índice
        // "vendedor.nombreComercial"; sparse para que no cuenten los usuarios que no son vendedores.
        @Indexed(unique = true, sparse = true) String nombreComercial,
        String categoriaPrincipalId) {
}
