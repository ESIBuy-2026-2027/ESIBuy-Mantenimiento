package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.shared.infrastructure.seguridad.CifradorAesGcm;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring. El cifrado en sí se prueba en CifradorAesGcmTest; aquí, que el adaptador cifra con su clave
// y que solo acepta una clave AES-256 válida
class AesGcmCifradoDatosPagoAdapterTest {

    private static final String DATOS = "4111111111111111;Juan Pérez;12/27";

    // Clave de prueba de n bytes, en Base64 como llega de la variable de entorno
    private static String claveDe(int bytes) {
        byte[] clave = new byte[bytes];
        Arrays.fill(clave, (byte) 7);
        return Base64.getEncoder().encodeToString(clave);
    }

    @Test
    @DisplayName("HU6-CP16: cifra con la clave configurada")
    void cifraConLaClaveConfigurada() {
        String clave = claveDe(32);
        AesGcmCifradoDatosPagoAdapter adaptador = new AesGcmCifradoDatosPagoAdapter(clave);

        String cifrado = adaptador.cifrar(DATOS);

        // Solo quien tiene la misma clave recupera los datos
        assertEquals(DATOS, new CifradorAesGcm(Base64.getDecoder().decode(clave)).descifrar(cifrado));
    }

    @Test
    @DisplayName("HU6-CP16: rechaza una clave nula o vacía")
    void rechazaClaveNulaOVacia() {
        // Sin la variable de entorno la aplicación no debe arrancar cifrando con una clave cualquiera
        assertThrows(IllegalArgumentException.class, () -> new AesGcmCifradoDatosPagoAdapter(null));
        assertThrows(IllegalArgumentException.class, () -> new AesGcmCifradoDatosPagoAdapter(""));
        assertThrows(IllegalArgumentException.class, () -> new AesGcmCifradoDatosPagoAdapter("   "));
    }

    @Test
    @DisplayName("HU6-CP16: rechaza una clave que no está en Base64")
    void rechazaClaveQueNoEsBase64() {
        String clave = "esto-no-es-base64!";

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new AesGcmCifradoDatosPagoAdapter(clave));
        // Un mensaje propio que diga qué clave falla, sin repetir su valor
        assertEquals("La clave de cifrado de los datos de pago no está en Base64", error.getMessage());
    }

    @Test
    @DisplayName("HU6-CP16: rechaza una clave que no mide 32 bytes")
    void rechazaClaveQueNoMide32Bytes() {
        // AES admite claves de 16 y 24 bytes, pero los datos de pago exigen AES-256: solo valen 32
        for (int bytes : new int[] {16, 24, 31, 33}) {
            String clave = claveDe(bytes);
            assertThrows(IllegalArgumentException.class, () -> new AesGcmCifradoDatosPagoAdapter(clave),
                    "Debería rechazar una clave de " + bytes + " bytes");
        }
    }
}
