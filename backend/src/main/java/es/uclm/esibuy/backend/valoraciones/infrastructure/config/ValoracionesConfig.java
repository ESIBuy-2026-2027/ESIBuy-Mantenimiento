package es.uclm.esibuy.backend.valoraciones.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.EstadisticasValoracionesVendedorUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.LimpiezaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.IdentidadVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosDelVendedorPort;
import es.uclm.esibuy.backend.valoraciones.application.service.ConsultaValoracionesService;
import es.uclm.esibuy.backend.valoraciones.application.service.EstadisticasValoracionesVendedorService;
import es.uclm.esibuy.backend.valoraciones.application.service.ValoracionProductosService;
import es.uclm.esibuy.backend.valoraciones.application.service.LimpiezaValoracionesService;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.pedidos.ComprasAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.productos.ProductosAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionDocumentMapper;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionMongoAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionMongoRepository;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios.IdentidadVendedorAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.productos.ProductosDelVendedorAdapter;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;

@Configuration
public class ValoracionesConfig {

    @Bean
    ValoracionRepositoryPort valoracionRepositoryPort(ValoracionMongoRepository valoracionMongoRepository) {
        return new ValoracionMongoAdapter(valoracionMongoRepository, new ValoracionDocumentMapper());
    }

    @Bean
    ClienteEnSesionPort valoracionesClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    ComprasPort valoracionesComprasPort(ConsultaComprasUseCase consultaComprasUseCase) {
        return new ComprasAdapter(consultaComprasUseCase);
    }

    @Bean
    ProductosPort valoracionesProductosPort(ValoracionesProductoUseCase valoracionesProductoUseCase) {
        return new ProductosAdapter(valoracionesProductoUseCase);
    }

    @Bean
    ValoracionProductosUseCase valoracionProductosUseCase(ValoracionRepositoryPort valoracionRepositoryPort,
            ClienteEnSesionPort clienteEnSesionPort, ComprasPort comprasPort, ProductosPort productosPort) {
        return new ValoracionProductosService(valoracionRepositoryPort, clienteEnSesionPort, comprasPort, productosPort);
    }

    @Bean
    ConsultaValoracionesUseCase consultaValoracionesUseCase(ValoracionRepositoryPort valoracionRepositoryPort,
            ClienteEnSesionPort clienteEnSesionPort, ComprasPort comprasPort) {
        return new ConsultaValoracionesService(valoracionRepositoryPort, clienteEnSesionPort, comprasPort);
    }

    @Bean
    LimpiezaValoracionesUseCase limpiezaValoracionesUseCase(ValoracionRepositoryPort valoracionRepositoryPort) {
        return new LimpiezaValoracionesService(valoracionRepositoryPort);
    }

    @Bean
    IdentidadVendedorPort valoracionesIdentidadVendedorPort(AutenticacionUseCase autenticacionUseCase) {
        return new IdentidadVendedorAdapter(autenticacionUseCase);
    }

    @Bean
    ProductosDelVendedorPort valoracionesProductosDelVendedorPort(
            InventarioVendedorUseCase inventarioVendedorUseCase) {
        return new ProductosDelVendedorAdapter(inventarioVendedorUseCase);
    }

    @Bean
    EstadisticasValoracionesVendedorUseCase estadisticasValoracionesVendedorUseCase(
            ValoracionRepositoryPort valoracionRepositoryPort,
            IdentidadVendedorPort identidadVendedorPort,
            ProductosDelVendedorPort productosDelVendedorPort) {
        return new EstadisticasValoracionesVendedorService(valoracionRepositoryPort,
                identidadVendedorPort, productosDelVendedorPort);
    }
}
