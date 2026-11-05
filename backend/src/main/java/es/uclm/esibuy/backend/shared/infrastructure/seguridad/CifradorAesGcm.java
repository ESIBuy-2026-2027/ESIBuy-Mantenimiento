package es.uclm.esibuy.backend.shared.infrastructure.seguridad;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifrado AES-GCM compartido por los adaptadores que guardan datos sensibles cifrados (el secreto TOTP en
 * usuarios, los datos de pago en metodospago). Cada adaptador decide qué clave usa y cómo la valida.
 *
 * <p>Formato del resultado: Base64 de IV (12 bytes) seguido del texto cifrado con su etiqueta de 128 bits.
 */
public class CifradorAesGcm {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int BYTES_IV = 12;
    private static final int BITS_ETIQUETA = 128;

    private final SecretKey clave;
    private final SecureRandom aleatorio = new SecureRandom();

    public CifradorAesGcm(byte[] clave) {
        this.clave = new SecretKeySpec(clave, "AES");
    }

    public String cifrar(String textoEnClaro) {
        try {
            byte[] iv = new byte[BYTES_IV];
            aleatorio.nextBytes(iv); // un IV nuevo y aleatorio en cada cifrado
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(BITS_ETIQUETA, iv));
            byte[] cifrado = cipher.doFinal(textoEnClaro.getBytes(StandardCharsets.UTF_8));
            byte[] resultado = ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
            return Base64.getEncoder().encodeToString(resultado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se ha podido cifrar", e);
        }
    }

    public String descifrar(String textoCifrado) {
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifrado);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            // Los primeros 12 bytes son el IV; el resto, el texto cifrado con su etiqueta
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(BITS_ETIQUETA, datos, 0, BYTES_IV));
            byte[] enClaro = cipher.doFinal(datos, BYTES_IV, datos.length - BYTES_IV);
            return new String(enClaro, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            // Incluye AEADBadTagException: el texto cifrado se ha manipulado o la clave no es la correcta
            throw new IllegalStateException("No se ha podido descifrar", e);
        }
    }
}
