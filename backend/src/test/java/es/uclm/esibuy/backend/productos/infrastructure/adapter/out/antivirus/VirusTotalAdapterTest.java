package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus;

import es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Análisis de la URL de la imagen de un producto con la API v3 de VirusTotal (HU31, T397). VirusTotal no se llama de
// verdad: MockRestServiceServer hace de servidor y comprueba qué peticiones llegan. Sin esperas reales entre consultas
// (Duration.ZERO). Una imagen es maliciosa si algún motor la marca como malicious (decisión del equipo).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP29 a CP31, propuestos.
class VirusTotalAdapterTest {

    private static final String BASE = "https://www.virustotal.com/api/v3";
    private static final String IMAGEN = "https://cdn.esibuy.test/teclado.png";
    // VirusTotal identifica una URL por la propia URL en Base64 URL-safe sin el relleno '=' del final
    private static final String ID_URL = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(IMAGEN.getBytes(StandardCharsets.UTF_8));
    private static final String ID_ANALISIS = "u-1a2b3c-1730000000";

    private MockRestServiceServer virusTotal;
    private VirusTotalAdapter adapter;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        virusTotal = MockRestServiceServer.bindTo(builder).build();
        adapter = new VirusTotalAdapter(builder.build(), 2, Duration.ZERO);
    }

    @Test
    @DisplayName("HU31-CP29: una URL ya analizada que algún motor marca como maliciosa es MALICIOSA")
    void unaUrlConInformeMaliciosoEsMaliciosa() {
        virusTotal.expect(requestTo(BASE + "/urls/" + ID_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(informe(1, 0), MediaType.APPLICATION_JSON));

        assertEquals(ResultadoAnalisis.MALICIOSA, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP29: una URL ya analizada que ningún motor marca como maliciosa es SEGURA, aunque haya sospechosos")
    void unaUrlConInformeSinMaliciososEsSegura() {
        virusTotal.expect(requestTo(BASE + "/urls/" + ID_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(informe(0, 3), MediaType.APPLICATION_JSON));

        assertEquals(ResultadoAnalisis.SEGURA, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP30: una URL que VirusTotal no conoce se envía a analizar y se usa el análisis cuando termina")
    void unaUrlNuevaSeEnviaAAnalizarYSeEsperaAlResultado() {
        esperarUrlDesconocida();
        esperarEnvioAAnalizar();
        esperarConsultaDelAnalisis(analisis("queued", 0));
        esperarConsultaDelAnalisis(analisis("completed", 1));

        assertEquals(ResultadoAnalisis.MALICIOSA, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP30: si el análisis termina en la primera consulta no se vuelve a consultar")
    void siElAnalisisTerminaEnLaPrimeraConsultaNoSeVuelveAConsultar() {
        esperarUrlDesconocida();
        esperarEnvioAAnalizar();
        esperarConsultaDelAnalisis(analisis("completed", 0));

        assertEquals(ResultadoAnalisis.SEGURA, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP30: si el análisis no termina tras el máximo de consultas no hay resultado")
    void siElAnalisisNoTerminaNoHayResultado() {
        esperarUrlDesconocida();
        esperarEnvioAAnalizar();
        esperarConsultaDelAnalisis(analisis("queued", 0));
        esperarConsultaDelAnalisis(analisis("in-progress", 0));

        assertEquals(ResultadoAnalisis.SIN_RESULTADO, adapter.analizar(IMAGEN));
        virusTotal.verify();   // solo 2 consultas (el máximo del adaptador): una tercera haría fallar el test
    }

    // 401 WrongCredentialsError (API key mal), 429 QuotaExceededError (4/min o 500/día), 500 y 503 (VirusTotal caído)
    @ParameterizedTest
    @ValueSource(ints = {401, 429, 500, 503})
    @DisplayName("HU31-CP31: si VirusTotal responde con un error al buscar el informe no hay resultado")
    void unErrorAlBuscarElInformeNoDaResultado(int estado) {
        virusTotal.expect(requestTo(BASE + "/urls/" + ID_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.valueOf(estado)).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"code\": \"x\", \"message\": \"x\"}}"));

        assertEquals(ResultadoAnalisis.SIN_RESULTADO, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP31: si se acaba la cuota al enviar una URL nueva a analizar no hay resultado")
    void sinCuotaAlEnviarAAnalizarNoHayResultado() {
        esperarUrlDesconocida();
        virusTotal.expect(requestTo(BASE + "/urls")).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertEquals(ResultadoAnalisis.SIN_RESULTADO, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    @Test
    @DisplayName("HU31-CP31: si no se puede conectar con VirusTotal (red caída o tiempo agotado) no hay resultado")
    void sinConexionNoHayResultado() {
        virusTotal.expect(requestTo(BASE + "/urls/" + ID_URL))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertEquals(ResultadoAnalisis.SIN_RESULTADO, adapter.analizar(IMAGEN));
        virusTotal.verify();
    }

    // GET /urls/{id} de una URL que nadie ha analizado: 404 NotFoundError
    private void esperarUrlDesconocida() {
        virusTotal.expect(requestTo(BASE + "/urls/" + ID_URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"code\": \"NotFoundError\", \"message\": \"URL not found\"}}"));
    }

    // POST /urls con la URL en un formulario: VirusTotal devuelve el id del análisis
    private void esperarEnvioAAnalizar() {
        virusTotal.expect(requestTo(BASE + "/urls")).andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(Map.of("url", IMAGEN)))
                .andRespond(withSuccess("{\"data\": {\"type\": \"analysis\", \"id\": \"" + ID_ANALISIS + "\"}}",
                        MediaType.APPLICATION_JSON));
    }

    private void esperarConsultaDelAnalisis(String respuesta) {
        virusTotal.expect(requestTo(BASE + "/analyses/" + ID_ANALISIS)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(respuesta, MediaType.APPLICATION_JSON));
    }

    // Informe de una URL (GET /urls/{id}) con el recuento de motores de su último análisis
    private static String informe(int maliciosos, int sospechosos) {
        return """
                {"data": {"type": "url", "id": "x", "attributes": {"last_analysis_stats":
                  {"malicious": %d, "suspicious": %d, "harmless": 60, "undetected": 20, "timeout": 0}}}}
                """.formatted(maliciosos, sospechosos);
    }

    // Análisis (GET /analyses/{id}): status queued, in-progress o completed, con el recuento de motores
    private static String analisis(String estado, int maliciosos) {
        return """
                {"data": {"type": "analysis", "id": "%s", "attributes": {"status": "%s", "stats":
                  {"malicious": %d, "suspicious": 0, "harmless": 60, "undetected": 20, "timeout": 0}}}}
                """.formatted(ID_ANALISIS, estado, maliciosos);
    }
}
