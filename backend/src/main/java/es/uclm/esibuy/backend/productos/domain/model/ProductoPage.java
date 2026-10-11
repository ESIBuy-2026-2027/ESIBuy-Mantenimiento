package es.uclm.esibuy.backend.productos.domain.model;

import java.util.List;

/**
 * Resultado paginado del dominio, sin exponer tipos de Spring Data (como UsuarioPage).
 */
public record ProductoPage(
        List<Producto> productos,
        int pagina,
        int tamano,
        long totalElementos
) {
}
