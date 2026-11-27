package es.uclm.esibuy.backend.estadisticas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.NombreComercialVendedorPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ProductosPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.ValoracionesPort;
import es.uclm.esibuy.backend.estadisticas.application.service.EstadisticasAdministradorService;
import es.uclm.esibuy.backend.estadisticas.application.service.EstadisticasVendedorService;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos.VentasPlataformaAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos.VentasAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos.InventarioAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos.ProductosPlataformaAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios.NombreComercialVendedorAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios.VendedorEnSesionAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.valoraciones.ValoracionesAdapter;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasPlataformaUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductosPlataformaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadisticasValoracionesVendedorUseCase;

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
    ValoracionesPort estadisticasValoracionesPort(
            EstadisticasValoracionesVendedorUseCase estadisticasValoracionesVendedorUseCase) {
        return new ValoracionesAdapter(estadisticasValoracionesVendedorUseCase);
    }

    @Bean
    EstadisticasVendedorUseCase estadisticasVendedorUseCase(VendedorEnSesionPort estadisticasVendedorEnSesionPort,
            VentasPort estadisticasVentasPort, InventarioPort estadisticasInventarioPort,
            ValoracionesPort estadisticasValoracionesPort, Clock clock) {
        return new EstadisticasVendedorService(estadisticasVendedorEnSesionPort, estadisticasVentasPort,
                estadisticasInventarioPort, estadisticasValoracionesPort, clock);
    }

    @Bean
    VentasPlataformaPort estadisticasVentasPlataformaPort(VentasPlataformaUseCase ventasPlataformaUseCase) {
        return new VentasPlataformaAdapter(ventasPlataformaUseCase);
    }

    @Bean
    ProductosPlataformaPort estadisticasProductosPlataformaPort(
            ProductosPlataformaUseCase productosPlataformaUseCase) {
        return new ProductosPlataformaAdapter(productosPlataformaUseCase);
    }

    @Bean
    NombreComercialVendedorPort estadisticasNombreComercialVendedorPort(
            AdministracionUsuariosUseCase administracionUsuariosUseCase) {
        return new NombreComercialVendedorAdapter(administracionUsuariosUseCase);
    }

    @Bean
    EstadisticasAdministradorUseCase estadisticasAdministradorUseCase(VentasPlataformaPort ventasPlataformaPort,
            ProductosPlataformaPort productosPlataformaPort,
            NombreComercialVendedorPort nombreComercialVendedorPort) {
        return new EstadisticasAdministradorService(ventasPlataformaPort, productosPlataformaPort,
                nombreComercialVendedorPort);
    }
}
