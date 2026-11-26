package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoAbiertaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaResueltaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

// Sin Spring: el repositorio se sustituye por uno en memoria. Resolución de incidencias por el administrador (HU50):
// abrir una incidencia pendiente con su respuesta (T302) y cerrarla, que la elimina (T303).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU50-CP05 a CP07, CP10 y CP11, propuestos.
class ResolucionIncidenciasServiceTest {

    private static final Instant FECHA = Instant.parse("2026-11-06T10:15:30Z");

    // La hora del reloj del servicio y la de una resolución anterior
    private static final Instant AHORA = Instant.parse("2026-11-25T12:00:00Z");
    private static final Instant RESOLUCION_ANTERIOR = Instant.parse("2026-11-20T09:00:00Z");

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ResolucionIncidenciasService servicio = new ResolucionIncidenciasService(repositorio,
            Clock.fixed(AHORA, ZoneOffset.UTC));

    private static Incidencia incidencia(String id, EstadoIncidencia estado, String respuesta) {
        Instant fechaResolucion = estado == EstadoIncidencia.RESUELTA ? RESOLUCION_ANTERIOR : null;
        return new Incidencia(id, "u-1", "No me llega el pedido", "Lo pedí hace una semana", estado, respuesta, FECHA,
                fechaResolucion);
    }

    // Guarda las incidencias por id y apunta cuántas veces se ha guardado y
    // eliminado
    private static class RepositorioEnMemoria implements IncidenciaRepositoryPort {
        private final Map<String, Incidencia> incidencias = new HashMap<>();
        private int guardados;
        private int eliminados;
        private int resoluciones;
        // Lo que hace otro administrador justo antes de que se registre la resolución
        private Runnable antesDeRegistrar = () -> {
            // Por defecto, nadie se adelanta
        };

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
        public IncidenciaPage buscarPorEstado(EstadoIncidencia estado, int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la resolución");
        }

        @Override
        public IncidenciaPage buscarSupervisadas(EstadoIncidencia estado, String busqueda, String ordenarPor,
                String sentido, int pagina, int tamano) {
            throw new UnsupportedOperationException("No lo usa la resolución");
        }

        @Override
        public Optional<Incidencia> buscarPorId(String id) {
            return Optional.ofNullable(incidencias.get(id));
        }

        @Override
        public void eliminar(String id) {
            eliminados++;
            incidencias.remove(id);
        }

        // Como el adaptador real: solo registra la resolución de una incidencia que
        // sigue abierta
        @Override
        public boolean registrarResolucion(Incidencia resuelta) {
            antesDeRegistrar.run();
            Incidencia actual = incidencias.get(resuelta.id());
            if (actual == null || actual.estado() != EstadoIncidencia.ABIERTA) {
                return false;
            }
            resoluciones++;
            incidencias.put(resuelta.id(), resuelta);
            return true;
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

    // Cierre (T303): al cerrar una incidencia se elimina, esté pendiente o abierta

    @ParameterizedTest(name = "estado {0}")
    @EnumSource(EstadoIncidencia.class)
    @DisplayName("HU50-CP10: cerrar una incidencia la elimina, esté pendiente o abierta")
    void cerrarUnaIncidenciaLaElimina(EstadoIncidencia estado) {
        repositorio.preparar(incidencia("i-1", estado, estado == EstadoIncidencia.ABIERTA ? "Respuesta" : ""));
        repositorio.preparar(incidencia("i-2", EstadoIncidencia.PENDIENTE, ""));

        servicio.cerrar("i-1");

        assertTrue(repositorio.buscarPorId("i-1").isEmpty());
        assertTrue(repositorio.buscarPorId("i-2").isPresent());
        assertEquals(1, repositorio.eliminados);
    }

    @Test
    @DisplayName("HU50-CP11: cerrar una incidencia que no existe se rechaza sin eliminar nada")
    void cerrarUnaIncidenciaQueNoExisteSeRechaza() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.PENDIENTE, ""));

        assertThrows(IncidenciaNoEncontradaException.class, () -> servicio.cerrar("no-existe"));
        assertEquals(0, repositorio.eliminados);
        assertTrue(repositorio.buscarPorId("i-1").isPresent());
    }

    // Resolver (HU192, T513): la fecha de resolución sale del reloj del servicio y
    // no se sobrescribe

    @Test
    @DisplayName("HU192-CP06: resolver una incidencia abierta la registra resuelta con la hora del reloj")
    void resolverLaRegistraConLaHoraDelReloj() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.ABIERTA, "Respuesta"));

        Incidencia resuelta = servicio.resolver("i-1");

        assertEquals(EstadoIncidencia.RESUELTA, resuelta.estado());
        assertEquals(AHORA, resuelta.fechaResolucion());
        assertEquals(1, repositorio.resoluciones);
        assertEquals(resuelta, repositorio.buscarPorId("i-1").orElseThrow());
    }

    @Test
    @DisplayName("HU192-CP07: resolver una incidencia que no existe se rechaza sin registrar nada")
    void resolverUnaIncidenciaQueNoExisteSeRechaza() {
        assertThrows(IncidenciaNoEncontradaException.class, () -> servicio.resolver("no-existe"));
        assertEquals(0, repositorio.resoluciones);
        assertEquals(0, repositorio.guardados);
    }

    @Test
    @DisplayName("HU192-CP08: resolver una incidencia ya resuelta se rechaza y conserva la fecha original")
    void resolverUnaYaResueltaConservaLaFechaOriginal() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.RESUELTA, "Respuesta"));

        assertThrows(IncidenciaYaResueltaException.class, () -> servicio.resolver("i-1"));
        assertEquals(0, repositorio.resoluciones);
        assertEquals(RESOLUCION_ANTERIOR, repositorio.buscarPorId("i-1").orElseThrow().fechaResolucion());
    }

    @Test
    @DisplayName("HU192-CP08: resolver una incidencia pendiente se rechaza y sigue pendiente")
    void resolverUnaPendienteSeRechaza() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.PENDIENTE, ""));

        assertThrows(IncidenciaNoAbiertaException.class, () -> servicio.resolver("i-1"));
        assertEquals(0, repositorio.resoluciones);
        assertNull(repositorio.buscarPorId("i-1").orElseThrow().fechaResolucion());
    }

    @Test
    @DisplayName("HU192-CP09: si otro administrador la resuelve a la vez, se rechaza y queda la primera fecha")
    void siOtroLaResuelveALaVezSeRechaza() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.ABIERTA, "Respuesta"));
        repositorio.antesDeRegistrar = () -> repositorio
                .preparar(incidencia("i-1", EstadoIncidencia.RESUELTA, "Respuesta"));

        assertThrows(IncidenciaYaResueltaException.class, () -> servicio.resolver("i-1"));
        assertEquals(RESOLUCION_ANTERIOR, repositorio.buscarPorId("i-1").orElseThrow().fechaResolucion());
    }

    @Test
    @DisplayName("HU192-CP09: si otro administrador la cierra a la vez, se responde que no existe y no reaparece")
    void siOtroLaCierraALaVezNoReaparece() {
        repositorio.preparar(incidencia("i-1", EstadoIncidencia.ABIERTA, "Respuesta"));
        repositorio.antesDeRegistrar = () -> repositorio.eliminar("i-1");

        assertThrows(IncidenciaNoEncontradaException.class, () -> servicio.resolver("i-1"));
        assertTrue(repositorio.buscarPorId("i-1").isEmpty());
        assertEquals(0, repositorio.guardados);
    }
}
