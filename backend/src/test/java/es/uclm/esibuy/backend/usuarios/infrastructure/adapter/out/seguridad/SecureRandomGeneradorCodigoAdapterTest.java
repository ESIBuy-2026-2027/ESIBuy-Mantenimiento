package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test del adaptador real (sin Spring y sin mocks), como BCryptHashContrasenaAdapterTest.
// El adaptador recibe la fuente aleatoria por constructor (T231, D5): aquí se le pasa un SecureRandom
// real para comprobar el formato, y en los casos frontera una fuente controlada que devuelve siempre
// el mismo número, porque con un número al azar el test no daría siempre el mismo resultado.
class SecureRandomGeneradorCodigoAdapterTest {

    @Test
    @DisplayName("HU2-CP13: genera un código de 6 dígitos")
    void generaUnCodigoDeSeisDigitos() {
        GeneradorCodigoPort generador = new SecureRandomGeneradorCodigoAdapter(new SecureRandom());

        String codigo = generador.generar();

        // \d{6}: exactamente 6 caracteres y todos dígitos (ni letras, ni signo, ni espacios)
        assertTrue(codigo.matches("\\d{6}"), "El código debe tener 6 dígitos, pero fue: " + codigo);
    }

    @Test
    @DisplayName("HU2-CP13: rellena con ceros a la izquierda los números de menos de 6 cifras")
    void rellenaConCerosALaIzquierda() {
        GeneradorCodigoPort generador = new SecureRandomGeneradorCodigoAdapter(fuenteQueDevuelve(42));

        // Como número, 000042 sería 42: el usuario recibiría un código de 2 cifras
        assertEquals("000042", generador.generar());
    }

    // Fuente controlada: un SecureRandom que siempre devuelve el mismo número. Se sobrescribe
    // nextInt(int), que es el método que usa el adaptador para pedir un número por debajo del límite.
    private static SecureRandom fuenteQueDevuelve(int valor) {
        return new SecureRandom() {
            @Override
            public int nextInt(int limite) {
                return valor;
            }
        };
    }
}
