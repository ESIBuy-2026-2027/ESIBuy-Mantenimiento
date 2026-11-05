package es.uclm.esibuy.backend.pedidos.application.port.out;

/** Lo que pedidos necesita del módulo de métodos de pago (HU6). */
@FunctionalInterface
public interface MetodosPagoPort {

    boolean esDelCliente(String emailCliente, String metodoId);
}
