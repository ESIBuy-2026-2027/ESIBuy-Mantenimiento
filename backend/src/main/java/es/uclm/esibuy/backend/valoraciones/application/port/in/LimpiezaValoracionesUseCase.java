package es.uclm.esibuy.backend.valoraciones.application.port.in;

/**
 * Limpieza interna de valoraciones asociadas a productos eliminados.
 */
public interface LimpiezaValoracionesUseCase {

    void eliminarDeProducto(String productoId);
}
