package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import es.uclm.esibuy.backend.incidencias.application.port.in.ConsultaIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.application.port.in.ResolucionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutoresPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

// Seguridad en la capa de negocio (R8): crear una incidencia exige el rol CLIENTE o VENDEDOR y que el correo del
// comando sea el de la sesión; el administrador no crea incidencias. Las anotaciones solo se evalúan con Spring, así
// que se usa un contexto mínimo (sin MongoDB) con el servicio real y dobles de los puertos de salida.
// Ver las incidencias de todos (HU49) y abrirlas o cerrarlas (HU50) es solo del administrador.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP10, HU49-CP09 y HU50-CP09, propuestos.
@SpringJUnitConfig(SeguridadIncidenciasServiceTest.Configuracion.class)
class SeguridadIncidenciasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String VENDEDOR = "vendedor@esibuy.test";
    private static final String ADMINISTRADOR = "admin@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        IncidenciaRepositoryPort incidenciaRepository() {
            return mock(IncidenciaRepositoryPort.class);
        }

        @Bean
        AutorEnSesionPort autorEnSesion() {
            return mock(AutorEnSesionPort.class);
        }

        @Bean
        CreacionIncidenciasUseCase creacionIncidencias(IncidenciaRepositoryPort repositorio,
                AutorEnSesionPort sesion) {
            return new CreacionIncidenciasService(repositorio, sesion, Clock.systemUTC());
        }

        @Bean
        ConsultaIncidenciasUseCase consultaIncidencias(IncidenciaRepositoryPort repositorio, AutorEnSesionPort sesion) {
            return new ConsultaIncidenciasService(repositorio, sesion);
        }

        @Bean
        AutoresPort autores() {
            return mock(AutoresPort.class);
        }

        @Bean
        SupervisionIncidenciasUseCase supervisionIncidencias(IncidenciaRepositoryPort repositorio, AutoresPort autores) {
            return new SupervisionIncidenciasService(repositorio, autores);
        }

        @Bean
        ResolucionIncidenciasUseCase resolucionIncidencias(IncidenciaRepositoryPort repositorio) {
            return new ResolucionIncidenciasService(repositorio);
        }
    }

    @Autowired
    private IncidenciaRepositoryPort incidenciaRepository;
    @Autowired
    private AutorEnSesionPort autorEnSesion;
    @Autowired
    private CreacionIncidenciasUseCase creacionIncidencias;
    @Autowired
    private ConsultaIncidenciasUseCase consultaIncidencias;
    @Autowired
    private AutoresPort autores;
    @Autowired
    private SupervisionIncidenciasUseCase supervisionIncidencias;
    @Autowired
    private ResolucionIncidenciasUseCase resolucionIncidencias;

    @BeforeEach
    void limpiarDobles() {
        reset(incidenciaRepository, autorEnSesion, autores);
    }

    private static CrearIncidenciaCommand comando(String email) {
        return new CrearIncidenciaCommand(email, "Asunto", "Descripción");
    }

    private void comprobarQueRechaza(String email) {
        CrearIncidenciaCommand comando = comando(email);

        assertThrows(AccessDeniedException.class, () -> creacionIncidencias.crear(comando));
        verifyNoInteractions(incidenciaRepository);
    }

    private void comprobarQueRechazaLaConsulta(String email) {
        assertThrows(AccessDeniedException.class, () -> consultaIncidencias.consultarPropias(email, 0, 10));
        verifyNoInteractions(incidenciaRepository);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU44-CP10: el cliente crea una incidencia con su propio correo")
    void elClienteCreaConSuPropioCorreo() {
        when(autorEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        CrearIncidenciaCommand comando = comando(CLIENTE);

        assertDoesNotThrow(() -> creacionIncidencias.crear(comando));
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU44-CP10: el vendedor crea una incidencia con su propio correo")
    void elVendedorCreaConSuPropioCorreo() {
        when(autorEnSesion.idPorCorreo(VENDEDOR)).thenReturn("v-1");
        CrearIncidenciaCommand comando = comando(VENDEDOR);

        assertDoesNotThrow(() -> creacionIncidencias.crear(comando));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU44-CP10: un cliente no crea una incidencia con el correo de otro usuario")
    void unClienteNoCreaConElCorreoDeOtro() {
        comprobarQueRechaza(VENDEDOR);
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU44-CP10: un vendedor no crea una incidencia con el correo de otro usuario")
    void unVendedorNoCreaConElCorreoDeOtro() {
        comprobarQueRechaza(CLIENTE);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU44-CP10: un administrador no crea incidencias, ni siquiera con su propio correo")
    void unAdministradorNoCrea() {
        comprobarQueRechaza(ADMINISTRADOR);
    }

    @Test
    @DisplayName("HU44-CP10: sin sesión se rechaza")
    void sinSesionSeRechaza() {
        CrearIncidenciaCommand comando = comando(CLIENTE);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> creacionIncidencias.crear(comando));
        verifyNoInteractions(incidenciaRepository);
    }

    
    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU44-CP19: el cliente consulta sus incidencias con su propio correo")
    void elClienteConsultaConSuPropioCorreo() {
        when(autorEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");

        assertDoesNotThrow(() -> consultaIncidencias.consultarPropias(CLIENTE, 0, 10));
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU44-CP19: el vendedor consulta sus incidencias con su propio correo")
    void elVendedorConsultaConSuPropioCorreo() {
        when(autorEnSesion.idPorCorreo(VENDEDOR)).thenReturn("v-1");

        assertDoesNotThrow(() -> consultaIncidencias.consultarPropias(VENDEDOR, 0, 10));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU44-CP19: un cliente no consulta las incidencias con el correo de otro usuario")
    void unClienteNoConsultaConElCorreoDeOtro() {
        comprobarQueRechazaLaConsulta(VENDEDOR);
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU44-CP19: un vendedor no consulta las incidencias con el correo de otro usuario")
    void unVendedorNoConsultaConElCorreoDeOtro() {
        comprobarQueRechazaLaConsulta(CLIENTE);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU44-CP19: un administrador no consulta incidencias por aquí, ni siquiera con su propio correo")
    void unAdministradorNoConsulta() {
        comprobarQueRechazaLaConsulta(ADMINISTRADOR);
    }

    @Test
    @DisplayName("HU44-CP19: sin sesión no se consultan las incidencias")
    void sinSesionNoSeConsulta() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> consultaIncidencias.consultarPropias(CLIENTE, 0, 10));
        verifyNoInteractions(incidenciaRepository);
    }

    // Supervisión (HU49): solo el administrador ve las incidencias de todos

    private void comprobarQueRechazaLaSupervision() {
        assertThrows(AccessDeniedException.class, () -> supervisionIncidencias.consultarTodas(0, 10));
        verifyNoInteractions(incidenciaRepository, autores);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU49-CP09: el administrador consulta las incidencias de todos")
    void elAdministradorSupervisaLasIncidencias() {
        when(incidenciaRepository.buscarTodas(0, 10)).thenReturn(new IncidenciaPage(List.of(), 0, 10, 0));

        assertDoesNotThrow(() -> supervisionIncidencias.consultarTodas(0, 10));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU49-CP09: un cliente no consulta las incidencias de todos")
    void unClienteNoSupervisaLasIncidencias() {
        comprobarQueRechazaLaSupervision();
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU49-CP09: un vendedor no consulta las incidencias de todos")
    void unVendedorNoSupervisaLasIncidencias() {
        comprobarQueRechazaLaSupervision();
    }

    @Test
    @DisplayName("HU49-CP09: sin sesión no se consultan las incidencias de todos")
    void sinSesionNoSeSupervisan() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> supervisionIncidencias.consultarTodas(0, 10));
        verifyNoInteractions(incidenciaRepository, autores);
    }

    // Resolución (HU50): solo el administrador abre y cierra incidencias

    private static final String ID = "i-1";

    private void comprobarQueRechazaAbrir() {
        assertThrows(AccessDeniedException.class, () -> resolucionIncidencias.abrir(ID, "Respuesta"));
        verifyNoInteractions(incidenciaRepository);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU50-CP09: el administrador abre una incidencia")
    void elAdministradorAbreUnaIncidencia() {
        Incidencia pendiente = Incidencia.nueva("u-1", "Asunto", "Descripción", Instant.EPOCH);
        Incidencia conId = new Incidencia(ID, pendiente.autorId(), pendiente.asunto(), pendiente.descripcion(),
                pendiente.estado(), pendiente.respuesta(), pendiente.fechaCreacion());
        when(incidenciaRepository.buscarPorId(ID)).thenReturn(Optional.of(conId));
        when(incidenciaRepository.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertDoesNotThrow(() -> resolucionIncidencias.abrir(ID, "Respuesta"));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU50-CP09: un cliente no abre incidencias")
    void unClienteNoAbreIncidencias() {
        comprobarQueRechazaAbrir();
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU50-CP09: un vendedor no abre incidencias")
    void unVendedorNoAbreIncidencias() {
        comprobarQueRechazaAbrir();
    }

    @Test
    @DisplayName("HU50-CP09: sin sesión no se abren incidencias")
    void sinSesionNoSeAbrenIncidencias() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> resolucionIncidencias.abrir(ID, "Respuesta"));
        verifyNoInteractions(incidenciaRepository);
    }

    private void comprobarQueRechazaCerrar() {
        assertThrows(AccessDeniedException.class, () -> resolucionIncidencias.cerrar(ID));
        verifyNoInteractions(incidenciaRepository);
    }

    @Test
    @WithMockUser(username = ADMINISTRADOR, roles = "ADMINISTRADOR")
    @DisplayName("HU50-CP09: el administrador cierra una incidencia")
    void elAdministradorCierraUnaIncidencia() {
        when(incidenciaRepository.buscarPorId(ID)).thenReturn(Optional.of(
                new Incidencia(ID, "u-1", "Asunto", "Descripción", EstadoIncidencia.PENDIENTE, "", Instant.EPOCH)));

        assertDoesNotThrow(() -> resolucionIncidencias.cerrar(ID));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU50-CP09: un cliente no cierra incidencias")
    void unClienteNoCierraIncidencias() {
        comprobarQueRechazaCerrar();
    }

    @Test
    @WithMockUser(username = VENDEDOR, roles = "VENDEDOR")
    @DisplayName("HU50-CP09: un vendedor no cierra incidencias")
    void unVendedorNoCierraIncidencias() {
        comprobarQueRechazaCerrar();
    }

    @Test
    @DisplayName("HU50-CP09: sin sesión no se cierran incidencias")
    void sinSesionNoSeCierranIncidencias() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> resolucionIncidencias.cerrar(ID));
        verifyNoInteractions(incidenciaRepository);
    }
}