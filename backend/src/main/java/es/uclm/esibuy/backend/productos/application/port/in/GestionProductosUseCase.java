package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/** Gestión de los productos propios por parte del vendedor (HU31 alta, HU33 consulta, HU34 modificación, HU35 eliminación y HU16 activación y desactivación). */
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

    /**
     * Modifica un producto del vendedor en sesión (HU34), con las mismas reglas que el alta.
     *
     * @param comando correo del vendedor en sesión, id del producto y datos nuevos
     * @return el producto guardado
     */
    Producto modificarMiProducto(ModificarProductoCommand comando);

    /**
     * Elimina un producto del vendedor en sesión (HU35). El borrado es físico.
     *
     * @param emailVendedor correo del vendedor en sesión: solo puede eliminar sus productos
     * @param productoId id del producto
     */
    void eliminarMiProducto(String emailVendedor, String productoId);

    /**
     * Activa o desactiva un producto del vendedor en sesión (HU16). Solo los productos disponibles salen en el
     * catálogo del cliente. Pedir el estado que ya tiene no es un error: el producto queda igual.
     *
     * @param emailVendedor correo del vendedor en sesión: solo puede cambiar sus productos
     * @param productoId id del producto
     * @param disponible {@code true} para activarlo y {@code false} para desactivarlo
     * @return el producto guardado
     */
    Producto cambiarDisponibilidadMiProducto(String emailVendedor, String productoId, boolean disponible);
}
