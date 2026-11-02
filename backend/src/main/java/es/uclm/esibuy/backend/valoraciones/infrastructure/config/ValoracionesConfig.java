package es.uclm.esibuy.backend.valoraciones.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ConsultaValoracionesUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.application.service.ConsultaValoracionesService;
import es.uclm.esibuy.backend.valoraciones.application.service.ValoracionProductosService;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.pedidos.ComprasAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionDocumentMapper;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionMongoAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionMongoRepository;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;

/**
 * Configuración de Spring de las valoraciones (HU13): conecta cada puerto con su adaptador, como PedidosConfig. Los
 * nombres de los beans llevan «valoraciones» para no chocar con los puertos del mismo nombre de otros módulos.
 */
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
    ValoracionProductosUseCase valoracionProductosUseCase(ValoracionRepositoryPort valoracionRepositoryPort,
            ClienteEnSesionPort valoracionesClienteEnSesionPort, ComprasPort valoracionesComprasPort) {
        return new ValoracionProductosService(valoracionRepositoryPort, valoracionesClienteEnSesionPort,
                valoracionesComprasPort);
    }
    
    @Bean
    ConsultaValoracionesUseCase consultaValoracionesUseCase(ValoracionRepositoryPort valoracionRepositoryPort) {
        return new ConsultaValoracionesService(valoracionRepositoryPort);
    }
}
