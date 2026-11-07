package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: conversión entre ListaDeseos y su documento de MongoDB
class ListaDeseosDocumentMapperTest {

    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-11-04T10:00:00Z");

    private final ListaDeseosDocumentMapper mapper = new ListaDeseosDocumentMapper();

    @Test
    @DisplayName("HU14-CP06: una lista pasa a documento y vuelve sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        ListaDeseos lista = new ListaDeseos("l-1", "cliente-1",
                List.of(new ItemDeseo("p-1", AYER), new ItemDeseo("p-2", HOY)));

        ListaDeseosDocument documento = mapper.aDocumento(lista);
        ListaDeseos leida = mapper.aDominio(documento);

        assertEquals("cliente-1", documento.getUsuarioId());
        assertEquals("p-1", documento.getItems().get(0).getProductoId());
        assertEquals("l-1", leida.getId());
        assertEquals("cliente-1", leida.getClienteId());
        assertEquals(lista.getItems(), leida.getItems());
    }
}
