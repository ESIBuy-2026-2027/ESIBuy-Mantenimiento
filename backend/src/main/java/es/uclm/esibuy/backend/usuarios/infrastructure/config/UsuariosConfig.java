package es.uclm.esibuy.backend.usuarios.infrastructure.config;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.service.AdministracionUsuariosService;
import es.uclm.esibuy.backend.usuarios.application.service.AutenticacionService;
import es.uclm.esibuy.backend.usuarios.application.service.ListadoUsuariosService;
import es.uclm.esibuy.backend.usuarios.application.service.RegistroUsuarioService;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email.SmtpEnvioEmailAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.BCryptHashContrasenaAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.SecureRandomGeneradorCodigoAdapter;

import java.security.SecureRandom;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;

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

    @Bean
    RegistroUsuarioUseCase registroUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                  HashContrasenaPort hashContrasenaPort) {
        return new RegistroUsuarioService(usuarioRepositoryPort, hashContrasenaPort);
    }

    @Bean
    ListadoUsuariosUseCase listadoUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ListadoUsuariosService(usuarioRepositoryPort);
    }

    @Bean
    CodigoVerificacionRepositoryPort codigoVerificacionRepositoryPort(CodigoVerificacionMongoRepository repository) {
        return new CodigoVerificacionMongoAdapter(repository, new CodigoVerificacionDocumentMapper());
    }

    @Bean
    GeneradorCodigoPort generadorCodigoPort() {
        return new SecureRandomGeneradorCodigoAdapter(new SecureRandom()); // SecureRandom, nunca Random (T231, D5)
    }

    @Bean
    EnvioEmailPort envioEmailPort(JavaMailSender mailSender,
                                @Value("${esibuy.mail.remitente}") String remitente) {
        return new SmtpEnvioEmailAdapter(mailSender, remitente);
    }

    @Bean
    Clock reloj() {
        return Clock.systemUTC(); // la hora real; los tests de AutenticacionService usan un Clock fijo
    }

    @Bean
    AutenticacionUseCase autenticacionUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                            HashContrasenaPort hashContrasenaPort,
                                            CodigoVerificacionRepositoryPort codigoVerificacionRepositoryPort,
                                            GeneradorCodigoPort generadorCodigoPort,
                                            EnvioEmailPort envioEmailPort,
                                            Clock reloj) {
        return new AutenticacionService(usuarioRepositoryPort, hashContrasenaPort,
                codigoVerificacionRepositoryPort, generadorCodigoPort, envioEmailPort, reloj);
    }

    @Bean
    AdministracionUsuariosUseCase administracionUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new AdministracionUsuariosService(usuarioRepositoryPort);
    }
}
