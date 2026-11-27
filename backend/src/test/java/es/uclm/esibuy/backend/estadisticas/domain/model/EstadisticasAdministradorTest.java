package es.uclm.esibuy.backend.estadisticas.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstadisticasAdministradorTest {

    @Test
    @DisplayName("HU519-CP01: muestra los diez productos con más unidades vendidas")
    void muestraLosDiezProductosMasVendidos() {
        List<VentaRegistrada> ventas = new ArrayList<>(java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(numero -> new VentaRegistrada("o-" + numero, "p-" + numero, "Producto " + numero,
                        13 - numero, BigDecimal.valueOf(13 - numero)))
                .toList());
        ventas.add(new VentaRegistrada("o-13", "p-1", "Producto 1", 2, new BigDecimal("2")));

        EstadisticasAdministrador estadisticas = EstadisticasAdministrador.calcular(ventas, List.of());

        assertEquals(
                List.of("p-1", "p-2", "p-3", "p-4", "p-5", "p-6", "p-7", "p-8", "p-9", "p-10"),
                estadisticas.productosMasVendidos().stream().map(ProductoMasVendido::productoId).toList());
        assertEquals(14, estadisticas.productosMasVendidos().get(0).unidades());
    }

    @Test
    @DisplayName("HU520-CP01: ordena los productos por media y excluye los que no tienen valoraciones")
    void ordenaProductosMejorValoradosYExcluyeLosNoValorados() {
        List<ProductoValorado> productos = List.of(
                new ProductoValorado("p-1", "v-1", "Teclado", 4.5, 2),
                new ProductoValorado("p-2", "v-1", "Ratón", 4.9, 1),
                new ProductoValorado("p-3", "v-2", "Monitor", null, 0));

        EstadisticasAdministrador estadisticas = EstadisticasAdministrador.calcular(List.of(), productos);

        assertEquals(List.of("p-2", "p-1"),
                estadisticas.productosMejorValorados().stream().map(ValoracionProducto::productoId).toList());
    }

    @Test
    @DisplayName("HU521-CP01: pondera la media del vendedor por el número de valoraciones de sus productos")
    void ordenaVendedoresPorMediaPonderadaDeSusProductos() {
        List<ProductoValorado> productos = List.of(
                new ProductoValorado("p-1", "v-1", "Teclado", 5.0, 1),
                new ProductoValorado("p-2", "v-1", "Ratón", 3.0, 3),
                new ProductoValorado("p-3", "v-2", "Monitor", 4.0, 1),
                new ProductoValorado("p-4", "v-3", "Webcam", null, 0));

        EstadisticasAdministrador estadisticas = EstadisticasAdministrador.calcular(List.of(), productos);

        assertEquals(List.of("v-2", "v-1"),
                estadisticas.vendedoresMejorValorados().stream().map(VendedorMasValorado::vendedorId).toList());
        assertEquals(3.5, estadisticas.vendedoresMejorValorados().get(1).media(), 0.0001);
        assertEquals(4, estadisticas.vendedoresMejorValorados().get(1).numeroValoraciones());
    }
}
