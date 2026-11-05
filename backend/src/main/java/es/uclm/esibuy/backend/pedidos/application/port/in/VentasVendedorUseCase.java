package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.List;

/**
 * Ventas de un vendedor para sus estadísticas (HU17): el módulo de estadísticas las pide con el id del vendedor. No
 * hay un caso de uso del vendedor ni del cliente con este propósito.
 */
public interface VentasVendedorUseCase {

    /** Las líneas de los pedidos del vendedor que no se han cancelado. */
    List<VentaProducto> ventasDe(String vendedorId);
}
