package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: conversión entre MetodoPago y su documento de MongoDB
class MetodoPagoDocumentMapperTest {

    private final MetodoPagoDocumentMapper mapper = new MetodoPagoDocumentMapper();

    @Test
    @DisplayName("HU6-CP17: un método pasa a documento y vuelve sin perder ningún dato")
    void convierteAdocumentoYVueltaSinPerderDatos() {
        MetodoPago metodo = new MetodoPago("m1", "cliente-1", TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado", true);

        MetodoPagoDocument documento = mapper.aDocumento(metodo);
        MetodoPago leido = mapper.aDominio(documento);

        assertEquals("BIZUM", documento.getTipo());
        assertEquals("m1", leido.getId());
        assertEquals("cliente-1", leido.getClienteId());
        assertEquals(TipoMetodoPago.BIZUM, leido.getTipo());
        assertEquals("•••• 678", leido.getMascara());
        assertEquals("bloque-cifrado", leido.getDatosCifrados());
        assertTrue(leido.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP17: rechaza un documento que no cumple las reglas del dominio")
    void rechazaDocumentoQueNoCumpleElDominio() {
        MetodoPagoDocument sinMascara = mapper.aDocumento(
                new MetodoPago("m1", "cliente-1", TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado", false));
        sinMascara.setMascara(null);
        MetodoPagoDocument tipoDesconocido = mapper.aDocumento(
                new MetodoPago("m2", "cliente-1", TipoMetodoPago.BIZUM, "•••• 678", "bloque-cifrado", false));
        tipoDesconocido.setTipo("CHEQUE");

        assertThrows(MetodoPagoInvalidoException.class, () -> mapper.aDominio(sinMascara));
        assertThrows(MetodoPagoInvalidoException.class, () -> mapper.aDominio(tipoDesconocido));
    }
}
