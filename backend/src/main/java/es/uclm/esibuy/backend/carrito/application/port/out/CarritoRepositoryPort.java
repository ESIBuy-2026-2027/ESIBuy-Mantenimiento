package es.uclm.esibuy.backend.carrito.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.carrito.domain.model.Carrito;

/** Persistencia de los carritos: uno por cliente. */
public interface CarritoRepositoryPort {

    Optional<Carrito> buscarPorCliente(String clienteId);

    // Devuelve el carrito guardado: si era nuevo, con el id que le ha asignado la base de datos
    Carrito guardar(Carrito carrito);

    void eliminarPorCliente(String clienteId);
}
