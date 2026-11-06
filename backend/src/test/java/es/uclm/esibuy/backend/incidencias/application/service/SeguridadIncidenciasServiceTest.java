package es.uclm.esibuy.backend.incidencias.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;

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

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;

// Seguridad en la capa de negocio (R8): crear una incidencia exige el rol CLIENTE o VENDEDOR y que el correo del
// comando sea el de la sesión; el administrador no crea incidencias. Las anotaciones solo se evalúan con Spring, así
// que se usa un contexto mínimo (sin MongoDB) con el servicio real y dobles de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU44-CP10, propuesto.
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
    }

    @Autowired
    private IncidenciaRepositoryPort incidenciaRepository;
    @Autowired
    private AutorEnSesionPort autorEnSesion;
    @Autowired
    private CreacionIncidenciasUseCase creacionIncidencias;

    @BeforeEach
    void limpiarDobles() {
        reset(incidenciaRepository, autorEnSesion);
    }

    private static CrearIncidenciaCommand comando(String email) {
        return new CrearIncidenciaCommand(email, "Asunto", "Descripción");
    }

    private void comprobarQueRechaza(String email) {
        CrearIncidenciaCommand comando = comando(email);

        assertThrows(AccessDeniedException.class, () -> creacionIncidencias.crear(comando));
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
}