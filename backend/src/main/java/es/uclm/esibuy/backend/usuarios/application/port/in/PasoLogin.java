package es.uclm.esibuy.backend.usuarios.application.port.in;


public enum PasoLogin {
    CODIGO_EMAIL,         // paso 2: introducir el código enviado al correo
    AUTHENTICATOR,        // paso 3: ya tiene Authenticator; introducir su código
    ALTA_AUTHENTICATOR    // paso 3 con alta: aún no tiene Authenticator; escanear el QR y luego su primer código
}
