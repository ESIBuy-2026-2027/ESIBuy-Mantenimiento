package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.in.web;

import java.util.regex.Pattern;

import es.uclm.esibuy.backend.metodospago.application.port.in.AnadirMetodoPagoCommand;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Petición de alta de un método de pago (HU6). El CVV solo se valida aquí: no pasa al comando, así que
 * nunca llega a la aplicación ni se guarda.
 */
public record MetodoPagoRequest(
        @NotNull TipoMetodoPago tipo,
        String numeroTarjeta,
        String titular,
        String caducidad,
        String cvv,
        String urlPayPal,
        String telefonoBizum) {

    // 3 dígitos (Visa, Mastercard) o 4 (American Express)
    private static final Pattern FORMATO_CVV = Pattern.compile("^[0-9]{3,4}$");

    // Una tarjeta exige un CVV con formato válido; los demás tipos no lo usan
    @AssertTrue(message = "El CVV de la tarjeta no es válido")
    public boolean isCvvValido() {
        return tipo != TipoMetodoPago.TARJETA || (cvv != null && FORMATO_CVV.matcher(cvv).matches());
    }

    // El cliente sale de la sesión, nunca del cuerpo
    public AnadirMetodoPagoCommand aComando(String emailCliente) {
        return new AnadirMetodoPagoCommand(emailCliente, tipo, numeroTarjeta, titular, caducidad, urlPayPal,
                telefonoBizum);
    }
}
