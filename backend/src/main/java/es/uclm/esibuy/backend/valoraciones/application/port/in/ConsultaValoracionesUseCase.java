package es.uclm.esibuy.backend.valoraciones.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Consulta de las valoraciones de un producto por parte del cliente (HU13). */
public interface ConsultaValoracionesUseCase {

    /** Las valoraciones del producto, sin orden garantizado; una lista vacía si no tiene ninguna. */
    List<Valoracion> consultarPorProducto(String productoId);
}
