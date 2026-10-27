package es.uclm.esibuy.backend.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.WebApplicationContext;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;

// Configuración de la sesión (HU43, T319). Los atributos de la cookie JSESSIONID y el tiempo de inactividad los
// aplica Tomcat, que @WebMvcTest no arranca: aquí se levanta la aplicación entera en un puerto aleatorio, con el
// MongoDB de Testcontainers (Docker Desktop abierto), y se hace una petición HTTP real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU43-CP02 y CP03, propuestos.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestcontainersConfig.class)
class ConfiguracionSesionTest {

    @LocalServerPort
    private int puerto;

    @Autowired
    private WebApplicationContext contexto;

    @Test
    @DisplayName("HU43-CP02: la sesión caduca a los 15 minutos sin actividad si no se configura otro tiempo")
    void laSesionCaducaA15MinutosDeInactividadPorDefecto() {
        // En minutos, como lo guarda el contenedor de servlets
        assertEquals(15, contexto.getServletContext().getSessionTimeout());
    }

    @Test
    @DisplayName("HU43-CP03: la cookie de sesión es Secure, HttpOnly y SameSite=Strict")
    void laCookieDeSesionEsSecureHttpOnlyYSameSiteStrict() throws Exception {
        // Sin sesión, Spring Security guarda la petición en una sesión nueva antes de responder 401: así el
        // servidor envía la cookie JSESSIONID tal como la configura Tomcat
        HttpResponse<Void> respuesta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/auth/me")).GET().build(),
                HttpResponse.BodyHandlers.discarding());

        List<String> cookies = respuesta.headers().allValues("Set-Cookie");
        String cookieSesion = cookies.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("La respuesta no crea la cookie de sesión: " + cookies));

        assertTrue(cookieSesion.contains("; Secure"), cookieSesion);
        assertTrue(cookieSesion.contains("; HttpOnly"), cookieSesion);
        assertTrue(cookieSesion.contains("; SameSite=Strict"), cookieSesion);
    }
}
