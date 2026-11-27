package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorResultado;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProducto;

/** Contrato de GET /api/admin/statistics con los tres rankings globales. */
public record EstadisticasAdministradorResponse(List<ProductoMasVendido> productosMasVendidos,
        List<ProductoMejorValorado> productosMejorValorados, List<VendedorMejorValorado> vendedoresMejorValorados) {

    public record ProductoMasVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
    }

    public record ProductoMejorValorado(String productoId, String nombre, double media, int numeroValoraciones) {
    }

    public record VendedorMejorValorado(String vendedorId, String nombreComercial, double media,
            int numeroValoraciones) {
    }

    public static EstadisticasAdministradorResponse desde(EstadisticasAdministradorResultado resultado) {
        List<ProductoMasVendido> productosMasVendidos = resultado.productosMasVendidos().stream()
                .map(producto -> new ProductoMasVendido(producto.productoId(), producto.nombre(), producto.unidades(),
                        producto.ingresos()))
                .toList();
        List<ProductoMejorValorado> productos = resultado.productosMejorValorados().stream()
                .map(EstadisticasAdministradorResponse::productoMejorValorado)
                .toList();
        List<VendedorMejorValorado> vendedores = resultado.vendedoresMejorValorados().stream()
                .map(vendedor -> new VendedorMejorValorado(vendedor.vendedorId(), vendedor.nombreComercial(),
                        vendedor.media(), vendedor.numeroValoraciones()))
                .toList();
        return new EstadisticasAdministradorResponse(productosMasVendidos, productos, vendedores);
    }

    private static ProductoMejorValorado productoMejorValorado(ValoracionProducto producto) {
        return new ProductoMejorValorado(producto.productoId(), producto.nombre(), producto.media(), producto.numero());
    }
}
