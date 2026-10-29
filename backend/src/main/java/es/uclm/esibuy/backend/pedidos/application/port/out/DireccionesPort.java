package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;

/** Lo que pedidos necesita del módulo de direcciones (HU5): los datos de una dirección del cliente. */
@FunctionalInterface
public interface DireccionesPort {

    // Vacío si la dirección no existe o es de otro cliente
    Optional<DireccionEnvioPedido> consultar(String emailCliente, String direccionId);
}
