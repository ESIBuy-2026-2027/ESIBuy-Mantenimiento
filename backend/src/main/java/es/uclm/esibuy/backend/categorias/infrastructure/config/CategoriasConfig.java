package es.uclm.esibuy.backend.categorias.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.application.port.out.VendedoresPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.application.service.AdministracionCategoriasService;
import es.uclm.esibuy.backend.categorias.application.service.ConsultaCategoriasService;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaDocumentMapper;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaMongoAdapter;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaMongoRepository;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.productos.ProductosPorCategoriaAdapter;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.usuarios.VendedoresPorCategoriaAdapter;

/**
 * Configuración de Spring del módulo de categorías: conecta cada puerto con su adaptador, como
 * ProductosConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class CategoriasConfig {

    @Bean
    CategoriaRepositoryPort categoriaRepositoryPort(CategoriaMongoRepository categoriaMongoRepository,
            MongoTemplate mongoTemplate) {
        return new CategoriaMongoAdapter(categoriaMongoRepository, new CategoriaDocumentMapper(), mongoTemplate);
    }

    @Bean
    ConsultaCategoriasUseCase consultaCategoriasUseCase(CategoriaRepositoryPort categoriaRepositoryPort) {
        return new ConsultaCategoriasService(categoriaRepositoryPort);
    }

    @Bean
    ProductosPorCategoriaPort productosPorCategoriaPort(ConteoProductosUseCase conteoProductosUseCase) {
        return new ProductosPorCategoriaAdapter(conteoProductosUseCase);
    }

    @Bean
    VendedoresPorCategoriaPort vendedoresPorCategoriaPort() {
        return new VendedoresPorCategoriaAdapter();
    }

    @Bean
    AdministracionCategoriasUseCase administracionCategoriasUseCase(CategoriaRepositoryPort categoriaRepositoryPort,
            ProductosPorCategoriaPort productosPorCategoriaPort,
            VendedoresPorCategoriaPort vendedoresPorCategoriaPort) {
        return new AdministracionCategoriasService(categoriaRepositoryPort, productosPorCategoriaPort,
                vendedoresPorCategoriaPort);
    }
}
