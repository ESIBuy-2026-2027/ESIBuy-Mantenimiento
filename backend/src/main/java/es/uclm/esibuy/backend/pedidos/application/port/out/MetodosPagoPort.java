package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.util.Optional;

/** Lo que pedidos necesita del módulo de métodos de pago (HU6). */
@FunctionalInterface
public interface MetodosPagoPort {

    // Vacío si el método no existe o es de otro cliente
    Optional<MetodoPagoPedido> consultar(String emailCliente, String metodoId);
}
