package es.uclm.esibuy.backend.usuarios.infrastructure.config;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.service.AdministracionUsuariosService;
import es.uclm.esibuy.backend.usuarios.application.service.AutenticacionService;
import es.uclm.esibuy.backend.usuarios.application.service.AuthenticatorService;
import es.uclm.esibuy.backend.usuarios.application.service.GestionPerfilService;
import es.uclm.esibuy.backend.usuarios.application.service.ListadoUsuariosService;
import es.uclm.esibuy.backend.usuarios.application.service.RecuperacionContrasenaService;
import es.uclm.esibuy.backend.usuarios.application.service.RegistroUsuarioService;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web.CuentaActivaFilter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email.SmtpEnvioEmailAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoRecuperacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.FactorAutenticacionDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.FactorAutenticacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.FactorAutenticacionMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioDocumentMapper;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoRepository;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.AesGcmCifradoSecretoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.BCryptHashContrasenaAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.JavaTotpAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.SecureRandomGeneradorCodigoAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.application.service.CrearAdministradorService;

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
            MongoTemplate mongoTemplate, ConsultaCategoriasUseCase consultaCategorias) {
        return new UsuarioMongoAdapter(usuarioMongoRepository, new UsuarioDocumentMapper(), mongoTemplate,
                consultaCategorias);
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

    @Bean
    CrearAdministradorUseCase crearAdministradorUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                        HashContrasenaPort hashContrasenaPort) {
        return new CrearAdministradorService(usuarioRepositoryPort, hashContrasenaPort);
    }

    @Bean
    CodigoVerificacionRepositoryPort codigoVerificacionRepositoryPort(CodigoVerificacionMongoRepository repository) {
        return new CodigoVerificacionMongoAdapter(repository, new CodigoVerificacionDocumentMapper());
    }

    // HU3: misma colección y repositorio que los códigos del login, pero con su propio propósito
    @Bean
    CodigoRecuperacionRepositoryPort codigoRecuperacionRepositoryPort(CodigoVerificacionMongoRepository repository) {
        return new CodigoRecuperacionMongoAdapter(repository, new CodigoVerificacionDocumentMapper());
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
        return Clock.systemUTC(); // la hora real; los tests de AutenticacionService y RegistroUsuarioService usan un Clock fijo
    }

    @Bean
    AutenticacionUseCase autenticacionUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                            HashContrasenaPort hashContrasenaPort,
                                            CodigoVerificacionRepositoryPort codigoVerificacionRepositoryPort,
                                            GeneradorCodigoPort generadorCodigoPort,
                                            EnvioEmailPort envioEmailPort,
                                            FactorAutenticacionRepositoryPort factorAutenticacionRepositoryPort,
                                            Clock reloj) {
        return new AutenticacionService(usuarioRepositoryPort, hashContrasenaPort,
                codigoVerificacionRepositoryPort, generadorCodigoPort, envioEmailPort, factorAutenticacionRepositoryPort, reloj);
    }

    @Bean
    RecuperacionContrasenaUseCase recuperacionContrasenaUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                                HashContrasenaPort hashContrasenaPort,
                                                                CodigoRecuperacionRepositoryPort codigoRecuperacionRepositoryPort,
                                                                GeneradorCodigoPort generadorCodigoPort,
                                                                EnvioEmailPort envioEmailPort,
                                                                Clock reloj) {
        return new RecuperacionContrasenaService(usuarioRepositoryPort, hashContrasenaPort,
                codigoRecuperacionRepositoryPort, generadorCodigoPort, envioEmailPort, reloj);
    }

    // HU23 (T206): Spring Boot añade este filtro a cada petición e invalida la sesión de las cuentas bloqueadas
    @Bean
    CuentaActivaFilter cuentaActivaFilter(AutenticacionUseCase autenticacionUseCase) {
        return new CuentaActivaFilter(autenticacionUseCase);
    }

    @Bean
    AdministracionUsuariosUseCase administracionUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                                Clock reloj) {
        return new AdministracionUsuariosService(usuarioRepositoryPort, reloj);
    }

    @Bean
    FactorAutenticacionRepositoryPort factorAutenticacionRepositoryPort(FactorAutenticacionMongoRepository repository) {
        return new FactorAutenticacionMongoAdapter(repository, new FactorAutenticacionDocumentMapper());
    }

    @Bean
    TotpPort totpPort() {
        return new JavaTotpAdapter();
    }

    // La clave AES del secreto TOTP sale de application.properties (T20, D3: riesgo asumido)
    @Bean
    CifradoSecretoPort cifradoSecretoPort(@Value("${esibuy.totp.clave-cifrado}") String clave) {
        return new AesGcmCifradoSecretoAdapter(clave);
    }

    @Bean
    AuthenticatorUseCase authenticatorUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                              FactorAutenticacionRepositoryPort factorAutenticacionRepositoryPort,
                                              TotpPort totpPort, CifradoSecretoPort cifradoSecretoPort, Clock reloj) {
        return new AuthenticatorService(usuarioRepositoryPort, factorAutenticacionRepositoryPort, totpPort,
                cifradoSecretoPort, reloj);
    }

    @Bean
    GestionPerfilUseCase gestionPerfilUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                              HashContrasenaPort hashContrasenaPort) {
        return new GestionPerfilService(usuarioRepositoryPort, hashContrasenaPort);
    }
}
