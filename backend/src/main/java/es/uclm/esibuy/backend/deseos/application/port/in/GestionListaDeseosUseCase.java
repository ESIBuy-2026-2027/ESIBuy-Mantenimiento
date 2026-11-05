package es.uclm.esibuy.backend.deseos.application.port.in;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/**
 * Gestión de su lista de deseos por parte del cliente (HU14). Recibe el correo del cliente en sesión: solo puede
 * ver y cambiar su lista.
 */
public interface GestionListaDeseosUseCase {

    /**
     * Añade un producto a la lista del cliente, y la crea si todavía no tiene. Añadir un producto que ya está no
     * es un error: la lista no cambia.
     *
     * @return la lista guardada
     * @throws es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException si no existe o no
     *         está disponible
     */
    ListaDeseos anadir(String emailCliente, String productoId);

    /**
     * La lista del cliente con los datos actuales de sus productos. Un producto desactivado sigue en ella, marcado
     * como no disponible; uno eliminado desaparece y la lista se guarda sin él. Si no tiene lista, una vacía.
     */
    ListaDeseosDetallada consultar(String emailCliente);

    /**
     * Quita un producto de la lista del cliente, esté o no disponible. Quitar un producto que no está no es un
     * error: la lista no cambia y, si el cliente no tiene lista, no se crea.
     *
     * @return la lista actualizada, como en {@link #consultar}
     */
    ListaDeseosDetallada quitar(String emailCliente, String productoId);
}
