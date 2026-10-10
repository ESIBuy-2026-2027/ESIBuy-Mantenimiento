package es.uclm.esibuy.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

/**
 * Configuración mínima de Spring Security.
 *
 * <p>Por defecto se deniega todo (mínimo privilegio) y solo se abren el registro y los dos primeros
 * pasos del login (HU2, T230), porque quien los usa todavía no tiene sesión. Sin sesión se responde
 * 401 («no sé quién eres»); con sesión pero sin permiso, 403.
 *
 * <p>CSRF: el token va en la cookie {@code XSRF-TOKEN}, que el frontend puede leer, y el frontend lo
 * devuelve en la cabecera {@code X-XSRF-TOKEN} en cada petición que cambia datos (T230, D4). Se ignora
 * solo en el registro: no hay sesión que un ataque CSRF pueda aprovechar.
 *
 * <p>El logout ({@code POST /api/auth/logout}) lo hace Spring Security: invalida la sesión, borra la
 * cookie y responde 204 (T230, D14).
 *
 * <p>La zona de administración ({@code /api/admin/**}) solo la usa el rol ADMINISTRADOR (HU18, T58):
 * ocultar el menú en el frontend es solo experiencia de usuario, la barrera real es esta. Spring
 * comprueba la autoridad {@code ROLE_ADMINISTRADOR}, que el login tendrá que dar a cada usuario según
 * su rol.
 */
@Configuration
public class SecurityConfig {

    private static final String RUTA_REGISTRO = "/api/auth/register/cliente";
    private static final String RUTA_LOGIN = "/api/auth/login";
    private static final String RUTA_LOGOUT = "/api/auth/logout";
    // Página de error interna de Spring Boot: abierta para que un 403 o un 500 no se conviertan en 401
    private static final String RUTA_ERROR = "/error";
    private static final String RUTA_CODIGO_EMAIL = "/api/auth/verify-email-code";
    private static final String RUTA_ALTA_AUTHENTICATOR = "/api/auth/enroll-authenticator";
    private static final String RUTA_CODIGO_AUTHENTICATOR = "/api/auth/verify-authenticator-code";
    private static final String RUTAS_ADMINISTRACION = "/api/admin/**";
    // Texto y no el enum Rol: shared no depende del dominio de ningún módulo. Es el mismo valor que Rol.ADMINISTRADOR.
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, RUTA_REGISTRO, RUTA_LOGIN, RUTA_CODIGO_EMAIL,
                                RUTA_ALTA_AUTHENTICATOR, RUTA_CODIGO_AUTHENTICATOR).permitAll()
                        .requestMatchers(RUTA_ERROR).permitAll()
                        .requestMatchers(RUTAS_ADMINISTRACION).hasRole(ROL_ADMINISTRADOR)
                        .anyRequest().authenticated())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(logout -> logout
                        .logoutUrl(RUTA_LOGOUT)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        .deleteCookies("JSESSIONID"))
                // spa(): el token CSRF va en la cookie XSRF-TOKEN (legible por el frontend, sin HttpOnly) y
                // el frontend lo devuelve en la cabecera X-XSRF-TOKEN (T230, D4)
                .csrf(csrf -> csrf
                        .spa()
                        .ignoringRequestMatchers(RUTA_REGISTRO));
        return http.build();
    }

    
}
