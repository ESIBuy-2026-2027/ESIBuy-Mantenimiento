package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.seguridad;

import java.util.Base64;

import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.shared.infrastructure.seguridad.CifradorAesGcm;

// Cifra los datos de pago con AES-GCM (CifradorAesGcm) y una clave propia, distinta de la del secreto TOTP
public class AesGcmCifradoDatosPagoAdapter implements CifradoDatosPagoPort {

    // AES-256: se genera con `openssl rand -base64 32`
    private static final int BYTES_CLAVE = 32;

    private final CifradorAesGcm cifrador;

    // Falla al crearse, y por tanto al arrancar la aplicación, si la clave no es válida. Los mensajes nunca
    // incluyen la clave.
    public AesGcmCifradoDatosPagoAdapter(String claveBase64) {
        if (claveBase64 == null || claveBase64.isBlank()) {
            throw new IllegalArgumentException("Falta la clave de cifrado de los datos de pago");
        }
        byte[] clave = descodificar(claveBase64);
        if (clave.length != BYTES_CLAVE) {
            throw new IllegalArgumentException(
                    "La clave de cifrado de los datos de pago debe medir " + BYTES_CLAVE + " bytes (AES-256)");
        }
        this.cifrador = new CifradorAesGcm(clave);
    }

    private static byte[] descodificar(String claveBase64) {
        try {
            return Base64.getDecoder().decode(claveBase64);
        } catch (IllegalArgumentException e) {
            // Sin encadenar la causa: su mensaje indica qué carácter de la clave es incorrecto
            throw new IllegalArgumentException("La clave de cifrado de los datos de pago no está en Base64");
        }
    }

    @Override
    public String cifrar(String datosEnClaro) {
        return cifrador.cifrar(datosEnClaro);
    }
}
