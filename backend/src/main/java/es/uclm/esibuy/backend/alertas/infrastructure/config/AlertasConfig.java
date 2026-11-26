package es.uclm.esibuy.backend.alertas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;
import es.uclm.esibuy.backend.alertas.application.service.ConsultaAlertasService;
import es.uclm.esibuy.backend.alertas.application.service.GeneracionAlertasService;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.deseos.FavoritosAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence.AlertaDocumentMapper;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence.AlertaMongoAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence.AlertaMongoRepository;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios.TiposClienteAdapter;
import es.uclm.esibuy.backend.deseos.application.port.in.FavoritosProductoUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.TipoClienteUseCase;

/**
 * Configuración de Spring de las alertas (AB#529): conecta cada puerto con su adaptador. Los nombres de los beans
 * llevan «alertas» para no chocar con los puertos del mismo nombre de otros módulos.
 */
@Configuration
public class AlertasConfig {

    @Bean
    AlertaRepositoryPort alertasRepositoryPort(AlertaMongoRepository alertaMongoRepository) {
        return new AlertaMongoAdapter(alertaMongoRepository, new AlertaDocumentMapper());
    }

    @Bean
    FavoritosPort alertasFavoritosPort(FavoritosProductoUseCase favoritosProductoUseCase) {
        return new FavoritosAdapter(favoritosProductoUseCase);
    }

    @Bean
    TiposClientePort alertasTiposClientePort(TipoClienteUseCase tipoClienteUseCase) {
        return new TiposClienteAdapter(tipoClienteUseCase);
    }

    @Bean
    ClienteEnSesionPort alertasClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    // El reloj es el de UsuariosConfig: con él se fecha cada alerta
    @Bean
    GeneracionAlertasUseCase generacionAlertasUseCase(AlertaRepositoryPort alertasRepositoryPort,
            FavoritosPort alertasFavoritosPort, TiposClientePort alertasTiposClientePort, Clock reloj) {
        return new GeneracionAlertasService(alertasRepositoryPort, alertasFavoritosPort, alertasTiposClientePort,
                reloj);
    }

    @Bean
    ConsultaAlertasUseCase consultaAlertasUseCase(AlertaRepositoryPort alertasRepositoryPort,
            ClienteEnSesionPort alertasClienteEnSesionPort) {
        return new ConsultaAlertasService(alertasRepositoryPort, alertasClienteEnSesionPort);
    }
}
