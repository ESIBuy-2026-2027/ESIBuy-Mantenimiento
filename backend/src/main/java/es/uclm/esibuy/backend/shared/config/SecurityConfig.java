package es.uclm.esibuy.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración mínima de Spring Security.
 *
 * <p>Por defecto se deniega todo (mínimo privilegio) y solo se abre el registro, porque quien
 * se registra todavía no tiene sesión. CSRF se ignora únicamente en el registro por el mismo
 * motivo: no hay sesión que un ataque CSRF pueda aprovechar. El login (HU2) añadirá aquí sus reglas.
 *
 * <p>La zona de administración ({@code /api/admin/**}) solo la usa el rol ADMINISTRADOR (HU18, T58):
 * ocultar el menú en el frontend es solo experiencia de usuario, la barrera real es esta. Spring
 * comprueba la autoridad {@code ROLE_ADMINISTRADOR}, que el login tendrá que dar a cada usuario según
 * su rol.
 */
@Configuration
public class SecurityConfig {

    private static final String RUTA_REGISTRO = "/api/auth/registro";
    private static final String RUTAS_ADMINISTRACION = "/api/admin/**";
    // Texto y no el enum Rol: shared no depende del dominio de ningún módulo. Es el mismo valor que Rol.ADMINISTRADOR.
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, RUTA_REGISTRO).permitAll()
                        .requestMatchers(RUTAS_ADMINISTRACION).hasRole(ROL_ADMINISTRADOR)
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(RUTA_REGISTRO));
        return http.build();
    }
}
