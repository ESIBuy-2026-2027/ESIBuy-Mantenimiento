package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

/**
 * Respuesta del alta de Microsoft Authenticator (HU2, T20): el QR como imagen (data URI), listo para
 * un {@code <img src>} del frontend.
 */
public record AltaAuthenticatorResponse(String qr) {
}
