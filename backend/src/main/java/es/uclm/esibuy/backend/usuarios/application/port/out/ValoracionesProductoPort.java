package es.uclm.esibuy.backend.usuarios.application.port.out;

/** Lo que usuarios necesita del módulo de valoraciones al eliminar productos. */
public interface ValoracionesProductoPort {

    void eliminarDeProducto(String productoId);
}
