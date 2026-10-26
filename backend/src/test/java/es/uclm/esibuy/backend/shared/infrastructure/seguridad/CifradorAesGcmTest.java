package es.uclm.esibuy.backend.shared.infrastructure.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring. Lo usan el secreto TOTP (usuarios) y los datos de pago (metodospago)
class CifradorAesGcmTest {

    // Claves AES-256 de prueba (32 bytes), solo para los tests
    private static final byte[] CLAVE = new byte[32];
    private static final byte[] OTRA_CLAVE = rellena((byte) 1);
    private static final String DATOS = "4111111111111111;Juan Pérez;12/27";

    private final CifradorAesGcm cifrador = new CifradorAesGcm(CLAVE);

    private static byte[] rellena(byte valor) {
        byte[] clave = new byte[32];
        Arrays.fill(clave, valor);
        return clave;
    }

    @Test
    @DisplayName("HU6-CP16: con la misma clave se recuperan los datos originales")
    void conLaMismaClaveSeRecuperanLosDatos() {
        assertEquals(DATOS, cifrador.descifrar(cifrador.cifrar(DATOS)));
    }

    @Test
    @DisplayName("HU6-CP16: el resultado del cifrado no contiene los datos en claro")
    void elCifradoNoContieneLosDatos() {
        String cifrado = cifrador.cifrar(DATOS);

        assertFalse(cifrado.contains("4111111111111111"));
        assertFalse(cifrado.contains("Juan Pérez"));
    }

    @Test
    @DisplayName("HU6-CP16: cifrar dos veces los mismos datos da resultados distintos")
    void cadaCifradoEsDistinto() {
        // IV aleatorio en cada cifrado: dos tarjetas iguales no dejan el mismo dato guardado
        assertNotEquals(cifrador.cifrar(DATOS), cifrador.cifrar(DATOS));
    }

    @Test
    @DisplayName("HU6-CP16: con otra clave los datos no se pueden descifrar")
    void conOtraClaveNoSeDescifran() {
        String cifrado = cifrador.cifrar(DATOS);

        CifradorAesGcm otro = new CifradorAesGcm(OTRA_CLAVE);
        assertThrows(IllegalStateException.class, () -> otro.descifrar(cifrado));
    }

    @Test
    @DisplayName("HU6-CP16: un cifrado manipulado no se puede descifrar")
    void unCifradoManipuladoSeRechaza() {
        byte[] bytes = Base64.getDecoder().decode(cifrador.cifrar(DATOS));
        bytes[bytes.length - 1] ^= 1; // cambia un solo bit de la etiqueta
        String manipulado = Base64.getEncoder().encodeToString(bytes);

        assertThrows(IllegalStateException.class, () -> cifrador.descifrar(manipulado));
    }
}
