package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;

/**
 * Lo que la API devuelve de un método de pago: solo la máscara. Nunca el bloque cifrado ni el cliente,
 * que ya es el de la sesión.
 */
public record MetodoPagoResponse(String id, String tipo, String mascara, boolean predeterminado) {

    public static MetodoPagoResponse desde(MetodoPago metodo) {
        return new MetodoPagoResponse(metodo.getId(), metodo.getTipo().name(), metodo.getMascara(),
                metodo.isPredeterminado());
    }
}
