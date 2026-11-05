package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.List;

/**
 * Operaciones internas necesarias para eliminar una cuenta de vendedor.
 */
public interface GestionProductosVendedorUseCase {

    /**
     * Desactiva todos los productos del vendedor y devuelve sus identificadores.
     */
    List<String> desactivarTodos(String vendedorId);

    /**
     * Elimina físicamente un producto. Es idempotente para permitir reintentos.
     */
    void eliminar(String productoId);
}
