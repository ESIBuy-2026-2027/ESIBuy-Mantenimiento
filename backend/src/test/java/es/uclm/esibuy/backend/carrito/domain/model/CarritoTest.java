package es.uclm.esibuy.backend.carrito.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException;
import es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException;
import es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CarritoTest {

    private static final String CLIENTE = "cliente-1";
    private static final Instant AYER = Instant.parse("2026-10-28T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-10-29T10:00:00Z");

    private static Carrito carritoCon(String productoId, int cantidad) {
        Carrito carrito = Carrito.vacio(CLIENTE);
        carrito.anadir(productoId, cantidad, 100, AYER);
        return carrito;
    }

    @Test
    @DisplayName("HU9-CP10: añadir un producto nuevo crea su línea con la cantidad y la fecha")
    void anadirUnProductoNuevoCreaSuLinea() {
        Carrito carrito = Carrito.vacio(CLIENTE);

        carrito.anadir("p-1", 2, 10, HOY);

        assertEquals(1, carrito.getLineas().size());
        LineaCarrito linea = carrito.getLineas().get(0);
        assertEquals("p-1", linea.productoId());
        assertEquals(2, linea.cantidad());
        assertEquals(HOY, linea.addedAt());
        assertEquals(HOY, linea.updatedAt());
    }

    @Test
    @DisplayName("HU9-CP10: añadir un producto que ya está suma las unidades y conserva cuándo se añadió")
    void anadirUnProductoQueYaEstaSumaLasUnidades() {
        Carrito carrito = carritoCon("p-1", 2);

        carrito.anadir("p-1", 3, 10, HOY);

        assertEquals(1, carrito.getLineas().size());
        LineaCarrito linea = carrito.getLineas().get(0);
        assertEquals(5, linea.cantidad());
        assertEquals(AYER, linea.addedAt());
        assertEquals(HOY, linea.updatedAt());
    }

    @Test
    @DisplayName("HU9-CP11: no deja añadir más unidades de las que hay en stock, contando las que ya están")
    void noDejaAnadirMasDelStock() {
        Carrito carrito = Carrito.vacio(CLIENTE);
        carrito.anadir("p-1", 4, 5, AYER);

        assertThrows(StockInsuficienteException.class, () -> carrito.anadir("p-1", 2, 5, HOY));
        assertEquals(4, carrito.getLineas().get(0).cantidad());
    }

    @Test
    @DisplayName("HU9-CP11: no deja fijar una cantidad mayor que el stock")
    void noDejaFijarMasDelStock() {
        Carrito carrito = carritoCon("p-1", 1);

        assertThrows(StockInsuficienteException.class, () -> carrito.fijarCantidad("p-1", 6, 5, HOY));
        assertEquals(1, carrito.getLineas().get(0).cantidad());
    }

    @Test
    @DisplayName("HU9-CP13: fijar la cantidad la sustituye en lugar de sumarla")
    void fijarLaCantidadLaSustituye() {
        Carrito carrito = carritoCon("p-1", 2);

        carrito.fijarCantidad("p-1", 7, 10, HOY);

        assertEquals(7, carrito.getLineas().get(0).cantidad());
        assertEquals(HOY, carrito.getLineas().get(0).updatedAt());
    }

    @Test
    @DisplayName("HU9-CP13: una cantidad menor que 1 no se acepta ni al añadir ni al fijarla")
    void unaCantidadMenorQueUnoNoSeAcepta() {
        Carrito carrito = carritoCon("p-1", 2);

        assertThrows(CantidadInvalidaException.class, () -> carrito.anadir("p-2", 0, 10, HOY));
        assertThrows(CantidadInvalidaException.class, () -> carrito.fijarCantidad("p-1", -1, 10, HOY));
        assertEquals(1, carrito.getLineas().size());
    }

    @Test
    @DisplayName("HU9-CP13: no se puede fijar la cantidad de un producto que no está en el carrito")
    void noSePuedeFijarLaCantidadDeUnProductoQueNoEsta() {
        Carrito carrito = carritoCon("p-1", 2);

        assertThrows(LineaCarritoNoEncontradaException.class, () -> carrito.fijarCantidad("p-2", 1, 10, HOY));
    }

    @Test
    @DisplayName("HU9-CP14: quitar un producto deja los demás; quitar uno que no está no cambia nada")
    void quitarUnProductoDejaLosDemas() {
        Carrito carrito = carritoCon("p-1", 2);
        carrito.anadir("p-2", 1, 10, HOY);

        carrito.quitar("p-1");
        carrito.quitar("p-no-esta");

        assertEquals(1, carrito.getLineas().size());
        assertEquals("p-2", carrito.getLineas().get(0).productoId());
    }

    @Test
    @DisplayName("HU9-CP14: vaciar quita todas las líneas")
    void vaciarQuitaTodasLasLineas() {
        Carrito carrito = carritoCon("p-1", 2);
        carrito.anadir("p-2", 1, 10, HOY);

        carrito.vaciar();

        assertTrue(carrito.getLineas().isEmpty());
        assertEquals(CLIENTE, carrito.getClienteId());
    }

    @Test
    @DisplayName("HU9-CP18: la limpieza quita los productos que ya no se pueden comprar o sin stock")
    void laLimpiezaQuitaLosProductosQueNoSePuedenComprar() {
        Carrito carrito = carritoCon("p-1", 2);
        carrito.anadir("p-retirado", 1, 10, AYER);
        carrito.anadir("p-agotado", 1, 10, AYER);

        boolean cambiado = carrito.limpiar(Map.of("p-1", 10, "p-agotado", 0), HOY);

        assertTrue(cambiado);
        assertEquals(1, carrito.getLineas().size());
        assertEquals("p-1", carrito.getLineas().get(0).productoId());
    }

    @Test
    @DisplayName("HU9-CP18: la limpieza ajusta la cantidad al stock si ha bajado")
    void laLimpiezaAjustaLaCantidadAlStock() {
        Carrito carrito = carritoCon("p-1", 8);

        boolean cambiado = carrito.limpiar(Map.of("p-1", 3), HOY);

        assertTrue(cambiado);
        assertEquals(3, carrito.getLineas().get(0).cantidad());
        assertEquals(HOY, carrito.getLineas().get(0).updatedAt());
    }

    @Test
    @DisplayName("HU9-CP18: si todo se puede comprar, la limpieza no cambia nada")
    void siTodoSePuedeComprarLaLimpiezaNoCambiaNada() {
        Carrito carrito = carritoCon("p-1", 2);

        boolean cambiado = carrito.limpiar(Map.of("p-1", 10), HOY);

        assertFalse(cambiado);
        assertEquals(2, carrito.getLineas().get(0).cantidad());
        assertEquals(AYER, carrito.getLineas().get(0).updatedAt());
    }
}
