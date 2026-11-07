package es.uclm.esibuy.backend.deseos.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Un producto de la lista de deseos con sus datos actuales. {@code precioFinal} ya lleva el descuento que le toca
 * al cliente, y {@code disponible} es falso si el vendedor lo ha desactivado.
 */
public record DeseoDetallado(String productoId, String nombre, String imagen, BigDecimal precioFinal, int stock,
        boolean disponible, Instant addedAt) {
}
