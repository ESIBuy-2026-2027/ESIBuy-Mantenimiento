package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Consulta de las valoraciones de un producto (HU13, T371). Sin Spring: el repositorio se sustituye por un doble de
// prueba (clase interna de este test). Sin orden garantizado: no hay fecha (decisión del equipo).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP15, propuesto.
class ConsultaValoracionesServiceTest {

    private static final Valoracion DE_C1 = new Valoracion("v-1", "p-1", "c-1", 4, "Buena calidad");
    private static final Valoracion DE_C2 = new Valoracion("v-2", "p-1", "c-2", 2, "");
    private static final Valoracion DE_OTRO_PRODUCTO = new Valoracion("v-3", "p-2", "c-1", 5, "Perfecto");

    private final ValoracionesGuardadas valoraciones = new ValoracionesGuardadas();
    private final ConsultaValoracionesService servicio = new ConsultaValoracionesService(valoraciones);

    @Test
    @DisplayName("HU13-CP15: consultar un producto devuelve todas sus valoraciones y solo las suyas")
    void devuelveLasValoracionesDelProducto() {
        valoraciones.guardadas.addAll(List.of(DE_C1, DE_C2, DE_OTRO_PRODUCTO));

        assertEquals(List.of(DE_C1, DE_C2), servicio.consultarPorProducto("p-1"));
    }

    @Test
    @DisplayName("HU13-CP15: un producto sin valoraciones devuelve una lista vacía")
    void unProductoSinValoracionesDevuelveUnaListaVacia() {
        valoraciones.guardadas.add(DE_OTRO_PRODUCTO);

        assertTrue(servicio.consultarPorProducto("p-1").isEmpty());
    }

    // Valoraciones ya guardadas; consultar no guarda nada
    private static final class ValoracionesGuardadas implements ValoracionRepositoryPort {
        private final List<Valoracion> guardadas = new ArrayList<>();

        @Override
        public Valoracion guardar(Valoracion valoracion) {
            throw new UnsupportedOperationException("Consultar valoraciones no guarda");
        }

        @Override
        public boolean existePorClienteYProducto(String clienteId, String productoId) {
            throw new UnsupportedOperationException("Consultar valoraciones no comprueba duplicados");
        }

        @Override
        public List<Valoracion> buscarPorProducto(String productoId) {
            return guardadas.stream().filter(v -> v.productoId().equals(productoId)).toList();
        }

        @Override
        public void eliminarPorProducto(String productoId) {
            throw new UnsupportedOperationException("Consultar valoraciones no elimina");
        }
    }
}
