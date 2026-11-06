package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

// Sin Spring: conversión entre Preferencias y su documento de MongoDB (HU46, T287).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU46-CP11, propuesto.
class PreferenciasDocumentMapperTest {

    private final PreferenciasDocumentMapper mapper = new PreferenciasDocumentMapper();

    @Test
    @DisplayName("HU46-CP11: unas preferencias pasan a documento y vuelven sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        Preferencias preferencias = new Preferencias("pr-1", "cliente-1", List.of("cat-2", "cat-1"));

        PreferenciasDocument documento = mapper.aDocumento(preferencias);
        Preferencias leidas = mapper.aDominio(documento);

        assertEquals("cliente-1", documento.getClienteId());
        assertEquals(List.of("cat-2", "cat-1"), documento.getCategorias());
        assertEquals("pr-1", leidas.getId());
        assertEquals("cliente-1", leidas.getClienteId());
        assertEquals(List.of("cat-2", "cat-1"), leidas.getCategorias());
    }
}
