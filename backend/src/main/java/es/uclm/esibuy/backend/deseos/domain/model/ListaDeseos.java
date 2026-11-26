package es.uclm.esibuy.backend.deseos.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.uclm.esibuy.backend.deseos.domain.exception.ListaDeseosInvalidaException;

/**
 * Lista de deseos de un cliente (HU14, PBI 534): un cliente puede tener una o varias listas con nombre, cada una
 * con cada producto una sola vez. Sus favoritos (AB#528) son una lista fija aparte, de tipo FAVORITOS.
 */
public class ListaDeseos {

    public static final String NOMBRE_FAVORITOS = "Favoritos";
    public static final String NOMBRE_POR_DEFECTO = "Mi lista de deseos";
    public static final int NOMBRE_MAXIMO = 80;

    private final String id;
    private final String clienteId;
    private final TipoListaDeseos tipo;
    private String nombre;
    private final List<ItemDeseo> items;

    // Los favoritos de un cliente
    public ListaDeseos(String id, String clienteId, List<ItemDeseo> items) {
        this(id, clienteId, TipoListaDeseos.FAVORITOS, NOMBRE_FAVORITOS, items);
    }

    public ListaDeseos(String id, String clienteId, TipoListaDeseos tipo, String nombre, List<ItemDeseo> items) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.nombre = tipo == TipoListaDeseos.FAVORITOS ? NOMBRE_FAVORITOS : nombreDeLista(nombre);
        this.items = new ArrayList<>(items);
    }

    // Favoritos aún no guardados, para un cliente que todavía no tiene ninguno
    public static ListaDeseos vacia(String clienteId) {
        return new ListaDeseos(null, clienteId, List.of());
    }

    public static ListaDeseos nueva(String clienteId, String nombre) {
        return new ListaDeseos(null, clienteId, TipoListaDeseos.LISTA, normalizarNombre(nombre), List.of());
    }

    public void renombrar(String nuevoNombre) {
        if (esFavoritos()) {
            throw new ListaDeseosInvalidaException("Los favoritos no se pueden renombrar");
        }
        this.nombre = normalizarNombre(nuevoNombre);
    }

    public boolean esFavoritos() {
        return tipo == TipoListaDeseos.FAVORITOS;
    }

    private static String nombreDeLista(String nombre) {
        return nombre == null || nombre.isBlank() ? NOMBRE_POR_DEFECTO : normalizarNombre(nombre);
    }

    private static String normalizarNombre(String nombre) {
        if (nombre == null) {
            throw new ListaDeseosInvalidaException("El nombre de la lista es obligatorio");
        }
        String recortado = nombre.trim();
        if (recortado.isEmpty() || recortado.length() > NOMBRE_MAXIMO) {
            throw new ListaDeseosInvalidaException("El nombre de la lista no es válido");
        }
        return recortado;
    }

    // Añadir un producto que ya está no es un error: la lista ya queda como se quería y conserva su fecha
    public void anadir(String productoId, Instant ahora) {
        boolean yaEsta = items.stream().anyMatch(item -> item.productoId().equals(productoId));
        if (!yaEsta) {
            items.add(new ItemDeseo(productoId, ahora, 1));
        }
    }

    /**
     * Actualiza la cantidad deseada de un producto de la lista, respetando el stock disponible.
     */
    public void cambiarCantidad(String productoId, int cantidad, int stockDisponible) {
        if (cantidad < 1 || cantidad > stockDisponible) {
            throw new ListaDeseosInvalidaException("La cantidad no es válida para el stock disponible");
        }
        for (int i = 0; i < items.size(); i++) {
            ItemDeseo actual = items.get(i);
            if (actual.productoId().equals(productoId)) {
                items.set(i, new ItemDeseo(actual.productoId(), actual.addedAt(), cantidad));
                return;
            }
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

    public TipoListaDeseos getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<ItemDeseo> getItems() {
        return List.copyOf(items);
    }
}
