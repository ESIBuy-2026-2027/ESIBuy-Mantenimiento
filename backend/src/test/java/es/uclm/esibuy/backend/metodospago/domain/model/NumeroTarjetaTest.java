package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumeroTarjetaTest {

    @Test
    @DisplayName("HU6-CP01: acepta un número de tarjeta válido según Luhn")
    void creaNumeroDeTarjetaValido() {
        // Número de prueba de Visa: 16 dígitos y supera Luhn
        NumeroTarjeta numero = new NumeroTarjeta("4111111111111111");

        assertEquals("4111111111111111", numero.valor());
    }

    @Test
    @DisplayName("HU6-CP01: rechaza un número que no supera la comprobación de Luhn")
    void rechazaNumeroConLuhnIncorrecto() {
        // Mismo número de prueba con el último dígito cambiado: deja de cumplir Luhn
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("4111111111111112"));
    }

    @Test
    @DisplayName("HU6-CP01: rechaza un número con letras o separadores")
    void rechazaNumeroConCaracteresNoNumericos() {
        // Solo dígitos: el formulario no debe enviar espacios ni guiones. Valores elegidos para que la
        // suma de Luhn, tratando cada carácter como (código - '0'), salga múltiplo de 10: así el
        // rechazo se debe al formato y no a Luhn.
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("4111 1111 1111 1118"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("4111-1111-1111-1116"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("411111111111113A"));
    }

    @Test
    @DisplayName("HU6-CP01: rechaza un número de 12 dígitos")
    void rechazaNumeroDeDoceDigitos() {
        // Frontera inferior: 12 dígitos no valen; el mínimo es 13. Cumple Luhn para que el rechazo sea por longitud
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("411111111117"));
    }

    @Test
    @DisplayName("HU6-CP01: rechaza un número de 20 dígitos")
    void rechazaNumeroDeVeinteDigitos() {
        // Frontera superior: 20 dígitos no valen; el máximo es 19. Cumple Luhn para que el rechazo sea por longitud
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta("41111111111111111115"));
    }

    @Test
    @DisplayName("HU6-CP01: rechaza un número nulo o vacío")
    void rechazaNumeroNuloOVacio() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta(null));
        assertThrows(MetodoPagoInvalidoException.class, () -> new NumeroTarjeta(""));
    }

    @Test
    @DisplayName("HU6-CP06: la máscara muestra solo los últimos cuatro dígitos")
    void enmascaraTodoMenosLosUltimosCuatroDigitos() {
        // Número de prueba de Mastercard: termina en 4444
        NumeroTarjeta numero = new NumeroTarjeta("5555555555554444");

        assertEquals("•••• 4444", numero.mascara());
    }
}
