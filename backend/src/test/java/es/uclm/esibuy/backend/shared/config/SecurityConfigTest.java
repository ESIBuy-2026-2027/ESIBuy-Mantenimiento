package es.uclm.esibuy.backend.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Acceso a la zona de administración por rol (HU18, T58). Todavía no hay login ni endpoints de
// administración: los usuarios se simulan con @WithMockUser y la ruta la da un controlador de prueba,
// que solo existe en este test.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU18-CP01 a CP03, propuestos a partir de los criterios
// de aceptación de la HU18.
@WebMvcTest(SecurityConfigTest.AdministracionDePruebaController.class)
@Import({SecurityConfig.class, SecurityConfigTest.AdministracionDePruebaController.class})
class SecurityConfigTest {

    private static final String RUTA_ADMINISTRACION = "/api/admin/prueba";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("HU18-CP01: sin sesión no se accede a la zona de administración")
    void sinSesionNoSeAccedeAAdministracion() throws Exception {
        // 403 y no 401: todavía no hay ningún mecanismo de login configurado (task 230).
        mockMvc.perform(get(RUTA_ADMINISTRACION)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("HU18-CP02: un cliente no accede a la zona de administración")
    void clienteNoAccedeAAdministracion() throws Exception {
        mockMvc.perform(get(RUTA_ADMINISTRACION)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    @DisplayName("HU18-CP02: un vendedor no accede a la zona de administración")
    void vendedorNoAccedeAAdministracion() throws Exception {
        mockMvc.perform(get(RUTA_ADMINISTRACION)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("HU18-CP03: un administrador accede a la zona de administración")
    void administradorAccedeAAdministracion() throws Exception {
        mockMvc.perform(get(RUTA_ADMINISTRACION)).andExpect(status().isOk());
    }

    // Endpoint de administración de prueba: así el test comprueba un 200 real sin depender de los
    // endpoints de otras tasks (HU19 a HU24).
    @RestController
    static class AdministracionDePruebaController {

        @GetMapping(RUTA_ADMINISTRACION)
        String responder() {
            return "ok";
        }
    }
}
