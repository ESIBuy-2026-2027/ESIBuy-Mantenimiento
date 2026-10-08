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
 */
@Configuration
public class SecurityConfig {

    private static final String RUTA_REGISTRO = "/api/auth/registro";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, RUTA_REGISTRO).permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(RUTA_REGISTRO));
        return http.build();
    }
}
