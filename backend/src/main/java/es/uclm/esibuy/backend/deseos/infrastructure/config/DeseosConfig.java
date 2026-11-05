package es.uclm.esibuy.backend.deseos.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.application.service.GestionListaDeseosService;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence.ListaDeseosDocumentMapper;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence.ListaDeseosMongoAdapter;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence.ListaDeseosMongoRepository;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.productos.ProductosAdapter;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring de la lista de deseos (HU14): conecta cada puerto con su adaptador, como CarritoConfig.
 * Los nombres de los beans llevan «deseos» para no chocar con los puertos del mismo nombre de otros módulos.
 */
@Configuration
public class DeseosConfig {

    @Bean
    ListaDeseosRepositoryPort deseosRepositoryPort(ListaDeseosMongoRepository listaDeseosMongoRepository) {
        return new ListaDeseosMongoAdapter(listaDeseosMongoRepository, new ListaDeseosDocumentMapper());
    }

    @Bean
    ClienteEnSesionPort deseosClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    ProductosPort deseosProductosPort(CatalogoProductosUseCase catalogoProductosUseCase) {
        return new ProductosAdapter(catalogoProductosUseCase);
    }

    // El reloj es el de UsuariosConfig: con él se fecha cada producto de la lista
    @Bean
    GestionListaDeseosUseCase gestionListaDeseosUseCase(ListaDeseosRepositoryPort deseosRepositoryPort,
            ClienteEnSesionPort deseosClienteEnSesionPort, ProductosPort deseosProductosPort, Clock reloj) {
        return new GestionListaDeseosService(deseosRepositoryPort, deseosClienteEnSesionPort, deseosProductosPort,
                reloj);
    }
}
