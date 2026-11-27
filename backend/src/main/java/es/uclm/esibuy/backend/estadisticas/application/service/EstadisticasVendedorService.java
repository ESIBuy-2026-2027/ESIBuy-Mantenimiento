package es.uclm.esibuy.backend.estadisticas.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;
import es.uclm.esibuy.backend.estadisticas.domain.model.PeriodoEstadisticasVendedor;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ValoracionesPort;

/** Estadísticas del vendedor en sesión (HU17): reúne sus ventas y sus productos y deja el cálculo al dominio. */
public class EstadisticasVendedorService implements EstadisticasVendedorUseCase {

    private final VendedorEnSesionPort vendedorEnSesion;
    private final VentasPort ventas;
    private final InventarioPort inventario;
    private final ValoracionesPort valoraciones;
    private final Clock clock;

    public EstadisticasVendedorService(VendedorEnSesionPort vendedorEnSesion, VentasPort ventas,
            InventarioPort inventario, ValoracionesPort valoraciones, Clock clock) {
        this.vendedorEnSesion = vendedorEnSesion;
        this.ventas = ventas;
        this.inventario = inventario;
        this.valoraciones = valoraciones;
        this.clock = clock;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public EstadisticasVendedor consultar(String emailVendedor, PeriodoEstadisticasVendedor periodo) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        LocalDate hoy = LocalDate.now(clock);
        Instant hasta = hoy.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        Optional<Instant> desde = periodo.desde(hoy, clock.getZone());
        return EstadisticasVendedor.calcular(
                ventas.ventasDe(vendedorId, desde, hasta),
                inventario.productosDe(vendedorId),
                valoraciones.resumenDe(emailVendedor, desde, hasta),
                periodo,
                hoy);
    }
}
