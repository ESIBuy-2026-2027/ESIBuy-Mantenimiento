package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetodoPagoTest {

    // El bloque cifrado es opaco para el dominio: cualquier texto no vacío sirve en estos tests
    private static final String DATOS_CIFRADOS = "bloque-cifrado";

    @Test
    @DisplayName("HU6-CP07: un método de pago se crea asociado a un cliente por identificador")
    void creaMetodoAsociadoAUnCliente() {
        MetodoPago metodo = MetodoPago.nuevo("cliente-1", TipoMetodoPago.TARJETA, "•••• 4444", DATOS_CIFRADOS);

        // Sin id hasta que se guarda
        assertNull(metodo.getId());
        assertEquals("cliente-1", metodo.getClienteId());
        assertEquals(TipoMetodoPago.TARJETA, metodo.getTipo());
        assertEquals("•••• 4444", metodo.getMascara());
        assertEquals(DATOS_CIFRADOS, metodo.getDatosCifrados());
    }

    @Test
    @DisplayName("HU6-CP07: un método guardado se reconstruye conservando si es predeterminado")
    void reconstruyeMetodoGuardadoConservandoElPredeterminado() {
        // Lo usa el mapper al leer de MongoDB
        MetodoPago metodo = new MetodoPago("m1", "cliente-1", TipoMetodoPago.TARJETA, "•••• 4444",
                DATOS_CIFRADOS, true);

        assertEquals("m1", metodo.getId());
        assertTrue(metodo.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP07: rechaza un método sin cliente, sin tipo, sin máscara o sin datos cifrados")
    void rechazaMetodoConDatosObligatoriosAusentes() {
        TipoMetodoPago tarjeta = TipoMetodoPago.TARJETA;

        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo(null, tarjeta, "•••• 4444", DATOS_CIFRADOS));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo(" ", tarjeta, "•••• 4444", DATOS_CIFRADOS));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo("cliente-1", null, "•••• 4444", DATOS_CIFRADOS));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo("cliente-1", tarjeta, null, DATOS_CIFRADOS));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo("cliente-1", tarjeta, "", DATOS_CIFRADOS));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo("cliente-1", tarjeta, "•••• 4444", null));
        assertThrows(MetodoPagoInvalidoException.class, () -> MetodoPago.nuevo("cliente-1", tarjeta, "•••• 4444", ""));
    }

    @Test
    @DisplayName("HU6-CP09: un método recién creado no es predeterminado")
    void nuevoMetodoNoEsPredeterminado() {
        MetodoPago metodo = MetodoPago.nuevo("cliente-1", TipoMetodoPago.BIZUM, "•••• 678", DATOS_CIFRADOS);

        assertFalse(metodo.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: marcar como predeterminado deja el método predeterminado")
    void marcaMetodoComoPredeterminado() {
        MetodoPago metodo = MetodoPago.nuevo("cliente-1", TipoMetodoPago.PAYPAL, "paypal.me", DATOS_CIFRADOS);

        metodo.marcarPredeterminado();

        assertTrue(metodo.isPredeterminado());
    }

    @Test
    @DisplayName("HU6-CP09: desmarcar deja el método como no predeterminado")
    void desmarcaMetodoPredeterminado() {
        MetodoPago metodo = MetodoPago.nuevo("cliente-1", TipoMetodoPago.PAYPAL, "paypal.me", DATOS_CIFRADOS);
        metodo.marcarPredeterminado();

        metodo.desmarcarPredeterminado();

        assertFalse(metodo.isPredeterminado());
    }
}
