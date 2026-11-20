package es.uclm.esibuy.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import es.uclm.esibuy.backend.shared.error.ManejadorAccesoDenegado;

/**
 * Configuración mínima de Spring Security.
 *
 * <p>
 * Por defecto se deniega, (mínimo privilegio) y solo se abren el registro,
 * los dos primeros pasos del login (HU2, T230), la recuperación de contraseña
 * (HU3) y la consulta de las categorías, porque quien los usa todavía no tiene
 * sesión. Sin sesión se responde 401 («no sé quién eres»); con sesión pero sin
 * permiso, 403.
 *
 * <p>
 * CSRF: Spring's SPA cookie-to-header pattern is used. React must read `XSRF-TOKEN` and return it as `X-XSRF-TOKEN` on state-changing requests, so this cookie intentionally has `HttpOnly=false`. The session cookie remains HttpOnly. The CSRF token is not a session credential and is checked against the request token. `Secure` and `SameSite=Strict` reduce exposure; this does not replace XSS prevention, since same-origin script execution can read the CSRF cookie. Protection also covers public registration routes.
 *
 * <p>
 * El logout ({@code POST /api/auth/logout}) lo hace Spring Security: invalida
 * la sesión, borra la cookie y responde 204 (T230, D14).
 *
 * <p>
 * La zona de administración ({@code /api/admin/**}) solo la usa el rol
 * ADMINISTRADOR (HU18, T58): ocultar el menú en el frontend es solo
 * experiencia de usuario, la barrera real es esta. Spring comprueba la
 * autoridad {@code ROLE_ADMINISTRADOR}, que el login tendrá que dar a cada
 * usuario según su rol.
 *
 * <p>
 * La zona de vendedor ({@code /api/seller/**}) solo la usa el rol VENDEDOR
 * (HU15, T57). El administrador supervisa los productos y pedidos de los
 * vendedores en solo lectura por su propia zona
 * ({@code /api/admin/sellers/...}, HU26); los datos de la cuenta del vendedor
 * sí los gestiona él (HU22, HU23 y HU24). Que el vendedor esté activo lo
 * comprueba el login.
 *
 * <p>
 * La zona de cliente ({@code /api/customer/**}) solo la usa el rol CLIENTE: de
 * momento, sus métodos de pago (HU6) y sus direcciones de envío (HU5).
 */
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

        private static final String RUTA_REGISTRO = "/api/auth/register/cliente";
        private static final String RUTA_REGISTRO_VENDEDOR = "/api/auth/register/vendedor";
        private static final String RUTA_LOGIN = "/api/auth/login";
        private static final String RUTA_LOGOUT = "/api/auth/logout";
        // Recuperación de contraseña (HU3): sin sesión, pero con token CSRF (no van en
        // ignoringRequestMatchers)
        private static final String RUTA_SOLICITAR_RECUPERACION = "/api/auth/password-recovery";
        private static final String RUTA_RESTABLECER_CONTRASENA = "/api/auth/password-recovery/confirm";
        // Página de error interna de Spring Boot: abierta para que un 403 o un 500 no
        // se conviertan en 401
        private static final String RUTA_ERROR = "/error";
        // Health check de Render (Actuator): abierto porque Render lo consulta sin
        // sesión. Es lo único de /actuator que se
        // expone (management.endpoints.web.exposure.include=health) y sin detalles de
        // los componentes.
        private static final String RUTAS_SALUD = "/actuator/health/**";
        private static final String RUTA_CODIGO_EMAIL = "/api/auth/verify-email-code";
        private static final String RUTA_ALTA_AUTHENTICATOR = "/api/auth/enroll-authenticator";
        private static final String RUTA_CODIGO_AUTHENTICATOR = "/api/auth/verify-authenticator-code";
        // Las categorías del catálogo son públicas: el registro del vendedor las elige
        // sin sesión.
        private static final String RUTA_CATEGORIAS = "/api/categorias";
        private static final String RUTAS_ADMINISTRACION = "/api/admin/**";
        // Texto y no el enum Rol: shared no depende del dominio de ningún módulo. Es el
        // mismo valor que Rol.ADMINISTRADOR.
        private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
        private static final String RUTAS_VENDEDOR = "/api/seller/**";
        private static final String ROL_VENDEDOR = "VENDEDOR";
        // El perfil propio (HU4) y eliminar la cuenta propia (HU25 y HU47) son del
        // cliente y del vendedor; el
        // administrador no tiene perfil aquí.
        private static final String RUTAS_PERFIL = "/api/perfil/**";
        private static final String ROL_CLIENTE = "CLIENTE";
        private static final String RUTAS_CLIENTE = "/api/customer/**";
        // Catálogo de productos del cliente (HU7) y detalle de cada producto (HU8):
        // solo los ve un CLIENTE.
        private static final String RUTAS_CATALOGO = "/api/products/**";
        // Valorar un producto (HU13): solo el CLIENTE, y solo esta ruta, no cualquier
        // POST bajo /api/products
        private static final String RUTA_VALORACIONES = "/api/products/*/valoraciones";
        // Rutas de seguridad: acceso restringido a usuarios autenticados
        private static final String RUTAS_SEGURIDAD = "/api/seguridad/**";

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.POST, RUTA_REGISTRO, RUTA_REGISTRO_VENDEDOR,
                                                                RUTA_LOGIN, RUTA_CODIGO_EMAIL,
                                                                RUTA_ALTA_AUTHENTICATOR, RUTA_CODIGO_AUTHENTICATOR,
                                                                RUTA_SOLICITAR_RECUPERACION,
                                                                RUTA_RESTABLECER_CONTRASENA)
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, RUTA_CATEGORIAS).permitAll()
                                                .requestMatchers(RUTA_ERROR).permitAll()
                                                .requestMatchers(HttpMethod.GET, RUTAS_SALUD).permitAll()
                                                .requestMatchers(RUTAS_ADMINISTRACION).hasRole(ROL_ADMINISTRADOR)
                                                .requestMatchers(RUTAS_VENDEDOR).hasRole(ROL_VENDEDOR)
                                                .requestMatchers(RUTAS_CLIENTE).hasRole(ROL_CLIENTE)
                                                .requestMatchers(RUTAS_PERFIL).hasAnyRole(ROL_CLIENTE, ROL_VENDEDOR)
                                                .requestMatchers(HttpMethod.GET, RUTAS_CATALOGO).hasRole(ROL_CLIENTE)
                                                .requestMatchers(HttpMethod.POST, RUTA_VALORACIONES)
                                                .hasRole(ROL_CLIENTE)
                                                .requestMatchers(RUTAS_SEGURIDAD)
                                                .hasAnyRole(ROL_CLIENTE, ROL_VENDEDOR, ROL_ADMINISTRADOR)
                                                .anyRequest().authenticated())
                                .exceptionHandling(excepciones -> excepciones
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                                                .accessDeniedHandler(new ManejadorAccesoDenegado()))
                                .logout(logout -> logout
                                                .logoutUrl(RUTA_LOGOUT)
                                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                                                                HttpStatus.NO_CONTENT))
                                                .deleteCookies("JSESSIONID"))
                                // spa(): React reads XSRF-TOKEN and copies it into X-XSRF-TOKEN (T230, D4).
                                // HttpOnly=false is required for that flow; Secure and SameSite=Strict
                                // limit transport and cross-site exposure. JSESSIONID remains HttpOnly.
                                .csrf(csrf -> csrf
                                                .spa()
                                                .csrfTokenRepository(repositorioTokenCsrf()));
                return http.build();
        }

        private static CookieCsrfTokenRepository repositorioTokenCsrf() {
                CookieCsrfTokenRepository repositorio = CookieCsrfTokenRepository.withHttpOnlyFalse();
                repositorio.setCookieCustomizer(cookie -> cookie.secure(true).sameSite("Strict"));
                return repositorio;
        }

}
