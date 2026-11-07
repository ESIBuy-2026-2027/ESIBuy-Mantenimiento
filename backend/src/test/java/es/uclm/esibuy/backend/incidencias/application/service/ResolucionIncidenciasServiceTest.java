package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

// Sin Spring: el repositorio se sustituye por uno en memoria. Resolución de incidencias por el administrador (HU50):
// abrir una incidencia pendiente con su respuesta (T302).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU50-CP05 a CP07, propuestos.
class ResolucionIncidenciasServiceTest {

    private static final Instant FECHA = Instant.parse("2026-11-06T10:15:30Z");

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ResolucionIncidenciasService servicio = new ResolucionIncidenciasService(repositorio);

    private static Incidencia incidencia(String id, EstadoIncidencia estado, String respuesta) {
        return new Incidencia(id, "u-1", "No me llega el pedido", "Lo pedí hace una semana", estado, respuesta, FECHA);
    }

    // Guarda las incidencias por id y apunta cuántas veces se ha guardado
    private static class RepositorioEnMemoria implements IncidenciaRepositoryPort {
        private final Map<String, Incidencia> incidencias = new HashMap<>();
        private int guardados;

        void preparar(Incidencia incidencia) {
            incidencias.put(incidencia.id(), incidencia);
        }

        @Override
        public Incidencia guardar(Incidencia incidencia) {
            guardados++;
            incidencias.put(incidencia.id(), incidencia);
            return incidencia;
        }

        @Override
        public IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la resolución");
        }

        @Override
        public IncidenciaPage buscarTodas(int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la resolución");
        }

        @Override
        public Optional<Incidencia> buscarPorId(String id) {
            return Optional.ofNullable(incidencias.get(id));
        }
    }

    @Test
    @DisplayName("HU50-CP05: abrir una incidencia pendiente la guarda abierta con la respuesta y la devuelve")
    void abrirUnaIncidenciaPendienteLaGuardaAbiertaConLaRespuesta() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.PENDIENTE, ""));

        Incidencia abierta = servicio.abrir("i-1", "Lo estamos revisando");

        assertEquals(EstadoIncidencia.ABIERTA, abierta.estado());
        assertEquals("Lo estamos revisando", abierta.respuesta());
        assertEquals(1, repositorio.guardados);
        Incidencia guardada = repositorio.buscarPorId("i-1").orElseThrow();
        assertEquals(EstadoIncidencia.ABIERTA, guardada.estado());
        assertEquals("Lo estamos revisando", guardada.respuesta());
    }

    @Test
    @DisplayName("HU50-CP06: abrir una incidencia que no existe se rechaza sin guardar nada")
    void abrirUnaIncidenciaQueNoExisteSeRechaza() {
        assertThrows(IncidenciaNoEncontradaException.class, () -> servicio.abrir("no-existe", "Respuesta"));
        assertEquals(0, repositorio.guardados);
    }

    @Test
    @DisplayName("HU50-CP07: abrir una incidencia ya abierta se rechaza y conserva la respuesta que tenía")
    void abrirUnaIncidenciaYaAbiertaSeRechazaYConservaLaRespuesta() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.ABIERTA, "Primera respuesta"));

        assertThrows(IncidenciaYaAbiertaException.class, () -> servicio.abrir("i-1", "Otra respuesta"));
        assertEquals(0, repositorio.guardados);
        assertEquals("Primera respuesta", repositorio.buscarPorId("i-1").orElseThrow().respuesta());
    }

    @Test
    @DisplayName("HU50-CP07: abrir sin respuesta se rechaza y la incidencia sigue pendiente")
    void abrirSinRespuestaSeRechazaYSiguePendiente() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.PENDIENTE, ""));

        assertThrows(IncidenciaInvalidaException.class, () -> servicio.abrir("i-1", "  "));
        assertEquals(0, repositorio.guardados);
        assertTrue(repositorio.buscarPorId("i-1").map(i -> i.estado() == EstadoIncidencia.PENDIENTE).orElseThrow());
    }
}
