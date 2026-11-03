package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

// Adaptador de AnalisisImagenPort con la API v3 de VirusTotal (HU31, T397). Primero busca el informe de la URL; si
// VirusTotal no la conoce, la envía a analizar y consulta el análisis como mucho maxConsultas veces. Por imagen son
// como mucho 2 + maxConsultas peticiones: con 2 consultas, las 4 por minuto de la API pública. Si VirusTotal falla o
// no responde, devuelve SIN_RESULTADO en lugar de lanzar la excepción.
public class VirusTotalAdapter implements AnalisisImagenPort {

    private static final String ANALISIS_TERMINADO = "completed";

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
        try {
            return consultarInforme(urlImagen);
        } catch (RestClientException e) {
            // Error de VirusTotal (401 API key, 429 cuota, 5xx) o sin conexión: no hay respuesta sobre la imagen
            return ResultadoAnalisis.SIN_RESULTADO;
        }
    }

    private ResultadoAnalisis consultarInforme(String urlImagen) {
        try {
            InformeUrl informe = virusTotal.get()
                    .uri("/urls/{id}", idDeUrl(urlImagen))
                    .retrieve()
                    .body(InformeUrl.class);
            return segun(informe.data().attributes().estadisticas());
        } catch (HttpClientErrorException.NotFound e) {
            // Nadie la ha analizado todavía: se envía a analizar y se espera un poco al resultado
            return analizarUrlNueva(urlImagen);
        }
    }

    private ResultadoAnalisis analizarUrlNueva(String urlImagen) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("url", urlImagen);
        EnvioAnalisis envio = virusTotal.post()
                .uri("/urls")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .body(EnvioAnalisis.class);
        String idAnalisis = envio.data().id();

        for (int consulta = 0; consulta < maxConsultas; consulta++) {
            esperar();
            Analisis analisis = virusTotal.get()
                    .uri("/analyses/{id}", idAnalisis)
                    .retrieve()
                    .body(Analisis.class);
            if (ANALISIS_TERMINADO.equals(analisis.data().attributes().status())) {
                return segun(analisis.data().attributes().stats());
            }
        }
        return ResultadoAnalisis.SIN_RESULTADO;
    }

    // Maliciosa si algún motor la marca como malicious; los sospechosos no cuentan (decisión del equipo)
    private static ResultadoAnalisis segun(Estadisticas estadisticas) {
        return estadisticas.malicious() > 0 ? ResultadoAnalisis.MALICIOSA : ResultadoAnalisis.SEGURA;
    }

    private void esperar() {
        try {
            Thread.sleep(esperaEntreConsultas);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // VirusTotal identifica una URL por la propia URL en Base64 URL-safe sin el relleno '='
    private static String idDeUrl(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }

    // Solo la parte del JSON de VirusTotal que se usa; el resto se ignora

    // Recuento de motores, igual en el informe de una URL y en un análisis
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Estadisticas(int malicious) {}

    // GET /urls/{id}
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InformeUrl(Datos data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Datos(Atributos attributes) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Atributos(@JsonProperty("last_analysis_stats") Estadisticas estadisticas) {}
    }

    // POST /urls: devuelve el id del análisis
    @JsonIgnoreProperties(ignoreUnknown = true)
    record EnvioAnalisis(Datos data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Datos(String id) {}
    }

    // GET /analyses/{id}: status queued, in-progress o completed
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Analisis(Datos data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Datos(Atributos attributes) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Atributos(String status, Estadisticas stats) {}
    }
}
