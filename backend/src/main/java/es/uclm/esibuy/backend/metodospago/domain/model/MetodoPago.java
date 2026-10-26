package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;

/**
 * Método de pago de un cliente. Java puro. Los datos sensibles llegan ya cifrados como un bloque opaco:
 * el dominio nunca los tiene en claro. Solo la máscara puede mostrarse. El cliente se referencia por
 * identificador.
 */
public class MetodoPago {

    private final String id;
    private final String clienteId;
    private final TipoMetodoPago tipo;
    private final String mascara;
    private final String datosCifrados;
    private boolean predeterminado;

    public MetodoPago(String id, String clienteId, TipoMetodoPago tipo, String mascara, String datosCifrados) {
        this.id = id;
        this.clienteId = exigirTexto(clienteId, "El cliente es obligatorio");
        if (tipo == null) {
            throw new MetodoPagoInvalidoException("El tipo de método de pago es obligatorio");
        }
        this.tipo = tipo;
        this.mascara = exigirTexto(mascara, "La máscara es obligatoria");
        this.datosCifrados = exigirTexto(datosCifrados, "Los datos cifrados son obligatorios");
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new MetodoPagoInvalidoException(mensaje);
        }
        return valor;
    }

    // Alta de un método nuevo: sin id hasta que se guarda
    public static MetodoPago nuevo(String clienteId, TipoMetodoPago tipo, String mascara, String datosCifrados) {
        return new MetodoPago(null, clienteId, tipo, mascara, datosCifrados);
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public TipoMetodoPago getTipo() {
        return tipo;
    }

    public String getMascara() {
        return mascara;
    }

    public String getDatosCifrados() {
        return datosCifrados;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void marcarPredeterminado() {
        this.predeterminado = true;
    }

    public void desmarcarPredeterminado() {
        this.predeterminado = false;
    }
}
