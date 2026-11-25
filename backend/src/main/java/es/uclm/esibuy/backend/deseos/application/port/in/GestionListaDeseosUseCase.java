package es.uclm.esibuy.backend.deseos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/**
 * Gestión de sus listas de deseos por parte del cliente (HU14, PBI 534). Recibe el correo del cliente en sesión:
 * solo puede ver y cambiar sus listas.
 */
public interface GestionListaDeseosUseCase {

    ListaDeseos anadir(String emailCliente, String productoId);

    ListaDeseosDetallada consultar(String emailCliente);

    ListaDeseosDetallada quitar(String emailCliente, String productoId);

    void eliminar(String emailCliente);

    List<ListaDeseosDetallada> consultarTodas(String emailCliente);

    ListaDeseosDetallada crearLista(String emailCliente, String nombre);

    ListaDeseosDetallada renombrarLista(String emailCliente, String listaId, String nombre);

    void eliminarLista(String emailCliente, String listaId);

    ListaDeseosDetallada anadirALista(String emailCliente, String listaId, String productoId);

    ListaDeseosDetallada quitarDeLista(String emailCliente, String listaId, String productoId);

    ListaDeseosDetallada actualizarCantidad(String emailCliente, String listaId, String productoId, int cantidad);
}
