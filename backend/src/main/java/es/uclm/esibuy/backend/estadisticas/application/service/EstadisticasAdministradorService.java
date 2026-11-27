package es.uclm.esibuy.backend.estadisticas.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorResultado;
import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.NombreComercialVendedorPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ProductosPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasAdministrador;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Reúne las ventas y valoraciones globales y calcula los rankings para el administrador. */
public class EstadisticasAdministradorService implements EstadisticasAdministradorUseCase {

    private final VentasPlataformaPort ventas;
    private final ProductosPlataformaPort productos;
    private final NombreComercialVendedorPort nombresVendedores;

    public EstadisticasAdministradorService(VentasPlataformaPort ventas, ProductosPlataformaPort productos,
            NombreComercialVendedorPort nombresVendedores) {
        this.ventas = ventas;
        this.productos = productos;
        this.nombresVendedores = nombresVendedores;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public EstadisticasAdministradorResultado consultar() {
        EstadisticasAdministrador estadisticas = EstadisticasAdministrador.calcular(ventas.consultar(),
                productos.consultar());
        List<EstadisticasAdministradorResultado.VendedorMejorValorado> vendedores = estadisticas
                .vendedoresMejorValorados().stream()
                .map(vendedor -> new EstadisticasAdministradorResultado.VendedorMejorValorado(
                        vendedor.vendedorId(), nombresVendedores.consultar(vendedor.vendedorId()), vendedor.media(),
                        vendedor.numeroValoraciones()))
                .toList();
        return new EstadisticasAdministradorResultado(estadisticas.productosMasVendidos(),
                estadisticas.productosMejorValorados(), vendedores);
    }
}
