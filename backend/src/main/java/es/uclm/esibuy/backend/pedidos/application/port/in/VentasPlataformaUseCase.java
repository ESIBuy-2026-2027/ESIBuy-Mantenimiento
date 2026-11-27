package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.List;

/** Líneas de venta de toda la plataforma para las estadísticas del administrador. */
public interface VentasPlataformaUseCase {

    /** Devuelve las líneas de todos los pedidos que no se han cancelado. */
    List<VentaProducto> consultar();
}
