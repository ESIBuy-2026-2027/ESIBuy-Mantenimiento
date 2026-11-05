package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: conversión entre DireccionEnvio y su documento de MongoDB
class DireccionDocumentMapperTest {

    private static final DatosDireccion DATOS = new DatosDireccion("Juan Pérez", "Calle Mayor 1", "2º B",
            new CodigoPostal("13071"), "Ciudad Real", "Ciudad Real", new TelefonoContacto("612345678"));

    private final DireccionDocumentMapper mapper = new DireccionDocumentMapper();

    @Test
    @DisplayName("HU5-CP09: una dirección pasa a documento y vuelve sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        DireccionEnvio direccion = new DireccionEnvio("d1", "cliente-1", DATOS, true);

        DireccionDocument documento = mapper.aDocumento(direccion);
        DireccionEnvio leida = mapper.aDominio(documento);

        assertEquals("13071", documento.getCodigoPostal());
        assertEquals("612345678", documento.getTelefono());
        assertEquals("d1", leida.getId());
        assertEquals("cliente-1", leida.getClienteId());
        assertEquals(DATOS, leida.getDatos());
        assertTrue(leida.isPrincipal());
    }

    @Test
    @DisplayName("HU5-CP09: rechaza un documento que no cumple las reglas del dominio")
    void rechazaDocumentoQueNoCumpleElDominio() {
        DireccionDocument sinCalle = mapper.aDocumento(new DireccionEnvio("d1", "cliente-1", DATOS, false));
        sinCalle.setCalle(null);
        DireccionDocument codigoPostalInvalido = mapper.aDocumento(new DireccionEnvio("d2", "cliente-1", DATOS, false));
        codigoPostalInvalido.setCodigoPostal("ABCDE");

        assertThrows(DireccionInvalidaException.class, () -> mapper.aDominio(sinCalle));
        assertThrows(DireccionInvalidaException.class, () -> mapper.aDominio(codigoPostalInvalido));
    }
}
