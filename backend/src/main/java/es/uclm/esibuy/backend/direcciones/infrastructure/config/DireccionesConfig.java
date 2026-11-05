package es.uclm.esibuy.backend.direcciones.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.direcciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;
import es.uclm.esibuy.backend.direcciones.application.service.GestionDireccionesService;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence.DireccionDocumentMapper;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence.DireccionMongoAdapter;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence.DireccionMongoRepository;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring del módulo de direcciones de envío (HU5): conecta cada puerto con su adaptador, como
 * MetodosPagoConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class DireccionesConfig {

    @Bean
    DireccionRepositoryPort direccionRepositoryPort(DireccionMongoRepository direccionMongoRepository) {
        return new DireccionMongoAdapter(direccionMongoRepository, new DireccionDocumentMapper());
    }

    // Nombre propio: metodospago ya tiene un bean clienteEnSesionPort (de su propio puerto)
    @Bean
    ClienteEnSesionPort clienteEnSesionDireccionesPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    GestionDireccionesUseCase gestionDireccionesUseCase(DireccionRepositoryPort direccionRepositoryPort,
            ClienteEnSesionPort clienteEnSesionDireccionesPort) {
        return new GestionDireccionesService(direccionRepositoryPort, clienteEnSesionDireccionesPort);
    }
}
