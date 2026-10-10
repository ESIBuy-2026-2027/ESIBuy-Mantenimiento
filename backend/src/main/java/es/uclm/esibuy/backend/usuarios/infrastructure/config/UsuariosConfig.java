package es.uclm.esibuy.backend.usuarios.infrastructure.config;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.service.ListadoUsuariosService;
import es.uclm.esibuy.backend.usuarios.application.service.RegistroUsuarioService;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.BCryptHashContrasenaAdapter;
import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Presenta a Spring las piezas del módulo de usuarios y cómo se conectan entre sí.
 *
 * <p>Se hace aquí, en infraestructura, para que dominio y aplicación no lleven anotaciones de
 * Spring. Cada {@code @Bean} es singleton por defecto: Spring crea una sola instancia y la
 * inyecta donde se necesita (por eso no se programa el patrón Singleton a mano).
 */
@Configuration
public class UsuariosConfig {

    @Bean
    HashContrasenaPort hashContrasenaPort() {
        return new BCryptHashContrasenaAdapter();
    }

    @Bean
    UsuarioRepositoryPort usuarioRepositoryPort(UsuarioMongoRepository usuarioMongoRepository,
            MongoTemplate mongoTemplate) {
        return new UsuarioMongoAdapter(usuarioMongoRepository, new UsuarioDocumentMapper(), mongoTemplate);
    }

    // Reloj del sistema para los servicios que necesitan la fecha actual (registro: mayoría de edad y
    // consentimiento). En los tests se sustituye por Clock.fixed(...).
    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    RegistroUsuarioUseCase registroUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                  HashContrasenaPort hashContrasenaPort, Clock reloj) {
        return new RegistroUsuarioService(usuarioRepositoryPort, hashContrasenaPort, reloj);
    }

    @Bean
    ListadoUsuariosUseCase listadoUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ListadoUsuariosService(usuarioRepositoryPort);
    }
}
