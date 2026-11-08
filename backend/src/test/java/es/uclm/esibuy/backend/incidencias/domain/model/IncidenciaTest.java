package es.uclm.esibuy.backend.incidencias.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException;

// Entidad Incidencia (HU44, T291): lo que un cliente o un vendedor reporta desde su perfil para que un administrador
// la revise. El asunto (hasta 100 caracteres) y la descripción (hasta 1000) son obligatorios y se guardan sin espacios
// en los extremos. Nace pendiente y sin respuesta. Java puro, sin Spring ni base de datos.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP01 a CP05, propuestos.
class IncidenciaTest {

    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");

    @Test
    @DisplayName("HU44-CP01: crea una incidencia con sus datos")
    void creaUnaIncidenciaConSusDatos() {
        Incidencia incidencia = new Incidencia("i-1", "u-1", "No me llega el pedido", "Lo pedí hace una semana",
                EstadoIncidencia.ABIERTA, "Lo estamos revisando", AHORA);

        assertEquals("i-1", incidencia.id());
        assertEquals("u-1", incidencia.autorId());
        assertEquals("No me llega el pedido", incidencia.asunto());
        assertEquals("Lo pedí hace una semana", incidencia.descripcion());
        assertEquals(EstadoIncidencia.ABIERTA, incidencia.estado());
        assertEquals("Lo estamos revisando", incidencia.respuesta());
        assertEquals(AHORA, incidencia.fechaCreacion());
    }

    @Test
    @DisplayName("HU44-CP01: una incidencia nueva nace pendiente, sin id y sin respuesta")
    void unaIncidenciaNuevaNacePendienteSinIdYSinRespuesta() {
        Incidencia incidencia = Incidencia.nueva("u-1", "No me llega el pedido", "Lo pedí hace una semana", AHORA);

        assertNull(incidencia.id());
        assertEquals("u-1", incidencia.autorId());
        assertEquals("No me llega el pedido", incidencia.asunto());
        assertEquals("Lo pedí hace una semana", incidencia.descripcion());
        assertEquals(EstadoIncidencia.PENDIENTE, incidencia.estado());
        assertEquals("", incidencia.respuesta());
        assertEquals(AHORA, incidencia.fechaCreacion());
    }

    @ParameterizedTest(name = "asunto \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("HU44-CP02: el asunto es obligatorio")
    void elAsuntoEsObligatorio(String asunto) {
        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva("u-1", asunto, "Descripción", AHORA));
    }

    @ParameterizedTest(name = "descripción \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("HU44-CP02: la descripción es obligatoria")
    void laDescripcionEsObligatoria(String descripcion) {
        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva("u-1", "Asunto", descripcion, AHORA));
    }

    @Test
    @DisplayName("HU44-CP03: el asunto, la descripción y la respuesta se guardan sin espacios en los extremos")
    void seGuardanSinEspaciosEnLosExtremos() {
        Incidencia incidencia = new Incidencia(null, "u-1", "  Asunto  ", "  Descripción  ", EstadoIncidencia.ABIERTA,
                "  Respuesta  ", AHORA);

        assertEquals("Asunto", incidencia.asunto());
        assertEquals("Descripción", incidencia.descripcion());
        assertEquals("Respuesta", incidencia.respuesta());
    }

    @Test
    @DisplayName("HU44-CP03: la respuesta es opcional: si falta, queda vacía")
    void laRespuestaEsOpcional() {
        assertEquals("", new Incidencia(null, "u-1", "Asunto", "Descripción", EstadoIncidencia.PENDIENTE, null, AHORA)
                .respuesta());
    }

    @Test
    @DisplayName("HU44-CP04: el asunto admite hasta 100 caracteres y la descripción hasta 1000")
    void admiteLosMaximos() {
        String cien = "a".repeat(100);
        String mil = "b".repeat(1000);

        Incidencia incidencia = Incidencia.nueva("u-1", cien, mil, AHORA);

        assertEquals(cien, incidencia.asunto());
        assertEquals(mil, incidencia.descripcion());
    }

    @Test
    @DisplayName("HU44-CP04: no cuentan los espacios de los extremos para el máximo")
    void losEspaciosDeLosExtremosNoCuentanParaElMaximo() {
        String cien = "a".repeat(100);

        assertEquals(cien, Incidencia.nueva("u-1", "  " + cien + "  ", "Descripción", AHORA).asunto());
    }

