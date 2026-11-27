package es.uclm.esibuy.backend.estadisticas.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.estadisticas.application.port.out.NombreComercialVendedorPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ProductosPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoValorado;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;

class EstadisticasAdministradorServiceTest {

    @Test
    @DisplayName("HU521-CP02: resuelve el nombre comercial de los vendedores del ranking")
    void consultaRankingsYEnriqueceLosVendedores() {
        VentasPlataformaPort ventas = () -> List.of(
                new VentaRegistrada("o-1", "p-1", "Teclado", 4, new BigDecimal("40.00")));
        ProductosPlataformaPort productos = () -> List.of(
                new ProductoValorado("p-1", "v-1", "Teclado", 4.5, 2));
        NombreComercialVendedorPort nombres = vendedorId -> "Tienda " + vendedorId;

        var resultado = new EstadisticasAdministradorService(ventas, productos, nombres).consultar();

        assertEquals("p-1", resultado.productosMasVendidos().get(0).productoId());
        assertEquals("p-1", resultado.productosMejorValorados().get(0).productoId());
        assertEquals("Tienda v-1", resultado.vendedoresMejorValorados().get(0).nombreComercial());
        assertEquals(4.5, resultado.vendedoresMejorValorados().get(0).media(), 0.0001);
    }
}
