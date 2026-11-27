package es.uclm.esibuy.backend.estadisticas.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoMasVendido;
import es.uclm.esibuy.backend.estadisticas.domain.model.ValoracionProducto;

/** Resultado de aplicación con los nombres comerciales resueltos para el ranking de vendedores. */
public record EstadisticasAdministradorResultado(List<ProductoMasVendido> productosMasVendidos,
        List<ValoracionProducto> productosMejorValorados, List<VendedorMejorValorado> vendedoresMejorValorados) {

    public record VendedorMejorValorado(String vendedorId, String nombreComercial, double media,
            int numeroValoraciones) {
    }
}
