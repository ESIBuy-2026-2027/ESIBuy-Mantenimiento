package es.uclm.esibuy.backend.deseos.application.port.out;

import java.math.BigDecimal;

/** Los datos actuales de un producto de la lista de deseos, tal como los da el módulo de productos. */
public record ProductoDeseado(String id, String nombre, String imagen, BigDecimal precioFinal, int stock,
        boolean disponible) {
}