    @Test
    @DisplayName("HU44-CP04: rechaza un asunto de más de 100 caracteres")
    void rechazaUnAsuntoDemasiadoLargo() {
        String largo = "a".repeat(101);

        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva("u-1", largo, "Descripción", AHORA));
    }

    @Test
    @DisplayName("HU44-CP04: rechaza una descripción de más de 1000 caracteres")
    void rechazaUnaDescripcionDemasiadoLarga() {
        String larga = "b".repeat(1001);

        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva("u-1", "Asunto", larga, AHORA));
    }

    @ParameterizedTest(name = "autor \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU44-CP05: la incidencia necesita un autor")
    void necesitaUnAutor(String autorId) {
        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva(autorId, "Asunto", "Descripción", AHORA));
    }

    @Test
    @DisplayName("HU44-CP05: la incidencia necesita una fecha de creación")
    void necesitaUnaFechaDeCreacion() {
        assertThrows(IncidenciaInvalidaException.class, () -> Incidencia.nueva("u-1", "Asunto", "Descripción", null));
    }

    @Test
    @DisplayName("HU44-CP05: la incidencia necesita un estado")
    void necesitaUnEstado() {
        assertThrows(IncidenciaInvalidaException.class,
                () -> new Incidencia(null, "u-1", "Asunto", "Descripción", null, "", AHORA));
    }

    // Apertura (HU50, T302): un administrador abre una incidencia pendiente y le escribe la respuesta, que es lo único
    // que el autor llega a ver, porque al cerrarla se elimina. La respuesta es obligatoria (hasta 1000 caracteres, sin
    // espacios en los extremos) y una incidencia ya abierta no se vuelve a abrir.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU50-CP01 a CP04, propuestos.

    @Test
    @DisplayName("HU50-CP01: abrir una incidencia pendiente la deja abierta con la respuesta del administrador")
    void abrirUnaIncidenciaPendienteLaDejaAbiertaConLaRespuesta() {
        Incidencia pendiente = new Incidencia("i-1", "u-1", "No me llega el pedido", "Lo pedí hace una semana",
                EstadoIncidencia.PENDIENTE, "", AHORA);

        Incidencia abierta = pendiente.abrir("Lo estamos revisando");

        assertEquals(EstadoIncidencia.ABIERTA, abierta.estado());
        assertEquals("Lo estamos revisando", abierta.respuesta());
        assertEquals("i-1", abierta.id());
        assertEquals("u-1", abierta.autorId());
        assertEquals("No me llega el pedido", abierta.asunto());
        assertEquals("Lo pedí hace una semana", abierta.descripcion());
        assertEquals(AHORA, abierta.fechaCreacion());
    }

    @ParameterizedTest(name = "respuesta \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("HU50-CP02: la respuesta es obligatoria para abrir una incidencia")
    void laRespuestaEsObligatoriaParaAbrir(String respuesta) {
        Incidencia pendiente = Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA);

        assertThrows(IncidenciaInvalidaException.class, () -> pendiente.abrir(respuesta));
    }

    @Test
    @DisplayName("HU50-CP03: la respuesta admite hasta 1000 caracteres y se guarda sin espacios en los extremos")
    void laRespuestaAdmiteElMaximoSinEspaciosEnLosExtremos() {
        String mil = "r".repeat(1000);
        Incidencia pendiente = Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA);

        assertEquals(mil, pendiente.abrir("  " + mil + "  ").respuesta());
    }

    @Test
    @DisplayName("HU50-CP03: rechaza una respuesta de más de 1000 caracteres")
    void rechazaUnaRespuestaDemasiadoLarga() {
        String larga = "r".repeat(1001);
        Incidencia pendiente = Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA);

        assertThrows(IncidenciaInvalidaException.class, () -> pendiente.abrir(larga));
    }

    @Test
    @DisplayName("HU50-CP04: una incidencia ya abierta no se vuelve a abrir")
    void unaIncidenciaYaAbiertaNoSeVuelveAAbrir() {
        Incidencia abierta = new Incidencia("i-1", "u-1", "Asunto", "Descripción", EstadoIncidencia.ABIERTA,
                "Lo estamos revisando", AHORA);

        assertThrows(IncidenciaYaAbiertaException.class, () -> abierta.abrir("Otra respuesta"));
    }
}