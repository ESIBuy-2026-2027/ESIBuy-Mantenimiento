package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.List;

/**
 * Productos de un vendedor para sus estadísticas (HU17): el módulo de estadísticas los pide con el id del vendedor.
 * A diferencia del listado de HU32, no pagina ni filtra. No hay un caso de uso del vendedor ni del catálogo con este
 * propósito.
 */
public interface InventarioVendedorUseCase {

    /** Todos los productos del vendedor, disponibles o no. */
    List<ProductoInventario> productosDe(String vendedorId);
}
