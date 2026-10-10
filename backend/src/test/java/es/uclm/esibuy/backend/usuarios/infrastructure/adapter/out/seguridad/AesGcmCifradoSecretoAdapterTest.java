package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Test del adaptador real de cifrado (T20, D3), sin Spring y sin mocks. El secreto TOTP no se puede
// hashear como una contraseña: el servidor necesita leerlo para calcular el código. Por eso se cifra
// con AES-GCM antes de guardarlo en MongoDB (factores_autenticacion.totpSecretEncrypted).
class AesGcmCifradoSecretoAdapterTest {

    // Clave AES de 256 bits (32 bytes) en Base64, solo para los tests. La de la aplicación va en
    // application.properties (riesgo asumido, D3)
    private static final String CLAVE_DE_PRUEBA = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String SECRETO_TOTP = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    private final CifradoSecretoPort cifrado = new AesGcmCifradoSecretoAdapter(CLAVE_DE_PRUEBA);

    @Test
    @DisplayName("HU2-CP24: lo que se cifra se recupera igual al descifrarlo")
    void descifrarDevuelveElSecretoOriginal() {
        String cifradoGuardado = cifrado.cifrar(SECRETO_TOTP);

        assertEquals(SECRETO_TOTP, cifrado.descifrar(cifradoGuardado));
    }

    @Test
    @DisplayName("HU2-CP24: el texto cifrado no contiene el secreto en claro")
    void elCifradoNoContieneElSecreto() {
        assertFalse(cifrado.cifrar(SECRETO_TOTP).contains(SECRETO_TOTP));
    }

    @Test
    @DisplayName("HU2-CP24: cifrar dos veces el mismo secreto da resultados distintos")
    void cadaCifradoEsDistinto() {
        // AES-GCM usa un valor aleatorio (IV) en cada cifrado: dos usuarios con el mismo secreto no
        // tendrían el mismo dato guardado, y no se puede deducir uno a partir del otro
        assertNotEquals(cifrado.cifrar(SECRETO_TOTP), cifrado.cifrar(SECRETO_TOTP));
    }

    @Test
    @DisplayName("HU2-CP24: un texto cifrado manipulado no se puede descifrar")
    void unCifradoManipuladoSeRechaza() {
        byte[] bytes = Base64.getDecoder().decode(cifrado.cifrar(SECRETO_TOTP));
        bytes[bytes.length - 1] ^= 1; // cambia un solo bit del final
        String manipulado = Base64.getEncoder().encodeToString(bytes);

        // GCM lleva una «etiqueta» de autenticación: si alguien cambia un solo bit, el descifrado falla
        // en vez de devolver un secreto falso
        assertThrows(IllegalStateException.class, () -> cifrado.descifrar(manipulado));
    }
}
