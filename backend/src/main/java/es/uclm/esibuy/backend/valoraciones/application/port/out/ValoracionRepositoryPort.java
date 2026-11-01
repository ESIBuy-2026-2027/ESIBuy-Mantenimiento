package es.uclm.esibuy.backend.valoraciones.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Persistencia de las valoraciones: una por cliente y producto. */
public interface ValoracionRepositoryPort {

    // Devuelve la valoración guardada: si era nueva, con el id que le ha asignado la base de datos
    Valoracion guardar(Valoracion valoracion);

    boolean existePorClienteYProducto(String clienteId, String productoId);

    List<Valoracion> buscarPorProducto(String productoId);
}   