package es.uclm.esibuy.backend.productos.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Descuento de un producto (HU31, T201). Reglas del contrato del frontend: activo + porcentaje mayor que 0 y
// no superior a 100; un descuento inactivo no lleva porcentaje.
// PENDIENTE: casos HU31-CP07 y HU31-CP08, propuestos a partir del formulario.
class DescuentoTest {

    @Test
    @DisplayName("HU31-CP07: un descuento activo con un porcentaje válido conserva sus valores")
    void conservaUnDescuentoActivoValido() {
        Descuento descuento = new Descuento(true, new BigDecimal("10"));

        assertTrue(descuento.activo());
        assertEquals(new BigDecimal("10"), descuento.porcentaje());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "100"})
    @DisplayName("HU31-CP07: acepta los porcentajes de los extremos válidos (0,01 y 100)")
    void aceptaLosPorcentajesLimite(String porcentaje) {
        assertDoesNotThrow(() -> new Descuento(true, new BigDecimal(porcentaje)));
    }

    @Test
    @DisplayName("HU31-CP07: sinDescuento devuelve un descuento inactivo y sin porcentaje")
    void sinDescuentoEsInactivoYSinPorcentaje() {
        Descuento descuento = Descuento.sinDescuento();

        assertFalse(descuento.activo());
        assertNull(descuento.porcentaje());
    }

    @Test
    @DisplayName("HU31-CP07: un descuento inactivo ignora el porcentaje que reciba")
    void unDescuentoInactivoIgnoraElPorcentaje() {
        Descuento descuento = new Descuento(false, new BigDecimal("30"));

        assertFalse(descuento.activo());
        assertNull(descuento.porcentaje());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "100.01", "150"})
    @DisplayName("HU31-CP08: rechaza un descuento activo con un porcentaje que no está entre 0 (excluido) y 100")
    void rechazaPorcentajesFueraDeRango(String porcentaje) {
        assertThrows(ProductoInvalidoException.class, () -> new Descuento(true, new BigDecimal(porcentaje)));
    }

    @Test
    @DisplayName("HU31-CP08: rechaza un descuento activo sin porcentaje")
    void rechazaUnDescuentoActivoSinPorcentaje() {
        assertThrows(ProductoInvalidoException.class, () -> new Descuento(true, null));
    }
}