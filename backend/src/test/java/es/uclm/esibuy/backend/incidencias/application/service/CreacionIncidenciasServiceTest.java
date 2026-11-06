package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba. Servicio de creación de incidencias
// (HU44, T293): el autor sale de la sesión, la incidencia nace pendiente y la fecha sale del reloj.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP08 y HU44-CP09, propuestos.
class CreacionIncidenciasServiceTest {

    private static final String EMAIL_CLIENTE = "cliente@esibuy.test";
    private static final String EMAIL_VENDEDOR = "vendedor@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final CreacionIncidenciasService servicio =
            new CreacionIncidenciasService(repositorio, new AutoresFijos(), RELOJ);

    private static class RepositorioEnMemoria implements IncidenciaRepositoryPort {
        private final List<Incidencia> guardadas = new ArrayList<>();

        @Override
        public Incidencia guardar(Incidencia incidencia) {
            Incidencia conId = new Incidencia("i-" + (guardadas.size() + 1), incidencia.autorId(),
                    incidencia.asunto(), incidencia.descripcion(), incidencia.estado(), incidencia.respuesta(),
                    incidencia.fechaCreacion());
            guardadas.add(conId);
            return conId;
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
    @DisplayName("HU44-CP08: crea la incidencia del cliente en sesión, pendiente y con la fecha del reloj")
    void creaLaIncidenciaDelClienteEnSesion() {
        Incidencia creada = servicio.crear(
                new CrearIncidenciaCommand(EMAIL_CLIENTE, "No me llega el pedido", "Lo pedí hace una semana"));

        assertNotNull(creada);
        assertEquals("i-1", creada.id());
        assertEquals("c-1", creada.autorId());
        assertEquals("No me llega el pedido", creada.asunto());
        assertEquals("Lo pedí hace una semana", creada.descripcion());
        assertEquals(EstadoIncidencia.PENDIENTE, creada.estado());
        assertEquals("", creada.respuesta());
        assertEquals(AHORA, creada.fechaCreacion());
    }

    @Test
    @DisplayName("HU44-CP08: un vendedor también crea incidencias: el autor es el vendedor en sesión")
    void creaLaIncidenciaDelVendedorEnSesion() {
        Incidencia creada = servicio.crear(new CrearIncidenciaCommand(EMAIL_VENDEDOR, "Error en mi catálogo", "No sale"));

        assertNotNull(creada);
        assertEquals("v-1", creada.autorId());
    }

    @Test
    @DisplayName("HU44-CP08: guarda una sola incidencia y devuelve la guardada")
    void guardaUnaSolaIncidencia() {
        Incidencia creada = servicio.crear(new CrearIncidenciaCommand(EMAIL_CLIENTE, "Asunto", "Descripción"));

        assertEquals(List.of(creada), repositorio.guardadas);
    }

    @Test
    @DisplayName("HU44-CP09: el asunto y la descripción se guardan sin espacios en los extremos")
    void recortaElAsuntoYLaDescripcion() {
        Incidencia creada = servicio.crear(new CrearIncidenciaCommand(EMAIL_CLIENTE, "  Asunto  ", "  Descripción  "));

        assertNotNull(creada);
        assertEquals("Asunto", creada.asunto());
        assertEquals("Descripción", creada.descripcion());
    }

    @Test
    @DisplayName("HU44-CP09: un asunto vacío se rechaza y no se guarda nada")
    void rechazaUnAsuntoVacio() {
        CrearIncidenciaCommand comando = new CrearIncidenciaCommand(EMAIL_CLIENTE, "  ", "Descripción");

        assertThrows(IncidenciaInvalidaException.class, () -> servicio.crear(comando));
        assertTrue(repositorio.guardadas.isEmpty());
    }

    @Test
    @DisplayName("HU44-CP09: una descripción vacía se rechaza y no se guarda nada")
    void rechazaUnaDescripcionVacia() {
        CrearIncidenciaCommand comando = new CrearIncidenciaCommand(EMAIL_CLIENTE, "Asunto", "");

        assertThrows(IncidenciaInvalidaException.class, () -> servicio.crear(comando));
        assertTrue(repositorio.guardadas.isEmpty());
    }
}