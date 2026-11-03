package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

// Adaptador de AnalisisImagenPort con la API v3 de VirusTotal (HU31, T397)
public class VirusTotalAdapter implements AnalisisImagenPort {

    private final RestClient virusTotal;
    private final int maxConsultas;
    private final Duration esperaEntreConsultas;

    public VirusTotalAdapter(RestClient virusTotal, int maxConsultas, Duration esperaEntreConsultas) {
        this.virusTotal = virusTotal;
        this.maxConsultas = maxConsultas;
        this.esperaEntreConsultas = esperaEntreConsultas;
    }

    @Override
    public ResultadoAnalisis analizar(String urlImagen) {
        InformeUrl informe = virusTotal.get()
                .uri("/urls/{id}", idDeUrl(urlImagen))
                .retrieve()
                .body(InformeUrl.class);
        return informe.data().attributes().estadisticas().malicious() > 0
                ? ResultadoAnalisis.MALICIOSA
                : ResultadoAnalisis.SEGURA;
    }

    // VirusTotal identifica una URL por la propia URL en Base64 URL-safe sin el relleno '='
    private static String idDeUrl(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }

    // Solo la parte del JSON de VirusTotal que se usa; el resto se ignora
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InformeUrl(Datos data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Datos(Atributos attributes) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Atributos(@JsonProperty("last_analysis_stats") Estadisticas estadisticas) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Estadisticas(int malicious) {}
    }
}
