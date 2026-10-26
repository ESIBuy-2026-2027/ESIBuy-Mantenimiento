package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.net.URI;
import java.net.URISyntaxException;

public record UrlPayPal(String valor) {

    public UrlPayPal {
        if (valor == null || !esUrlConServidor(valor)) {
            throw new MetodoPagoInvalidoException("La URL de PayPal no es válida");
        }
    }

    // Solo el servidor: la ruta identifica la cuenta del cliente
    public String mascara() {
        return URI.create(valor).getHost();
    }

    // Una URL absoluta: con esquema y con servidor
    private static boolean esUrlConServidor(String texto) {
        try {
            URI uri = new URI(texto);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
