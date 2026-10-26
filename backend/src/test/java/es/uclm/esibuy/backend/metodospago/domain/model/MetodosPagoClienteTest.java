package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.metodospago.domain.exception.LimiteMetodosPagoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoNoEncontradoException;
import es.uclm.esibuy.backend.metodospago.domain.exception.NuevoPredeterminadoObligatorioException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetodosPagoClienteTest {

    private static final String CLIENTE = "cliente-1";

    // Método aún no guardado, como el que llega al dar de alta uno
    private static MetodoPago nuevoMetodo() {
        return MetodoPago.nuevo(CLIENTE, TipoMetodoPago.TARJETA, "•••• 4444", "bloque-cifrado");
    }

    // Método ya guardado, como los que se cargan del repositorio
    private static MetodoPago metodoGuardado(String id) {
        return new MetodoPago(id, CLIENTE, TipoMetodoPago.TARJETA, "•••• 4444", "bloque-cifrado");
    }

    private static MetodoPago metodoGuardadoPredeterminado(String id) {
        MetodoPago metodo = metodoGuardado(id);
        metodo.marcarPredeterminado();
        return metodo;
    }

    @Test
    @DisplayName("HU6-CP08: permite añadir métodos hasta el límite de cinco")
    void anadeMetodosHastaElLimite() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            metodos.anadir(nuevoMetodo());
        }

        assertEquals(5, metodos.getMetodos().size());
    }

    @Test
    @DisplayName("HU6-CP08: rechaza añadir un sexto método")
    void rechazaSextoMetodo() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            metodos.anadir(nuevoMetodo());
        }

        // Frontera: 5 caben, el sexto no; y no se queda añadido
        assertThrows(LimiteMetodosPagoException.class, () -> metodos.anadir(nuevoMetodo()));
        assertEquals(5, metodos.getMetodos().size());
    }

    @Test
    @DisplayName("HU6-CP09: el primer método añadido queda como predeterminado")
    void primerMetodoQuedaPredeterminado() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(new ArrayList<>());
        MetodoPago primero = nuevoMetodo();

        metodos.anadir(primero);

        assertTrue(primero.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: un método añadido después no cambia el predeterminado")
    void metodoPosteriorNoCambiaElPredeterminado() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(new ArrayList<>());
        MetodoPago primero = nuevoMetodo();
        MetodoPago segundo = nuevoMetodo();
        metodos.anadir(primero);

        metodos.anadir(segundo);

        assertTrue(primero.isPredeterminado());
        assertFalse(segundo.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: marcar otro método deja solo uno como predeterminado")
    void marcarOtroLeDejaSoloUnPredeterminado() {
        MetodoPago actual = metodoGuardadoPredeterminado("m1");
        MetodoPago otro = metodoGuardado("m2");
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(actual, otro));

        metodos.marcarPredeterminado("m2");

        assertTrue(otro.isPredeterminado());
        assertFalse(actual.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: rechaza marcar un método que no es del cliente")
    void rechazaMarcarMetodoInexistente() {
        MetodoPago actual = metodoGuardadoPredeterminado("m1");
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(actual));

        assertThrows(MetodoPagoNoEncontradoException.class, () -> metodos.marcarPredeterminado("de-otro-cliente"));
        // Un id erróneo no deja al cliente sin predeterminado
        assertTrue(actual.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar un método no predeterminado conserva el predeterminado")
    void eliminaMetodoNoPredeterminado() {
        MetodoPago predeterminado = metodoGuardadoPredeterminado("m1");
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(predeterminado, metodoGuardado("m2")));

        // Sin elegir sustituto: el predeterminado no cambia
        metodos.eliminar("m2", null);

        assertEquals(List.of(predeterminado), metodos.getMetodos());
        assertTrue(predeterminado.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar el predeterminado exige indicar cuál pasa a serlo")
    void eliminarPredeterminadoExigeElegirOtro() {
        MetodoPago predeterminado = metodoGuardadoPredeterminado("m1");
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(predeterminado, metodoGuardado("m2")));

        assertThrows(NuevoPredeterminadoObligatorioException.class, () -> metodos.eliminar("m1", null));
        // No se elimina nada si falta la elección
        assertEquals(2, metodos.getMetodos().size());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar el predeterminado y elegir otro lo deja como predeterminado")
    void eliminarPredeterminadoYElegirOtro() {
        MetodoPago sustituto = metodoGuardado("m3");
        MetodosPagoCliente metodos = new MetodosPagoCliente(
                List.of(metodoGuardadoPredeterminado("m1"), metodoGuardado("m2"), sustituto));

        metodos.eliminar("m1", "m3");

        assertEquals(2, metodos.getMetodos().size());
        assertTrue(sustituto.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: rechaza elegir como sustituto el mismo método que se elimina")
    void rechazaElegirComoSustitutoElMetodoEliminado() {
        MetodoPago predeterminado = metodoGuardadoPredeterminado("m1");
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(predeterminado, metodoGuardado("m2")));

        // Elegirse a sí mismo equivale a no elegir: el cliente se quedaría sin predeterminado teniendo otros
        assertThrows(NuevoPredeterminadoObligatorioException.class, () -> metodos.eliminar("m1", "m1"));
        assertEquals(2, metodos.getMetodos().size());
        assertTrue(predeterminado.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP10: eliminar el único método deja la lista vacía sin exigir elección")
    void eliminarElUnicoMetodoDejaListaVacia() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(metodoGuardadoPredeterminado("m1")));

        // Es el predeterminado, pero no queda ningún otro que elegir
        metodos.eliminar("m1", null);

        assertTrue(metodos.getMetodos().isEmpty());
    }

    @Test
    @DisplayName("HU6-CP10: rechaza eliminar un método que no es del cliente")
    void rechazaEliminarMetodoInexistente() {
        MetodosPagoCliente metodos = new MetodosPagoCliente(List.of(metodoGuardadoPredeterminado("m1")));

        assertThrows(MetodoPagoNoEncontradoException.class, () -> metodos.eliminar("de-otro-cliente", null));
        assertEquals(1, metodos.getMetodos().size());
    }
}
