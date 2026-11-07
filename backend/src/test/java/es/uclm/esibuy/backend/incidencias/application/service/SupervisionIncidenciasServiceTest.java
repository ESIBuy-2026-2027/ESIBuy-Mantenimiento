package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.uclm.esibuy.backend.incidencias.application.port.out.AutoresPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisada;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisadaPage;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba. Supervisión de incidencias por el
// administrador (HU49, T307): ve las de todos los autores, cada una con el correo y el rol de quien la reportó.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU49-CP05 a CP08, propuestos.
class SupervisionIncidenciasServiceTest {

    private static final Instant FECHA = Instant.parse("2026-11-06T10:15:30Z");
    private static final AutorIncidencia CLIENTE = new AutorIncidencia("cliente@esibuy.test", "CLIENTE");
    private static final AutorIncidencia VENDEDOR = new AutorIncidencia("vendedor@esibuy.test", "VENDEDOR");

    private final RepositorioFijo repositorio = new RepositorioFijo();
    private final AutoresFijos autores = new AutoresFijos();
    private final SupervisionIncidenciasService servicio = new SupervisionIncidenciasService(repositorio, autores);

    private static Incidencia incidencia(String id, String autorId) {
        return new Incidencia(id, autorId, "Asunto " + id, "Descripción", EstadoIncidencia.PENDIENTE, "", FECHA);
    }

    // Devuelve la página que se le prepare y apunta qué se le ha pedido
    private static class RepositorioFijo implements IncidenciaRepositoryPort {
        private IncidenciaPage pagina = new IncidenciaPage(List.of(), 0, 10, 0);
        private int consultas;
        private int paginaPedida;
        private int tamanoPedido;

        @Override
        public Incidencia guardar(Incidencia incidencia) {
            throw new UnsupportedOperationException("No lo usa la supervisión");
        }

        @Override
        public IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la supervisión");
        }

        @Override
        public IncidenciaPage buscarTodas(int pagina, int tamano) {
            consultas++;
            paginaPedida = pagina;
            tamanoPedido = tamano;
            return this.pagina;
        }

        @Override
        public Optional<Incidencia> buscarPorId(String id) {
            throw new UnsupportedOperationException("No lo usa la supervisión");
        }
    }

    // c-1 es un cliente y v-1 un vendedor; cualquier otro id es una cuenta eliminada. Apunta a quién se ha buscado.
    private static class AutoresFijos implements AutoresPort {
        private final Map<String, AutorIncidencia> existentes = Map.of("c-1", CLIENTE, "v-1", VENDEDOR);
        private final List<String> buscados = new ArrayList<>();

        @Override
        public Optional<AutorIncidencia> buscarAutor(String autorId) {
            buscados.add(autorId);
            return Optional.ofNullable(existentes.get(autorId));
        }
    }

    @Test
    @DisplayName("HU49-CP05: devuelve la página pedida con el autor de cada incidencia")
    void devuelveLaPaginaPedidaConElAutorDeCadaIncidencia() {
        Incidencia delCliente = incidencia("i-1", "c-1");
        Incidencia delVendedor = incidencia("i-2", "v-1");
        repositorio.pagina = new IncidenciaPage(List.of(delCliente, delVendedor), 2, 5, 12);

        IncidenciaSupervisadaPage devuelta = servicio.consultarTodas(2, 5);

        assertEquals(2, repositorio.paginaPedida);
        assertEquals(5, repositorio.tamanoPedido);
        assertEquals(List.of(new IncidenciaSupervisada(delCliente, CLIENTE),
                new IncidenciaSupervisada(delVendedor, VENDEDOR)), devuelta.incidencias());
        assertEquals(2, devuelta.pagina());
        assertEquals(5, devuelta.tamano());
        assertEquals(12, devuelta.totalElementos());
    }

    @Test
    @DisplayName("HU49-CP06: una incidencia cuyo autor ya no existe se devuelve sin autor y no hace fallar la consulta")
    void unaIncidenciaDeUnaCuentaEliminadaSeDevuelveSinAutor() {
        Incidencia huerfana = incidencia("i-1", "eliminado");
        Incidencia delCliente = incidencia("i-2", "c-1");
        repositorio.pagina = new IncidenciaPage(List.of(huerfana, delCliente), 0, 10, 2);

        List<IncidenciaSupervisada> incidencias = servicio.consultarTodas(0, 10).incidencias();

        assertSame(huerfana, incidencias.get(0).incidencia());
        assertNull(incidencias.get(0).autor());
        assertEquals(CLIENTE, incidencias.get(1).autor());
    }

    @Test
    @DisplayName("HU49-CP07: busca cada autor una sola vez, aunque tenga varias incidencias en la página")
    void buscaCadaAutorUnaSolaVez() {
        repositorio.pagina = new IncidenciaPage(List.of(incidencia("i-1", "c-1"), incidencia("i-2", "v-1"),
                incidencia("i-3", "c-1"), incidencia("i-4", "eliminado"), incidencia("i-5", "eliminado")), 0, 10, 5);

        List<IncidenciaSupervisada> incidencias = servicio.consultarTodas(0, 10).incidencias();

        assertEquals(List.of("c-1", "v-1", "eliminado"), autores.buscados);
        assertEquals(CLIENTE, incidencias.get(2).autor());
        assertNull(incidencias.get(4).autor());
    }

    @Test
    @DisplayName("HU49-CP07: una página vacía no busca ningún autor")
    void unaPaginaVaciaNoBuscaAutores() {
        IncidenciaSupervisadaPage devuelta = servicio.consultarTodas(0, 10);

        assertEquals(List.of(), devuelta.incidencias());
        assertEquals(0, devuelta.totalElementos());
        assertEquals(List.of(), autores.buscados);
    }

    @ParameterizedTest(name = "página {0}, tamaño {1}")
    @CsvSource({"0,1", "0,50", "7,10"})
    @DisplayName("HU49-CP08: admite la primera página y los tamaños de 1 a 50")
    void admiteLasPaginasValidas(int pagina, int tamano) {
        servicio.consultarTodas(pagina, tamano);

        assertEquals(1, repositorio.consultas);
    }

    @ParameterizedTest(name = "página {0}, tamaño {1}")
    @CsvSource({"-1,10", "0,0", "0,-5", "0,51", "0,1000"})
    @DisplayName("HU49-CP08: rechaza una página negativa y un tamaño menor que 1 o mayor que 50, sin consultar nada")
    void rechazaUnaPaginaOUnTamanoFueraDeRango(int pagina, int tamano) {
        assertThrows(IncidenciaInvalidaException.class, () -> servicio.consultarTodas(pagina, tamano));
        assertEquals(0, repositorio.consultas);
        assertEquals(List.of(), autores.buscados);
    }
}
