package es.uclm.esibuy.backend.metodospago.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.application.service.GestionMetodosPagoService;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence.MetodoPagoDocumentMapper;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence.MetodoPagoMongoAdapter;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence.MetodoPagoMongoRepository;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.seguridad.AesGcmCifradoDatosPagoAdapter;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring del módulo de métodos de pago (HU6): conecta cada puerto con su adaptador, como
 * ProductosConfig. Los adaptadores y servicios no llevan anotaciones de Spring.
 */
@Configuration
public class MetodosPagoConfig {

    @Bean
    MetodoPagoRepositoryPort metodoPagoRepositoryPort(MetodoPagoMongoRepository metodoPagoMongoRepository) {
        return new MetodoPagoMongoAdapter(metodoPagoMongoRepository, new MetodoPagoDocumentMapper());
    }

    // La clave sale de la variable de entorno ESIBUY_PAGOS_CLAVE_CIFRADO (application.properties), nunca del
    // repositorio. Sin ella, o con una clave inválida, la aplicación no arranca.
    @Bean
    CifradoDatosPagoPort cifradoDatosPagoPort(@Value("${esibuy.pagos.clave-cifrado}") String clave) {
        return new AesGcmCifradoDatosPagoAdapter(clave);
    }

    @Bean
    ClienteEnSesionPort clienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    // El reloj es el de UsuariosConfig: con él se comprueba que la tarjeta no esté caducada
    @Bean
    GestionMetodosPagoUseCase gestionMetodosPagoUseCase(MetodoPagoRepositoryPort metodoPagoRepositoryPort,
            CifradoDatosPagoPort cifradoDatosPagoPort, ClienteEnSesionPort clienteEnSesionPort, Clock reloj) {
        return new GestionMetodosPagoService(metodoPagoRepositoryPort, cifradoDatosPagoPort, clienteEnSesionPort,
                reloj);
    }
}
