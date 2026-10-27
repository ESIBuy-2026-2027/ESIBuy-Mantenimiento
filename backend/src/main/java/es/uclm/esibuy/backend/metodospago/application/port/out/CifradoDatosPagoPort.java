package es.uclm.esibuy.backend.metodospago.application.port.out;

/**
 * Cifra los datos sensibles de un método de pago antes de guardarlos (los datos de pago nunca en claro).
 *
 * <p>PENDIENTE: descifrar() se añadirá cuando el pago de un pedido (HU10) necesite el dato completo.
 */
public interface CifradoDatosPagoPort {

    String cifrar(String datosEnClaro);
}
