package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.OptionalLong;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class JavaTotpAdapter implements TotpPort{

    private static final int SEGUNDOS_POR_PERIODO = 30;
    private static final int DIGITOS = 6;
    private static final int PERIODOS_DE_DESFASE = 1;
    private static final String EMISOR = "ESIBuy";   // nombre que verá el usuario en Authenticator

    private final CodeGenerator generador = new DefaultCodeGenerator(HashingAlgorithm.SHA1, DIGITOS);
    private final SecretGenerator generadorSecretos = new DefaultSecretGenerator();
private final QrGenerator generadorQr = new ZxingPngQrGenerator();

    @Override
    public OptionalLong periodoDelCodigo(String secreto, String codigo, Instant ahora) {
        long periodoActual = ahora.getEpochSecond() / SEGUNDOS_POR_PERIODO;
        for (long periodo = periodoActual - PERIODOS_DE_DESFASE; periodo <= periodoActual + PERIODOS_DE_DESFASE; periodo++) {
            if (coincide(generarCodigo(secreto, periodo), codigo)) {
                return OptionalLong.of(periodo);                
            }            
        }
        return OptionalLong.empty();
    }

    private String generarCodigo(String secreto, long periodo) {
        try {
            return generador.generate(secreto, periodo);
        } catch (CodeGenerationException e) {
            throw new IllegalStateException("No se ha podido generar el código TOTP", e);
        }
    }

    private static boolean coincide(String esperado, String recibido) {
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generarQr(String email, String secreto) {
            // Lo que contiene el QR: otpauth://totp/ESIBuy:juan@uclm.es?secret=...&issuer=ESIBuy&digits=6&period=30
        QrData datos = new QrData.Builder()
                .label(email)
                .secret(secreto)
                .issuer(EMISOR)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(DIGITOS)
                .period(SEGUNDOS_POR_PERIODO)
                .build();
        try {
            return getDataUriForImage(generadorQr.generate(datos), generadorQr.getImageMimeType());
        } catch (QrGenerationException e) {
            throw new IllegalStateException("No se ha podido generar el QR del alta", e);
        }
    }

    @Override
    public String generarSecreto() {
        return generadorSecretos.generate();   // 32 caracteres Base32 (160 bits), con SecureRandom por dentro
    }

    
    
}
