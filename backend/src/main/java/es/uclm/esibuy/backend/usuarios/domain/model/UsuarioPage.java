package es.uclm.esibuy.backend.usuarios.domain.model;

import java.util.List;

/**
 * Resultado paginado del dominio, sin exponer tipos de Spring Data.
 */
public record UsuarioPage(
        List<Usuario> usuarios,
        int pagina,
        int tamano,
        long totalElementos
) {
}
