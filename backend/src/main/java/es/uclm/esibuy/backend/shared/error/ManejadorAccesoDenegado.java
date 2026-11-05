package es.uclm.esibuy.backend.shared.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 403 de las reglas por ruta de {@code SecurityConfig}, con el mismo cuerpo que el de la capa de negocio
 * ({@link ManejadorGlobalErrores#accesoDenegado()}): un mensaje genérico y el código ACCESS_DENIED, sin
 * detalles internos. Se escribe a mano porque esta respuesta se da en un filtro, antes de llegar a un controlador.
 */
public class ManejadorAccesoDenegado implements AccessDeniedHandler {

    static final String MENSAJE = "Acceso denegado";
    static final String CODIGO = "ACCESS_DENIED";

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException excepcion)
            throws IOException {
        respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"mensaje\":\"" + MENSAJE + "\",\"code\":\"" + CODIGO + "\"}");
    }
}