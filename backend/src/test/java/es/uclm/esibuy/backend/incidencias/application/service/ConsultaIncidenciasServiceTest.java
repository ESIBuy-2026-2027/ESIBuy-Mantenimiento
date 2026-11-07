package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba. Consulta de las incidencias propias
// (HU44, T419): el autor sale de la sesión y la página pedida se valida antes de consultar.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP17 y HU44-CP18, propuestos.
class ConsultaIncidenciasServiceTest {

    private static final String EMAIL_CLIENTE = "cliente@esibuy.test";
    private static final String EMAIL_VENDEDOR = "vendedor@esibuy.test";
    private static final Incidencia INCIDENCIA = new Incidencia("i-1", "c-1", "Asunto", "Descripción",
            EstadoIncidencia.PENDIENTE, "", Instant.parse("2026-11-06T10:15:30Z"));
    private static final IncidenciaPage PAGINA = new IncidenciaPage(List.of(INCIDENCIA), 0, 10, 1);

    private final RepositorioFijo repositorio = new RepositorioFijo();
    private final ConsultaIncidenciasService servicio =
            new ConsultaIncidenciasService(repositorio, new AutoresFijos());

    // Devuelve siempre la misma página y apunta qué se le ha pedido
    private static class RepositorioFijo implements IncidenciaRepositoryPort {
        private int consultas;
        private String autorPedido;
        private int paginaPedida;
        private int tamanoPedido;

        @Override
        public Incidencia guardar(Incidencia incidencia) {
            throw new UnsupportedOperationException("No lo usa la consulta");
        }

        @Override
        public IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano) {
            consultas++;
            autorPedido = autorId;
            paginaPedida = pagina;
            tamanoPedido = tamano;
            return PAGINA;
        }

        @Override
        public IncidenciaPage buscarTodas(int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la consulta de las propias");
        }

        @Override
        public Optional<Incidencia> buscarPorId(String id) {
            throw new UnsupportedOperationException("No lo usa la consulta de las propias");
        }

        @Override
        public void eliminar(String id) {
            throw new UnsupportedOperationException("No lo usa la consulta de las propias");
        }
    }

    // Cada correo de la sesión tiene su id de usuario
    private static class AutoresFijos implements AutorEnSesionPort {
        private final Map<String, String> ids = Map.of(EMAIL_CLIENTE, "c-1", EMAIL_VENDEDOR, "v-1");

        @Override
        public String idPorCorreo(String email) {
            return ids.get(email);
        }
    }

    @Test
    @DisplayName("HU44-CP17: consulta las incidencias del cliente en sesión, con la página y el tamaño pedidos")
    void consultaLasIncidenciasDelClienteEnSesion() {
        IncidenciaPage devuelta = servicio.consultarPropias(EMAIL_CLIENTE, 2, 5);

        assertSame(PAGINA, devuelta);
        assertEquals("c-1", repositorio.autorPedido);
        assertEquals(2, repositorio.paginaPedida);
        assertEquals(5, repositorio.tamanoPedido);
    }

    @Test
    @DisplayName("HU44-CP17: un vendedor consulta las suyas: el autor es el vendedor en sesión")
    void consultaLasIncidenciasDelVendedorEnSesion() {
        IncidenciaPage devuelta = servicio.consultarPropias(EMAIL_VENDEDOR, 0, 10);

        assertSame(PAGINA, devuelta);
        assertEquals("v-1", repositorio.autorPedido);
    }

    @ParameterizedTest(name = "página {0}, tamaño {1}")
    @CsvSource({"0,1", "0,50", "7,10"})
    @DisplayName("HU44-CP18: admite la primera página y los tamaños de 1 a 50")
    void admiteLasPaginasValidas(int pagina, int tamano) {
        assertSame(PAGINA, servicio.consultarPropias(EMAIL_CLIENTE, pagina, tamano));
    }

    @ParameterizedTest(name = "página {0}, tamaño {1}")
    @CsvSource({"-1,10", "0,0", "0,-5", "0,51", "0,1000"})
    @DisplayName("HU44-CP18: rechaza una página negativa y un tamaño menor que 1 o mayor que 50, sin consultar nada")
    void rechazaUnaPaginaOUnTamanoFueraDeRango(int pagina, int tamano) {
        assertThrows(IncidenciaInvalidaException.class, () -> servicio.consultarPropias(EMAIL_CLIENTE, pagina, tamano));
        assertEquals(0, repositorio.consultas);
    }
}