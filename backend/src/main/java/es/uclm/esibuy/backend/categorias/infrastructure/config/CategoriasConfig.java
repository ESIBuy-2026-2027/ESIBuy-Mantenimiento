package es.uclm.esibuy.backend.categorias.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.service.ConsultaCategoriasService;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaDocumentMapper;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaMongoAdapter;
import es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence.CategoriaMongoRepository;

/**
 * Configuración de Spring del módulo de categorías: conecta cada puerto con su adaptador, como
 * ProductosConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class CategoriasConfig {

    @Bean
    CategoriaRepositoryPort categoriaRepositoryPort(CategoriaMongoRepository categoriaMongoRepository) {
        return new CategoriaMongoAdapter(categoriaMongoRepository, new CategoriaDocumentMapper());
    }

    @Bean
    ConsultaCategoriasUseCase consultaCategoriasUseCase(CategoriaRepositoryPort categoriaRepositoryPort) {
        return new ConsultaCategoriasService(categoriaRepositoryPort);
    }
}
