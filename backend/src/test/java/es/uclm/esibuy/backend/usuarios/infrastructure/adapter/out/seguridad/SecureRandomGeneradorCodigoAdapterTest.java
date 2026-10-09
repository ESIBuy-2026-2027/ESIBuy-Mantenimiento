package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

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
}
