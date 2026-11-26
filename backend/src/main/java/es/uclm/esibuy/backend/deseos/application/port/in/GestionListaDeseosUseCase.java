package es.uclm.esibuy.backend.deseos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/**
 * Gestión de sus productos favoritos (HU14, AB#528) y de sus listas de deseos (PBI 534) por parte del cliente. Van por
 * separado: un producto puede estar en los favoritos y en varias listas a la vez. Recibe el correo del cliente en
 * sesión: solo puede ver y cambiar lo suyo.
 */
public interface GestionListaDeseosUseCase {

    /** Añade un producto a los favoritos, y los crea si todavía no tiene. Uno que ya está no es un error. */
    ListaDeseos anadir(String emailCliente, String productoId);

    /** Los favoritos con los datos actuales de sus productos; si no tiene, vacíos. */
    ListaDeseosDetallada consultar(String emailCliente);

    /** Quita un producto de los favoritos. Uno que no está no es un error. */
    ListaDeseosDetallada quitar(String emailCliente, String productoId);

    /** Borra los favoritos y todas las listas del cliente, para cuando elimina su cuenta (HU25). */
    void eliminar(String emailCliente);

    /** Las listas de deseos, sin los favoritos. Si no tiene ninguna, se crea una por defecto. */
    List<ListaDeseosDetallada> consultarTodas(String emailCliente);

    ListaDeseosDetallada crearLista(String emailCliente, String nombre);

    ListaDeseosDetallada renombrarLista(String emailCliente, String listaId, String nombre);

    /** Elimina una lista; si era la última, se crea otra por defecto. */
    void eliminarLista(String emailCliente, String listaId);

    ListaDeseosDetallada anadirALista(String emailCliente, String listaId, String productoId);

    ListaDeseosDetallada quitarDeLista(String emailCliente, String listaId, String productoId);

    ListaDeseosDetallada actualizarCantidad(String emailCliente, String listaId, String productoId, int cantidad);
}
