package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

// Sin Spring: conversión entre Incidencia (HU44, T291) y su documento de MongoDB. El autor del dominio es el
// usuarioId del documento y el estado se guarda como texto.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU44-CP06, propuesto.
class IncidenciaDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");

    private final IncidenciaDocumentMapper mapper = new IncidenciaDocumentMapper();

    @Test
    @DisplayName("HU44-CP06: una incidencia pasa a documento y vuelve sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        Incidencia incidencia = new Incidencia("i-1", "u-1", "No me llega el pedido", "Lo pedí hace una semana",
                EstadoIncidencia.ABIERTA, "Lo estamos revisando", AHORA);

        IncidenciaDocument documento = mapper.aDocumento(incidencia);
        Incidencia leida = mapper.aDominio(documento);

        assertEquals("i-1", documento.getId());
        assertEquals("u-1", documento.getUsuarioId());
        assertEquals("No me llega el pedido", documento.getAsunto());
        assertEquals("Lo pedí hace una semana", documento.getDescripcion());
        assertEquals("ABIERTA", documento.getEstado());
        assertEquals("Lo estamos revisando", documento.getRespuesta());
        assertEquals(AHORA, documento.getFechaCreacion());
        assertEquals(incidencia, leida);
    }

    @Test
    @DisplayName("HU44-CP06: una incidencia nueva pasa a documento sin id, pendiente y sin respuesta")
    void unaIncidenciaNuevaPasaADocumentoSinId() {
        IncidenciaDocument documento = mapper.aDocumento(Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA));

        assertNull(documento.getId());
        assertEquals("PENDIENTE", documento.getEstado());
        assertEquals("", documento.getRespuesta());
    }

    @Test
    @DisplayName("HU44-CP06: un documento sin respuesta se lee con la respuesta vacía")
    void unDocumentoSinRespuestaSeLeeConLaRespuestaVacia() {
        IncidenciaDocument documento = mapper.aDocumento(Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA));
        documento.setRespuesta(null);

        assertEquals("", mapper.aDominio(documento).respuesta());
    }

    @Test
    @DisplayName("HU44-CP06: un documento que no cumple las reglas del dominio se rechaza al leerlo")
    void unDocumentoInvalidoSeRechazaAlLeerlo() {
        IncidenciaDocument documento = mapper.aDocumento(Incidencia.nueva("u-1", "Asunto", "Descripción", AHORA));
        documento.setAsunto("   ");

        assertThrows(IncidenciaInvalidaException.class, () -> mapper.aDominio(documento));
    }
}