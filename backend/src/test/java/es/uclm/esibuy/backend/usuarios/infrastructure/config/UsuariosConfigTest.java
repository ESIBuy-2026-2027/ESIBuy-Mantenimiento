package es.uclm.esibuy.backend.usuarios.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.service.AutenticacionService;
import es.uclm.esibuy.backend.usuarios.application.service.AuthenticatorService;
import es.uclm.esibuy.backend.usuarios.application.service.RecuperacionContrasenaService;
import es.uclm.esibuy.backend.usuarios.application.service.RegistroUsuarioService;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web.CuentaActivaFilter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.email.SmtpEnvioEmailAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoRecuperacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.FactorAutenticacionMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.UsuarioMongoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.AesGcmCifradoSecretoAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.BCryptHashContrasenaAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.JavaTotpAdapter;
import es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad.SecureRandomGeneradorCodigoAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Test técnico: Spring monta la cadena del registro al arrancar (HU1, AB#216).
// Arranca la aplicación entera con un MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class UsuariosConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta servicio, hash y adaptador de MongoDB")
    void elContextoConectaLasPiezasDelRegistro() {
        assertInstanceOf(RegistroUsuarioService.class, contexto.getBean(RegistroUsuarioUseCase.class));
        assertInstanceOf(BCryptHashContrasenaAdapter.class, contexto.getBean(HashContrasenaPort.class));
        assertInstanceOf(UsuarioMongoAdapter.class, contexto.getBean(UsuarioRepositoryPort.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta el login con sus adaptadores de códigos, generador y correo")
    void elContextoConectaLasPiezasDelLogin() {
        // T230: el login es el único que usa los adaptadores de la T231 y la T232, y se registran con él
        assertInstanceOf(AutenticacionService.class, contexto.getBean(AutenticacionUseCase.class));
        assertInstanceOf(CodigoVerificacionMongoAdapter.class, contexto.getBean(CodigoVerificacionRepositoryPort.class));
        assertInstanceOf(SecureRandomGeneradorCodigoAdapter.class, contexto.getBean(GeneradorCodigoPort.class));
        assertInstanceOf(SmtpEnvioEmailAdapter.class, contexto.getBean(EnvioEmailPort.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta el paso 3 (Authenticator) con su TOTP, su cifrado y sus factores")
    void elContextoConectaLasPiezasDelAuthenticator() {
        // T20: el paso 3 del login va en su propio servicio, con la librería TOTP, AES-GCM y MongoDB
        assertInstanceOf(AuthenticatorService.class, contexto.getBean(AuthenticatorUseCase.class));
        assertInstanceOf(JavaTotpAdapter.class, contexto.getBean(TotpPort.class));
        assertInstanceOf(AesGcmCifradoSecretoAdapter.class, contexto.getBean(CifradoSecretoPort.class));
        assertInstanceOf(FactorAutenticacionMongoAdapter.class, contexto.getBean(FactorAutenticacionRepositoryPort.class));
    }

    @Test
    @DisplayName("Configuración: el contexto registra el filtro que invalida la sesión de las cuentas desactivadas")
    void elContextoRegistraElFiltroDeCuentaActiva() {
        // HU23, T206: como bean de Spring, Spring Boot lo añade a los filtros de cada petición
        assertNotNull(contexto.getBean(CuentaActivaFilter.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta la recuperación de contraseña con su adaptador de códigos")
    void elContextoConectaLasPiezasDeLaRecuperacionDeContrasena() {
        // HU3, T400: usa el generador, el hash y el correo del login, y guarda sus códigos con su propio adaptador
        assertInstanceOf(RecuperacionContrasenaService.class, contexto.getBean(RecuperacionContrasenaUseCase.class));
        assertInstanceOf(CodigoRecuperacionMongoAdapter.class, contexto.getBean(CodigoRecuperacionRepositoryPort.class));
    }
}
