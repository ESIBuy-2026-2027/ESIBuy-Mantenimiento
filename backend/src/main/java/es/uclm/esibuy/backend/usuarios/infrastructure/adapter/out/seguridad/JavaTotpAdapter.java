package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.OptionalLong;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;

public class JavaTotpAdapter implements  TotpPort{

    private static final int SEGUNDOS_POR_PERIODO = 30;
    private static final int DIGITOS = 6;
    private static final int PERIODOS_DE_DESFASE = 1;

    private final CodeGenerator generador = new DefaultCodeGenerator(HashingAlgorithm.SHA1, DIGITOS);

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
    
}
