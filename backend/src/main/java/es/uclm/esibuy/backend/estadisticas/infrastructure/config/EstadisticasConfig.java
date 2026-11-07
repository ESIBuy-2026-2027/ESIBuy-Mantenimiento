package es.uclm.esibuy.backend.estadisticas.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.application.service.EstadisticasVendedorService;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos.VentasAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos.InventarioAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios.VendedorEnSesionAdapter;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring de las estadísticas del vendedor (HU17): conecta cada puerto con su adaptador, como
 * PedidosConfig. Los nombres de los beans llevan «estadisticas» para no chocar con puertos de otros módulos.
 */
@Configuration
public class EstadisticasConfig {

    @Bean
    VendedorEnSesionPort estadisticasVendedorEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new VendedorEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    VentasPort estadisticasVentasPort(VentasVendedorUseCase ventasVendedorUseCase) {
        return new VentasAdapter(ventasVendedorUseCase);
    }

    @Bean
    InventarioPort estadisticasInventarioPort(InventarioVendedorUseCase inventarioVendedorUseCase) {
        return new InventarioAdapter(inventarioVendedorUseCase);
    }

    @Bean
    EstadisticasVendedorUseCase estadisticasVendedorUseCase(VendedorEnSesionPort estadisticasVendedorEnSesionPort,
            VentasPort estadisticasVentasPort, InventarioPort estadisticasInventarioPort) {
        return new EstadisticasVendedorService(estadisticasVendedorEnSesionPort, estadisticasVentasPort,
                estadisticasInventarioPort);
    }
}
