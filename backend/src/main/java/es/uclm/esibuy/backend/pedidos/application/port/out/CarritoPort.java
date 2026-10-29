package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.util.List;
import java.util.Optional;

/** Lo que pedidos necesita del módulo del carrito (HU9): lo que el cliente compra de un proveedor, y quitarlo. */
public interface CarritoPort {

    // Vacío si el cliente no tiene en el carrito productos de ese proveedor
    Optional<CompraProveedor> compraDelProveedor(String emailCliente, String vendedorId);

    void quitar(String emailCliente, List<String> productoIds);
}
