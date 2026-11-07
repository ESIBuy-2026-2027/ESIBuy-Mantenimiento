package es.uclm.esibuy.backend.preferencias.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.application.service.GestionPreferenciasService;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.categorias.CategoriasAdapter;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence.PreferenciasDocumentMapper;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence.PreferenciasMongoAdapter;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence.PreferenciasMongoRepository;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring de las preferencias (HU46): conecta cada puerto con su adaptador, como DeseosConfig. Los
 * nombres de los beans llevan «preferencias» para no chocar con los puertos del mismo nombre de otros módulos.
 */
@Configuration
public class PreferenciasConfig {

    @Bean
    PreferenciasRepositoryPort preferenciasRepositoryPort(PreferenciasMongoRepository preferenciasMongoRepository) {
        return new PreferenciasMongoAdapter(preferenciasMongoRepository, new PreferenciasDocumentMapper());
    }

    @Bean
    ClienteEnSesionPort preferenciasClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    CategoriasPort preferenciasCategoriasPort(ConsultaCategoriasUseCase consultaCategoriasUseCase) {
        return new CategoriasAdapter(consultaCategoriasUseCase);
    }

    @Bean
    GestionPreferenciasUseCase gestionPreferenciasUseCase(PreferenciasRepositoryPort preferenciasRepositoryPort,
            ClienteEnSesionPort preferenciasClienteEnSesionPort, CategoriasPort preferenciasCategoriasPort) {
        return new GestionPreferenciasService(preferenciasRepositoryPort, preferenciasClienteEnSesionPort,
                preferenciasCategoriasPort);
    }
}
