package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus;

import es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Análisis de la URL de la imagen de un producto con la API v3 de VirusTotal (HU31, T397). VirusTotal no se llama de
// verdad: MockRestServiceServer hace de servidor y comprueba qué peticiones llegan. Sin esperas reales entre consultas
// (Duration.ZERO). Una imagen es maliciosa si algún motor la marca como malicious (decisión del equipo).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU31-CP29, propuesto.
class VirusTotalAdapterTest {

    private static final String BASE = "https://www.virustotal.com/api/v3";
    private static final String IMAGEN = "https://cdn.esibuy.test/teclado.png";
    // VirusTotal identifica una URL por la propia URL en Base64 URL-safe sin el relleno '=' del final
    private static final String ID_URL = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(IMAGEN.getBytes(StandardCharsets.UTF_8));

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

    // Informe de una URL (GET /urls/{id}) con el recuento de motores de su último análisis
    private static String informe(int maliciosos, int sospechosos) {
        return """
                {"data": {"type": "url", "id": "x", "attributes": {"last_analysis_stats":
                  {"malicious": %d, "suspicious": %d, "harmless": 60, "undetected": 20, "timeout": 0}}}}
                """.formatted(maliciosos, sospechosos);
    }
}
