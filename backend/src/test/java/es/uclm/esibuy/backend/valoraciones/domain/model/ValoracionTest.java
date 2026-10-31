package es.uclm.esibuy.backend.valoraciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionInvalidaException;

// Entidad Valoracion (HU13, T369): la puntuación de 1 a 5 que un cliente da a un producto, con un comentario
// opcional de hasta 500 caracteres. Java puro, sin Spring ni base de datos.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU13-CP01 y CP02, propuestos.
class ValoracionTest {

    @Test
    @DisplayName("HU13-CP01: crea una valoración con sus datos")
    void creaUnaValoracionConSusDatos() {
        Valoracion valoracion = new Valoracion("v-1", "p-1", "u-1", 4, "Buena calidad");

        assertEquals("v-1", valoracion.id());
        assertEquals("p-1", valoracion.productoId());
        assertEquals("u-1", valoracion.clienteId());
        assertEquals(4, valoracion.puntuacion());
        assertEquals("Buena calidad", valoracion.comentario());
    }

    @Test
    @DisplayName("HU13-CP01: una valoración nueva nace sin id")
    void unaValoracionNuevaNaceSinId() {
        Valoracion valoracion = Valoracion.nueva("p-1", "u-1", 5, "Muy recomendable");

        assertNull(valoracion.id());
        assertEquals("p-1", valoracion.productoId());
        assertEquals("u-1", valoracion.clienteId());
        assertEquals(5, valoracion.puntuacion());
        assertEquals("Muy recomendable", valoracion.comentario());
    }

    @Test
    @DisplayName("HU13-CP01: el comentario es opcional y se guarda sin espacios en los extremos")
    void elComentarioEsOpcionalYSeRecorta() {
        assertEquals("", Valoracion.nueva("p-1", "u-1", 3, null).comentario());
        assertEquals("Llegó a tiempo", Valoracion.nueva("p-1", "u-1", 3, "  Llegó a tiempo  ").comentario());
    }

    @ParameterizedTest(name = "puntuación {0}")
    @ValueSource(ints = {1, 5})
    @DisplayName("HU13-CP02: admite las puntuaciones de los extremos, 1 y 5")
    void admiteLasPuntuacionesDeLosExtremos(int puntuacion) {
        assertEquals(puntuacion, Valoracion.nueva("p-1", "u-1", puntuacion, "").puntuacion());
    }

    @ParameterizedTest(name = "puntuación {0}")
    @ValueSource(ints = {0, 6, -1})
    @DisplayName("HU13-CP02: rechaza una puntuación fuera de 1 a 5")
    void rechazaUnaPuntuacionFueraDeRango(int puntuacion) {
        assertThrows(ValoracionInvalidaException.class, () -> Valoracion.nueva("p-1", "u-1", puntuacion, ""));
    }
}
