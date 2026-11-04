package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarClienteCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarVendedorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoAuthenticatorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Seguridad en la capa de negocio (R8): el registro y los pasos del login son públicos (se llaman sin sesión);
// consultar al usuario en sesión exige estar autenticado y que el correo sea el de la sesión; el perfil y el id
// del usuario son del cliente y del vendedor, y saber si es premium, solo del cliente. Las anotaciones solo se
// evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con los servicios reales y dobles de
// prueba de los puertos de salida.
// Eliminar la cuenta propia (HU25) es solo del cliente y con su propio correo.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU2-CP29 y HU25-CP04, propuestos.
@SpringJUnitConfig(SeguridadSesionUsuarioServiceTest.Configuracion.class)
class SeguridadSesionUsuarioServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String VENDEDOR = "vendedor@esibuy.test";
    private static final String ADMIN = "admin@esibuy.test";
    private static final String OTRO = "otro@esibuy.test";
    private static final String PASSWORD = "Clave-Segura-123";

    private static final IniciarSesionCommand INICIO_SESION = new IniciarSesionCommand(CLIENTE, "Clave-Segura-123");
    private static final VerificarCodigoEmailCommand CODIGO_EMAIL = new VerificarCodigoEmailCommand(CLIENTE, "123456");
    private static final VerificarCodigoAuthenticatorCommand CODIGO_AUTHENTICATOR =
            new VerificarCodigoAuthenticatorCommand(CLIENTE, "123456");
    // Sin datos: el servicio los rechaza por inválidos, lo que demuestra que la seguridad ha dejado pasar la llamada
    private static final RegistrarClienteCommand REGISTRO_CLIENTE =
            new RegistrarClienteCommand(null, null, null, null, null, null, null, null, null, null, false);
    private static final RegistrarVendedorCommand REGISTRO_VENDEDOR =
            new RegistrarVendedorCommand(null, null, null, null, null, null, null, null, null, null, false);

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        UsuarioRepositoryPort usuarioRepository() {
            return mock(UsuarioRepositoryPort.class);
        }

        @Bean
        HashContrasenaPort hashContrasena() {
            return mock(HashContrasenaPort.class);
        }

        @Bean
        CodigoVerificacionRepositoryPort codigoVerificacionRepository() {
            return mock(CodigoVerificacionRepositoryPort.class);
        }

        @Bean
        GeneradorCodigoPort generadorCodigo() {
            return mock(GeneradorCodigoPort.class);
        }

        @Bean
        EnvioEmailPort envioEmail() {
            return mock(EnvioEmailPort.class);
        }

        @Bean
        FactorAutenticacionRepositoryPort factorAutenticacionRepository() {
            return mock(FactorAutenticacionRepositoryPort.class);
        }

        @Bean
        TotpPort totp() {
            return mock(TotpPort.class);
        }

        @Bean
        CifradoSecretoPort cifradoSecreto() {
            return mock(CifradoSecretoPort.class);
        }

        @Bean
        AutenticacionUseCase autenticacion(UsuarioRepositoryPort repositorio, HashContrasenaPort hash,
                CodigoVerificacionRepositoryPort codigos, GeneradorCodigoPort generador, EnvioEmailPort email,
                FactorAutenticacionRepositoryPort factores) {
            return new AutenticacionService(repositorio, hash, codigos, generador, email, factores,
                    Clock.systemUTC());
        }

        @Bean
        AuthenticatorUseCase authenticator(UsuarioRepositoryPort repositorio,
                FactorAutenticacionRepositoryPort factores, TotpPort totp, CifradoSecretoPort cifrado) {
            return new AuthenticatorService(repositorio, factores, totp, cifrado, Clock.systemUTC());
        }

        @Bean
        RegistroUsuarioUseCase registroUsuario(UsuarioRepositoryPort repositorio, HashContrasenaPort hash) {
            return new RegistroUsuarioService(repositorio, hash, Clock.systemUTC());
        }

        @Bean
        GestionPerfilUseCase gestionPerfil(UsuarioRepositoryPort repositorio, HashContrasenaPort hash) {
            return new GestionPerfilService(repositorio, hash);
        }
    }

    @Autowired
    private UsuarioRepositoryPort usuarioRepository;
    @Autowired
    private HashContrasenaPort hashContrasena;
    @Autowired
    private AutenticacionUseCase autenticacion;
    @Autowired
    private AuthenticatorUseCase authenticator;
    @Autowired
    private RegistroUsuarioUseCase registroUsuario;
    @Autowired
    private GestionPerfilUseCase gestionPerfil;

    @BeforeEach
    void limpiarDobles() {
        reset(usuarioRepository, hashContrasena);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU2-CP29: el registro y los pasos del login se pueden usar sin sesión")
    void elRegistroYElLoginSonPublicos() {
        assertAll(
                () -> assertThrows(UsuarioInvalidoException.class, () -> registroUsuario.registrarCliente(REGISTRO_CLIENTE)),
                () -> assertThrows(UsuarioInvalidoException.class, () -> registroUsuario.registrarVendedor(REGISTRO_VENDEDOR)),
                () -> assertThrows(CredencialesInvalidasException.class, () -> autenticacion.iniciarSesion(INICIO_SESION)),
                () -> assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(CODIGO_EMAIL)),
                () -> assertThrows(CredencialesInvalidasException.class, () -> authenticator.prepararAlta(CLIENTE)),
                () -> assertThrows(CodigoInvalidoException.class,
                        () -> authenticator.verificarCodigo(CODIGO_AUTHENTICATOR)));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU2-CP29: nadie consulta al usuario en sesión, su id, su perfil ni si es premium con el correo de otro")
    void nadieConsultaConElCorreoDeOtro() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.consultarUsuarioEnSesion(OTRO)),
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.consultarIdUsuarioEnSesion(OTRO)),
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.esClientePremium(OTRO)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionPerfil.consultarPerfil(OTRO)));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU2-CP29: un administrador consulta su propio usuario en sesión, pero no id, perfil ni premium")
    void unAdministradorSoloConsultaSuUsuarioEnSesion() {
        assertAll(
                () -> assertThrows(CredencialesInvalidasException.class,
                        () -> autenticacion.consultarUsuarioEnSesion(ADMIN)),
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.consultarIdUsuarioEnSesion(ADMIN)),
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.esClientePremium(ADMIN)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionPerfil.consultarPerfil(ADMIN)));
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU2-CP29: un vendedor consulta su usuario, su id y su perfil, pero no si es premium")
    void unVendedorNoConsultaSiEsPremium() {
        assertAll(
                () -> assertThrows(CredencialesInvalidasException.class,
                        () -> autenticacion.consultarUsuarioEnSesion(VENDEDOR)),
                () -> assertThrows(CredencialesInvalidasException.class,
                        () -> autenticacion.consultarIdUsuarioEnSesion(VENDEDOR)),
                () -> assertThrows(CredencialesInvalidasException.class, () -> gestionPerfil.consultarPerfil(VENDEDOR)),
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.esClientePremium(VENDEDOR)));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU2-CP29: un cliente sí consulta su usuario, su id, su perfil y si es premium")
    void unClienteConsultaLoSuyo() {
        assertAll(
                () -> assertThrows(CredencialesInvalidasException.class,
                        () -> autenticacion.consultarUsuarioEnSesion(CLIENTE)),
                () -> assertThrows(CredencialesInvalidasException.class,
                        () -> autenticacion.consultarIdUsuarioEnSesion(CLIENTE)),
                () -> assertThrows(CredencialesInvalidasException.class, () -> gestionPerfil.consultarPerfil(CLIENTE)),
                () -> assertThrows(CredencialesInvalidasException.class, () -> autenticacion.esClientePremium(CLIENTE)));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("HU2-CP29: un usuario anónimo no consulta al usuario en sesión ni su perfil")
    void unAnonimoNoConsultaNada() {
        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> autenticacion.consultarUsuarioEnSesion(CLIENTE)),
                () -> assertThrows(AccessDeniedException.class, () -> gestionPerfil.consultarPerfil(CLIENTE)));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("HU2-CP29: sin sesión se rechazan las consultas del usuario en sesión")
    void sinSesionSeRechazanLasConsultas() {
        assertAll(
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> autenticacion.consultarUsuarioEnSesion(CLIENTE)),
                () -> assertThrows(AuthenticationCredentialsNotFoundException.class,
                        () -> gestionPerfil.consultarPerfil(CLIENTE)));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU25-CP04: un cliente elimina su propia cuenta")
    void unClienteEliminaSuCuenta() {
        // El repositorio y el hash dejan que la llamada termine: así se ve que la seguridad la ha dejado pasar
        when(usuarioRepository.buscarPorCorreo(CLIENTE)).thenReturn(Optional.of(cliente()));
        when(hashContrasena.coincide(PASSWORD, "hash-simulado")).thenReturn(true);

        gestionPerfil.eliminarCuenta(CLIENTE, PASSWORD);

        verify(usuarioRepository).eliminarPorId("u-1");
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU25-CP04: un cliente no elimina la cuenta de otro")
    void unClienteNoEliminaLaCuentaDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionPerfil.eliminarCuenta(OTRO, PASSWORD));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU25-CP04: un vendedor no elimina su cuenta por aquí")
    void unVendedorNoEliminaSuCuenta() {
        assertThrows(AccessDeniedException.class, () -> gestionPerfil.eliminarCuenta(VENDEDOR, PASSWORD));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMINISTRADOR")
    @DisplayName("HU25-CP04: un administrador no elimina su cuenta por aquí")
    void unAdministradorNoEliminaSuCuenta() {
        assertThrows(AccessDeniedException.class, () -> gestionPerfil.eliminarCuenta(ADMIN, PASSWORD));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    @Test
    @DisplayName("HU25-CP04: sin sesión no se elimina ninguna cuenta")
    void sinSesionNoSeEliminaNingunaCuenta() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionPerfil.eliminarCuenta(CLIENTE, PASSWORD));
        verifyNoInteractions(usuarioRepository, hashContrasena);
    }

    private static Usuario cliente() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null), new Email(CLIENTE),
                "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, Month.MAY, 20), null),
                null);
    }
}