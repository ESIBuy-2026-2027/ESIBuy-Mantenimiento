package es.uclm.esibuy.backend.deseos.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Lista de deseos de un cliente (HU14): una por cliente (decisión del equipo), con cada producto una sola vez. */
public class ListaDeseos {

    private final String id;
    private final String clienteId;
    private final List<ItemDeseo> items;

    public ListaDeseos(String id, String clienteId, List<ItemDeseo> items) {
        this.id = id;
        this.clienteId = clienteId;
        this.items = new ArrayList<>(items);
    }

    // Lista aún no guardada, para un cliente que todavía no tiene ninguna
    public static ListaDeseos vacia(String clienteId) {
        return new ListaDeseos(null, clienteId, List.of());
    }

    public void anadir(String productoId, Instant ahora) {
        // Se implementa en el ciclo verde
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<ItemDeseo> getItems() {
        return List.copyOf(items);
    }
}
