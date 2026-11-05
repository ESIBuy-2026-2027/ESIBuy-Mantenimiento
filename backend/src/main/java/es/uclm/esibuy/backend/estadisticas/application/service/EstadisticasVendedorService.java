package es.uclm.esibuy.backend.estadisticas.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Estadísticas del vendedor en sesión (HU17): reúne sus ventas y sus productos y deja el cálculo al dominio. */
public class EstadisticasVendedorService implements EstadisticasVendedorUseCase {

    private final VendedorEnSesionPort vendedorEnSesion;
    private final VentasPort ventas;
    private final InventarioPort inventario;

    public EstadisticasVendedorService(VendedorEnSesionPort vendedorEnSesion, VentasPort ventas,
            InventarioPort inventario) {
        this.vendedorEnSesion = vendedorEnSesion;
        this.ventas = ventas;
        this.inventario = inventario;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public EstadisticasVendedor consultar(String emailVendedor) {
        throw new UnsupportedOperationException("Funcionalidad no implementada");
    }
}
