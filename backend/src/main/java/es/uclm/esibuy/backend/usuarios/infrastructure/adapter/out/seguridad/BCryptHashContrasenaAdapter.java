
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class BCryptHashContrasenaAdapter implements HashContrasenaPort {

    private static final int COSTE_BCRYPT = 10;
    private static final String ALGORITMO_HMAC = "HmacSHA256";
    private static final String PREFIJO_HASH = "{bcrypt-pepper-v1}";

    private final BCryptPasswordEncoder encoder;
    private final SecretKeySpec pepperKey;

    public BCryptHashContrasenaAdapter(String pepperBase64) {
        if (pepperBase64 == null || pepperBase64.isBlank()) {
            throw new IllegalArgumentException("El Pepper no puede estar vacío");
        }

        byte[] pepperBytes;

        try {
            pepperBytes = Base64.getDecoder().decode(pepperBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El Pepper debe estar codificado en Base64", exception);
        }

        if (pepperBytes.length != 32) {
            throw new IllegalArgumentException("El Pepper debe contener exactamente 32 bytes");
        }

        this.pepperKey = new SecretKeySpec(pepperBytes, ALGORITMO_HMAC);
        this.encoder = new BCryptPasswordEncoder(COSTE_BCRYPT);
    }

    @Override
    public String hashear(String password) {
        String passwordProtegida = aplicarPepper(password);
        return PREFIJO_HASH + encoder.encode(passwordProtegida);
    }

    @Override
    public boolean coincide(String password, String hash) {
        if (hash == null || password == null) {
            return false;
        }

        if (hash.startsWith(PREFIJO_HASH)) {
            String hashBCrypt = hash.substring(PREFIJO_HASH.length());
            return encoder.matches(aplicarPepper(password), hashBCrypt);
        }

        // Compatibilidad con hashes BCrypt creados antes de incorporar Pepper.
        return encoder.matches(password, hash);
    }

    private String aplicarPepper(String password) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(pepperKey);

            byte[] resultado = mac.doFinal(password.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(resultado);

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo aplicar el Pepper a la contraseña", exception);
        }
    }

    @Override
    public boolean necesitaRehash(String hash) {
        return hash == null || !hash.startsWith(PREFIJO_HASH);
    }

}
