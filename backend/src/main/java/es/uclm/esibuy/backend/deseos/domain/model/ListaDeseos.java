package es.uclm.esibuy.backend.deseos.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // Añadir un producto que ya está no es un error: la lista ya queda como se quería y conserva su fecha
    public void anadir(String productoId, Instant ahora) {
        boolean yaEsta = items.stream().anyMatch(item -> item.productoId().equals(productoId));
        if (!yaEsta) {
            items.add(new ItemDeseo(productoId, ahora));
        }
    }

    // Quitar un producto que no está no es un error: la lista ya queda como se quería. Dice si ha cambiado
    public boolean quitar(String productoId) {
        return items.removeIf(item -> item.productoId().equals(productoId));
    }

    /**
     * Quita los productos que ya no existen: el borrado de un producto es físico y la lista no guarda sus datos.
     *
     * @param productosExistentes id de los productos de la lista que siguen existiendo
     * @return si la lista ha cambiado y hay que guardarla
     */
    public boolean conservarSolo(Set<String> productosExistentes) {
        return items.removeIf(item -> !productosExistentes.contains(item.productoId()));
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
