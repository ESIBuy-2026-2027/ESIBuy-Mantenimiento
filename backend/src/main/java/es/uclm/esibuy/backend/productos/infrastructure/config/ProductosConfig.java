package es.uclm.esibuy.backend.productos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoDocumentMapper;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoAdapter;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoMongoRepository;

/**
 * Configuración de Spring del módulo de productos: conecta cada puerto con su adaptador, como
 * UsuariosConfig. El adaptador no lleva anotaciones de Spring.
 */
@Configuration
public class ProductosConfig {

    @Bean
    ProductoRepositoryPort productoRepositoryPort(ProductoMongoRepository productoMongoRepository) {
        return new ProductoMongoAdapter(productoMongoRepository, new ProductoDocumentMapper());
    }
}
