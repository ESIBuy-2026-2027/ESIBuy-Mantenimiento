package es.uclm.esibuy.backend.preferencias.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

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
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP10 y HU528-CP12, propuestos.
@SpringJUnitConfig(SeguridadPreferenciasServiceTest.Configuracion.class)
class SeguridadPreferenciasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String OTRO_CLIENTE = "otro@esibuy.test";
    private static final String CATEGORIA = "cat-1";

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
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP10: el cliente consulta sus preferencias con su propio correo")
    void elClienteUsaSusPreferencias() {
        assertDoesNotThrow(() -> gestionPreferencias.consultar(CLIENTE));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU46-CP10: un cliente no consulta las preferencias de otro cliente")
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
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    private void comprobarQueRechazaLasFavoritas(Class<? extends Exception> error, String email) {
        assertThrows(error, () -> gestionPreferencias.marcar(email, CATEGORIA));
        assertThrows(error, () -> gestionPreferencias.desmarcar(email, CATEGORIA));
        verifyNoInteractions(preferenciasRepository, clienteEnSesion, categorias);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU528-CP12: el cliente marca y desmarca sus categorías favoritas con su propio correo")
    void elClienteMarcaYDesmarcaSusFavoritas() {
        when(categorias.activas()).thenReturn(Set.of(CATEGORIA));

        assertDoesNotThrow(() -> gestionPreferencias.marcar(CLIENTE, CATEGORIA));
        assertDoesNotThrow(() -> gestionPreferencias.desmarcar(CLIENTE, CATEGORIA));
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "CLIENTE")
    @DisplayName("HU528-CP12: un cliente no marca ni desmarca las categorías favoritas de otro cliente")
    void unClienteNoCambiaLasFavoritasDeOtro() {
        comprobarQueRechazaLasFavoritas(AccessDeniedException.class, OTRO_CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "VENDEDOR")
    @DisplayName("HU528-CP12: un vendedor no marca ni desmarca categorías favoritas")
    void unVendedorNoTieneFavoritas() {
        comprobarQueRechazaLasFavoritas(AccessDeniedException.class, CLIENTE);
    }

    @Test
    @WithMockUser(username = CLIENTE, roles = "ADMINISTRADOR")
    @DisplayName("HU528-CP12: un administrador no marca ni desmarca categorías favoritas")
    void unAdministradorNoTieneFavoritas() {
        comprobarQueRechazaLasFavoritas(AccessDeniedException.class, CLIENTE);
    }

    @Test
    @DisplayName("HU528-CP12: sin sesión no se marcan ni se desmarcan categorías favoritas")
    void sinSesionNoSeCambianLasFavoritas() {
        comprobarQueRechazaLasFavoritas(AuthenticationCredentialsNotFoundException.class, CLIENTE);
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
