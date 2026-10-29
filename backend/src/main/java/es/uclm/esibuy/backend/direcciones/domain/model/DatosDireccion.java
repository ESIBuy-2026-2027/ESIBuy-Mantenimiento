package es.uclm.esibuy.backend.direcciones.domain.model;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;

/**
 * Lo que el cliente escribe de una dirección de envío (HU5). Se valida al construirse, así que una dirección
 * con estos datos siempre es enviable. Los detalles (piso, puerta...) son lo único opcional.
 */
public record DatosDireccion(
        String destinatario,
        String calle,
        String detalles,
        CodigoPostal codigoPostal,
        String ciudad,
        String provincia,
        TelefonoContacto telefono) {

    private static final int LONGITUD_MAXIMA = 100;
    // La calle lleva también el número
    private static final int LONGITUD_MAXIMA_CALLE = 150;

    public DatosDireccion {
        exigirTexto(destinatario, LONGITUD_MAXIMA, "El destinatario");
        exigirTexto(calle, LONGITUD_MAXIMA_CALLE, "La calle");
        exigirTexto(ciudad, LONGITUD_MAXIMA, "La ciudad");
        exigirTexto(provincia, LONGITUD_MAXIMA, "La provincia");
        // Unos detalles en blanco son unos detalles ausentes
        detalles = detalles == null || detalles.isBlank() ? null : detalles;
        if (detalles != null && detalles.length() > LONGITUD_MAXIMA) {
            throw new DireccionInvalidaException("Los detalles no pueden superar " + LONGITUD_MAXIMA + " caracteres");
        }
        if (codigoPostal == null) {
            throw new DireccionInvalidaException("El código postal es obligatorio");
        }
        if (telefono == null) {
            throw new DireccionInvalidaException("El teléfono de contacto es obligatorio");
        }
    }

    private static void exigirTexto(String valor, int longitudMaxima, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DireccionInvalidaException(campo + " es obligatorio");
        }
        if (valor.length() > longitudMaxima) {
            throw new DireccionInvalidaException(campo + " no puede superar " + longitudMaxima + " caracteres");
        }
    }
}
