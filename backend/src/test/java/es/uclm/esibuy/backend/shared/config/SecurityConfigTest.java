package es.uclm.esibuy.backend.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Acceso a la zona de administración por rol (HU18, T58). Todavía no hay login ni endpoints de
// administración: los usuarios se simulan con @WithMockUser y la ruta la da un controlador de prueba,
// que solo existe en este test.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU18-CP01 a CP03, propuestos a partir de los criterios
// de aceptación de la HU18.
@WebMvcTest({SecurityConfigTest.AdministracionDePruebaController.class, SecurityConfigTest.VendedorDePruebaController.class})
@Import({SecurityConfig.class, SecurityConfigTest.AdministracionDePruebaController.class,
        SecurityConfigTest.VendedorDePruebaController.class})
class SecurityConfigTest {

    private static final String RUTA_ADMINISTRACION = "/api/admin/prueba";
    private static final String RUTA_VENDEDOR = "/api/seller/prueba";
    private static final String RUTA_LOGIN = "/api/auth/login";
    private static final String COOKIE_CSRF = "XSRF-TOKEN";
    private static final String CABECERA_CSRF = "X-XSRF-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("HU18-CP01: sin sesión no se accede a la zona de administración")
    void sinSesionNoSeAccedeAAdministracion() throws Exception {
        // 401 sin sesión («no sé quién eres») y 403 con sesión sin permiso: T230, D10 (antes, el 403 por defecto de Spring)
        mockMvc.perform(get(RUTA_ADMINISTRACION)).andExpect(status().isUnauthorized());
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

    // Protección CSRF para el frontend (T230, D4): el backend da el token en una cookie que el
    // JavaScript del frontend puede leer, y el frontend lo devuelve en una cabecera en cada POST.
    // Tests técnicos: no validan una HU concreta, sino la configuración de seguridad.

    @Test
    @DisplayName("Seguridad: cualquier respuesta, aunque sea un 401, entrega el token CSRF en una cookie legible por el frontend")
    void unaPeticionRecibeElTokenCsrfEnUnaCookie() throws Exception {
        // Es lo primero que hace el frontend al arrancar (GET /api/auth/me sin sesión): necesita el token
        // antes de su primer POST, que es el login
        mockMvc.perform(get(RUTA_ADMINISTRACION))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().exists(COOKIE_CSRF))
                // Sin HttpOnly: el JavaScript del frontend tiene que poder leerla (la de sesión sí es HttpOnly)
                .andExpect(cookie().httpOnly(COOKIE_CSRF, false));
    }

    @Test
    @DisplayName("HU43-CP03: la cookie del token CSRF solo viaja por HTTPS y no se envía desde otros sitios")
    void laCookieCsrfEsSecureYSameSiteStrict() throws Exception {
        // Sigue legible por el frontend (sin HttpOnly), pero Secure y SameSite=Strict como la de sesión (T319)
        mockMvc.perform(get(RUTA_ADMINISTRACION))
                .andExpect(cookie().secure(COOKIE_CSRF, true))
                .andExpect(cookie().sameSite(COOKIE_CSRF, "Strict"))
                .andExpect(cookie().httpOnly(COOKIE_CSRF, false));
    }

    @Test
    @DisplayName("Seguridad: un POST sin token CSRF se rechaza con 403")
    void unPostSinTokenCsrfSeRechaza() throws Exception {
        mockMvc.perform(post(RUTA_LOGIN)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: un POST que devuelve en la cabecera el token de la cookie pasa el filtro CSRF")
    void unPostConElTokenDeLaCookieEnLaCabeceraSeAcepta() throws Exception {
        // Lo que hará el frontend: leer la cookie y copiarla en la cabecera X-XSRF-TOKEN
        MvcResult primeraPeticion = mockMvc.perform(get(RUTA_ADMINISTRACION)).andReturn();
        Cookie tokenCsrf = primeraPeticion.getResponse().getCookie(COOKIE_CSRF);
        assertNotNull(tokenCsrf, "La primera respuesta debe traer la cookie del token CSRF");

        // 404 y no 403: el filtro CSRF deja pasar la petición, y no hay controlador de login en este test
        mockMvc.perform(post(RUTA_LOGIN)
                        .cookie(tokenCsrf)
                        .header(CABECERA_CSRF, tokenCsrf.getValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Seguridad: la página de error de Spring es accesible sin sesión y conserva el código del error")
    void laPaginaDeErrorConservaElCodigoSinSesion() throws Exception {
        // Cuando Spring Security rechaza una petición (p. ej. un POST sin token CSRF → 403), Spring Boot
        // reenvía internamente a /error para construir la respuesta. Si /error exigiera sesión, el 403 se
        // convertiría en un 401 (y cualquier 500 también). MockMvc no hace ese reenvío: se simula aquí
        // pidiendo /error con el código original, como hace el servidor real.
        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isForbidden());
    }

    // Zona de vendedor (HU15, T57): misma idea que la de administración, con su propio endpoint de prueba.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU15-CP01 a CP03, propuestos a partir de los criterios
    // de la HU15.

    @Test
    @DisplayName("HU15-CP01: sin sesión no se accede a la zona de vendedor")
    void sinSesionNoSeAccedeALaZonaDeVendedor() throws Exception {
        mockMvc.perform(get(RUTA_VENDEDOR)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    @DisplayName("HU15-CP02: un cliente o un administrador no accede a la zona de vendedor")
    void otroRolNoAccedeALaZonaDeVendedor(String rol) throws Exception {
        mockMvc.perform(get(RUTA_VENDEDOR).with(user("usuario").roles(rol))).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    @DisplayName("HU15-CP03: un vendedor accede a la zona de vendedor")
    void vendedorAccedeALaZonaDeVendedor() throws Exception {
        mockMvc.perform(get(RUTA_VENDEDOR)).andExpect(status().isOk());
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

    // Endpoint de la zona de vendedor de prueba: todavía no hay endpoints de vendedor (llegarán con
    // los productos, HU31 y HU32).
    @RestController
    static class VendedorDePruebaController {

        @GetMapping(RUTA_VENDEDOR)
        String responder() {
            return "ok";
        }
    }
}
