package es.uclm.esibuy.backend.preferencias.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

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

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;

// Seguridad en la capa de negocio (R8): las preferencias exigen el rol CLIENTE y que el correo recibido sea el de la
// sesión. Las anotaciones solo se evalúan con Spring, así que se usa un contexto mínimo (sin MongoDB) con el servicio
// real y dobles de prueba de los puertos de salida.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU46-CP10, propuesto.
@SpringJUnitConfig(SeguridadPreferenciasServiceTest.Configuracion.class)
class SeguridadPreferenciasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";

    @Configuration
    @EnableMethodSecurity
    static class Configuracion {
        @Bean
        PreferenciasRepositoryPort preferenciasRepository() {
            return mock(PreferenciasRepositoryPort.class);
        }

        @Bean
        ClienteEnSesionPort clienteEnSesion() {
            return mock(ClienteEnSesionPort.class);
        }

        @Bean
        CategoriasPort categorias() {
            return mock(CategoriasPort.class);
        }

        @Bean
        GestionPreferenciasUseCase gestionPreferencias(PreferenciasRepositoryPort repositorio,
                ClienteEnSesionPort sesion, CategoriasPort categorias) {
            return new GestionPreferenciasService(repositorio, sesion, categorias);
        }
    }

    @Autowired
    private PreferenciasRepositoryPort preferenciasRepository;
    @Autowired
    private ClienteEnSesionPort clienteEnSesion;
    @Autowired
    private CategoriasPort categorias;
    @Autowired
    private GestionPreferenciasUseCase gestionPreferencias;

    @BeforeEach
    void limpiarDobles() {
        reset(preferenciasRepository, clienteEnSesion, categorias);
    }

    private void comprobarQueRechaza(String email) {
        assertThrows(AccessDeniedException.class, () -> gestionPreferencias.consultar(email));
        assertThrows(AccessDeniedException.class, () -> gestionPreferencias.guardar(email, List.of()));
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP10: el cliente consulta y guarda sus preferencias con su propio correo")
    void elClienteUsaSusPreferencias() {
        when(preferenciasRepository.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertDoesNotThrow(() -> gestionPreferencias.consultar(CLIENTE));
        assertDoesNotThrow(() -> gestionPreferencias.guardar(CLIENTE, List.of()));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP10: un cliente no consulta ni guarda las preferencias de otro cliente")
    void unClienteNoUsaLasPreferenciasDeOtro() {
        comprobarQueRechaza(OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU46-CP10: un vendedor no tiene preferencias")
    void unVendedorNoTienePreferencias() {
        comprobarQueRechaza(CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU46-CP10: un administrador no tiene preferencias")
    void unAdministradorNoTienePreferencias() {
        comprobarQueRechaza(CLIENTE);
    }

    @Test
    @DisplayName("HU46-CP10: sin sesión se rechazan las preferencias")
    void sinSesionSeRechazanLasPreferencias() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gestionPreferencias.consultar(CLIENTE));
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> gestionPreferencias.guardar(CLIENTE, List.of()));
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP25: el cliente elimina sus preferencias con su propio correo")
    void elClienteEliminaSusPreferenciasConSuPropioCorreo() {
        assertDoesNotThrow(() -> gestionPreferencias.eliminar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP25: un cliente no elimina las preferencias de otro cliente")
    void unClienteNoEliminaLasPreferenciasDeOtro() {
        assertThrows(AccessDeniedException.class, () -> gestionPreferencias.eliminar(OTRO_CLIENTE));
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    @Test
    @WithMockUser(username = "vendedor@esibuy.test", roles = "VENDEDOR")
    @DisplayName("HU46-CP25: un vendedor no elimina preferencias de clientes")
    void unVendedorNoEliminaPreferencias() {
        assertThrows(AccessDeniedException.class, () -> gestionPreferencias.eliminar("vendedor@esibuy.test"));
    }

    @Test
    @WithMockUser(username = "admin@esibuy.test", roles = "ADMINISTRADOR")
    @DisplayName("HU46-CP25: un administrador no elimina preferencias de clientes")
    void unAdministradorNoEliminaPreferencias() {
        assertThrows(AccessDeniedException.class, () -> gestionPreferencias.eliminar("admin@esibuy.test"));
    }

    @Test
    @DisplayName("HU46-CP25: sin sesión no se eliminan las preferencias")
    void sinSesionNoSeEliminanLasPreferencias() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> gestionPreferencias.eliminar(CLIENTE));
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }
}
