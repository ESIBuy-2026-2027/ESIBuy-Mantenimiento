package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/** Gestión de los productos propios por parte del vendedor (HU31 alta, HU33 consulta; HU34 y HU35 añadirán más). */
public interface GestionProductosUseCase {

    Producto crear(CrearProductoCommand comando);

    /**
     * Devuelve el detalle de un producto del vendedor en sesión (HU33).
     *
     * @param emailVendedor correo del vendedor en sesión: solo puede consultar sus productos
     * @param productoId id del producto
     * @return el producto
     */
    Producto consultarMiProducto(String emailVendedor, String productoId);
}
