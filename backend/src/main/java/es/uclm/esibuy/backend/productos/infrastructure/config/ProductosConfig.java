package es.uclm.esibuy.backend.productos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.SupervisionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.application.service.CatalogoProductosService;
import es.uclm.esibuy.backend.productos.application.service.GestionProductosService;
import es.uclm.esibuy.backend.productos.application.service.ListadoProductosService;
import es.uclm.esibuy.backend.productos.application.service.SupervisionProductosService;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoDocumentMapper;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoRepository;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios.UsuarioEnSesionAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios.VendedorAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring del módulo de productos: conecta cada puerto con su adaptador, como
 * UsuariosConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class ProductosConfig {

    @Bean
    ProductoRepositoryPort productoRepositoryPort(ProductoMongoRepository productoMongoRepository,
            MongoTemplate mongoTemplate) {
        return new ProductoMongoAdapter(productoMongoRepository, new ProductoDocumentMapper(), mongoTemplate);
    }

    @Bean
    UsuarioEnSesionPort usuarioEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new UsuarioEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    ListadoProductosUseCase listadoProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            UsuarioEnSesionPort usuarioEnSesionPort) {
        return new ListadoProductosService(productoRepositoryPort, usuarioEnSesionPort);
    }

    @Bean
    VendedorPort vendedorPort(AdministracionUsuariosUseCase administracionUsuariosUseCase) {
        return new VendedorAdapter(administracionUsuariosUseCase);
    }

    @Bean
    SupervisionProductosUseCase supervisionProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            VendedorPort vendedorPort) {
        return new SupervisionProductosService(productoRepositoryPort, vendedorPort);
    }

    @Bean
    GestionProductosUseCase gestionProductosUseCase(ProductoRepositoryPort productoRepositoryPort,
            UsuarioEnSesionPort usuarioEnSesionPort) {
        return new GestionProductosService(productoRepositoryPort, usuarioEnSesionPort);
    }

    @Bean
    CatalogoProductosUseCase catalogoProductosUseCase(ProductoRepositoryPort productoRepositoryPort) {
        return new CatalogoProductosService(productoRepositoryPort);
    }
}
