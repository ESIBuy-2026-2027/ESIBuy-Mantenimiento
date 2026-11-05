package es.uclm.esibuy.backend.carrito.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException;
import es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException;

/**
 * Carrito de un cliente (HU9): una línea por producto. Las reglas de cantidad y de stock viven aquí; el stock
 * lo pasa quien llama, que lo consulta al módulo de productos.
 */
public class Carrito {

    private final String id;
    private final String clienteId;
    private final List<LineaCarrito> lineas;

    public Carrito(String id, String clienteId, List<LineaCarrito> lineas) {
        this.id = id;
        this.clienteId = clienteId;
        this.lineas = new ArrayList<>(lineas);
    }

    // Carrito aún no guardado, para un cliente que todavía no tiene ninguno
    public static Carrito vacio(String clienteId) {
        return new Carrito(null, clienteId, List.of());
    }

    // Suma las unidades a las que ya hubiera de ese producto, sin pasar del stock
    public void anadir(String productoId, int cantidad, int stock, Instant ahora) {
        LineaCarrito.exigirCantidad(cantidad);
        Optional<LineaCarrito> existente = buscar(productoId);
        int total = existente.map(LineaCarrito::cantidad).orElse(0) + cantidad;
        exigirStock(total, stock);
        existente.ifPresentOrElse(
                linea -> sustituir(linea, linea.conCantidad(total, ahora)),
                () -> lineas.add(new LineaCarrito(productoId, total, ahora, ahora)));
    }

    // Sustituye la cantidad de un producto que ya está en el carrito
    public void fijarCantidad(String productoId, int cantidad, int stock, Instant ahora) {
        LineaCarrito.exigirCantidad(cantidad);
        LineaCarrito linea = buscar(productoId)
                .orElseThrow(() -> new LineaCarritoNoEncontradaException("El producto no está en el carrito"));
        exigirStock(cantidad, stock);
        sustituir(linea, linea.conCantidad(cantidad, ahora));
    }

    // Quitar un producto que no está no es un error: el carrito ya queda como se quería
    public void quitar(String productoId) {
        lineas.removeIf(linea -> linea.productoId().equals(productoId));
    }

    public void vaciar() {
        lineas.clear();
    }

    /**
     * Limpieza del carrito (AB#390): quita los productos que ya no se pueden comprar (no están en
     * {@code stockDeLosComprables} o no les queda stock) y baja la cantidad al stock si este ha bajado.
     *
     * @param stockDeLosComprables stock de cada producto del carrito que todavía se puede comprar, por su id
     * @return si el carrito ha cambiado y hay que guardarlo
     */
    public boolean limpiar(Map<String, Integer> stockDeLosComprables, Instant ahora) {
        boolean cambiado = false;
        for (LineaCarrito linea : List.copyOf(lineas)) {
            int stock = stockDeLosComprables.getOrDefault(linea.productoId(), 0);
            if (stock < 1) {
                lineas.remove(linea);
                cambiado = true;
            } else if (linea.cantidad() > stock) {
                sustituir(linea, linea.conCantidad(stock, ahora));
                cambiado = true;
            }
        }
        return cambiado;
    }

    private Optional<LineaCarrito> buscar(String productoId) {
        return lineas.stream().filter(linea -> linea.productoId().equals(productoId)).findFirst();
    }

    private void sustituir(LineaCarrito anterior, LineaCarrito nueva) {
        lineas.set(lineas.indexOf(anterior), nueva);
    }

    private static void exigirStock(int cantidad, int stock) {
        if (cantidad > stock) {
            throw new StockInsuficienteException("No hay stock suficiente");
        }
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<LineaCarrito> getLineas() {
        return List.copyOf(lineas);
    }
}
