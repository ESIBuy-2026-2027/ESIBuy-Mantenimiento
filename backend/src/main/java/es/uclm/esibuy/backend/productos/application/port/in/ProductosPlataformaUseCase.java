package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.List;

/** Consulta los productos de toda la plataforma para las estadísticas del administrador. */
public interface ProductosPlataformaUseCase {

    List<ProductoEstadistica> listar();
}
