package es.uclm.esibuy.backend.shared.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/**
 * Configuración de Jackson común a todos los módulos.
 *
 * <p>Por defecto Jackson convierte en booleano un texto ({@code "true"}) o un número ({@code 1}). El contrato de la
 * API solo admite {@code true} o {@code false}, así que esas conversiones se rechazan: el cuerpo no se puede leer y
 * responde 400 (ManejadorGlobalErrores). Solo afecta a los booleanos; los números escritos como texto se siguen
 * aceptando (decisión del equipo en la daily del 03/11/2026).
 */
@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer booleanosEstrictos() {
        return builder -> builder.withCoercionConfig(LogicalType.Boolean, config -> config
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail));
    }
}
