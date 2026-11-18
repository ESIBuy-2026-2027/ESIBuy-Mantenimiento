
package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.uclm.esibuy.backend.usuarios.application.port.out.ComprobadorContrasenaComprometidaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.ComprobacionContrasenaException;

/**
 * Adaptador que consulta Have I Been Pwned para comprobar si una
 * contraseña aparece en bases de datos de contraseñas comprometidas.
 *
 * Utiliza el mecanismo k-anonymity: únicamente envía los cinco
 * primeros caracteres del hash SHA-1 de la contraseña.
 */
public class HibpContrasenaAdapter implements ComprobadorContrasenaComprometidaPort {

    private static final String HIBP_RANGE_URL = "https://api.pwnedpasswords.com/range/";

    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private static final int HASH_PREFIX_LENGTH = 5;

    private static final Pattern RESPONSE_LINE_PATTERN = Pattern.compile("^([0-9A-Fa-f]{35}):([0-9]+)$");

    private final HttpClient httpClient;

    public HibpContrasenaAdapter() {
        this(HttpClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .build());
    }

    HibpContrasenaAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean estaComprometida(String contrasena) {
        String hash = calcularHashSha1(contrasena);

        String prefijo = hash.substring(0, HASH_PREFIX_LENGTH);
        String sufijo = hash.substring(HASH_PREFIX_LENGTH);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HIBP_RANGE_URL + prefijo))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "text/plain")
                .GET()
                .build();

        HttpResponse<String> response;

        try {
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ComprobacionContrasenaException(
                    "La consulta de contraseñas comprometidas fue interrumpida.",
                    e);
        } catch (IOException e) {
            throw new ComprobacionContrasenaException(
                    "No se pudo consultar el servicio de contraseñas comprometidas.",
                    e);
        }

        if (response.statusCode() != 200) {
            throw new ComprobacionContrasenaException(
                    "El servicio de contraseñas comprometidas devolvió un error HTTP.");
        }

        return contieneSufijoComprometido(response.body(), sufijo);
    }

    private String calcularHashSha1(String contrasena) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] hashBytes = digest.digest(
                    contrasena.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of()
                    .withUpperCase()
                    .formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new ComprobacionContrasenaException(
                    "No se pudo calcular el hash de la contraseña.",
                    e);
        }
    }

    private boolean contieneSufijoComprometido(String respuesta, String sufijo) {
        if (respuesta == null || respuesta.isBlank()) {
            throw new ComprobacionContrasenaException(
                    "El servicio de contraseñas comprometidas devolvió una respuesta vacía.");
        }

        String[] lineas = respuesta.split("\\R");

        for (String linea : lineas) {
            if (linea.isBlank()) {
                continue;
            }

            Matcher matcher = RESPONSE_LINE_PATTERN.matcher(linea.trim());

            if (!matcher.matches()) {
                throw new ComprobacionContrasenaException(
                        "El servicio de contraseñas comprometidas devolvió una respuesta inválida.");
            }

            String sufijoRespuesta = matcher.group(1);
            long frecuencia;

            try {
                frecuencia = Long.parseLong(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new ComprobacionContrasenaException(
                        "El servicio de contraseñas comprometidas devolvió una frecuencia inválida.",
                        e);
            }

            if (sufijoRespuesta.equalsIgnoreCase(sufijo) && frecuencia > 0) {
                return true;
            }
        }

        return false;
    }
}
