package es.uclm.esibuy.backend.usuarios.domain.exception;

/**
 * Por qué el dominio rechaza unos datos de registro. Es un valor cerrado para que la API pueda
 * devolver un {@code code} por cada motivo (fallos de la demo del Sprint 1) sin que el dominio
 * conozca el contrato con el frontend: el texto del {@code code} lo decide la infraestructura.
 */
public enum MotivoRechazo {
    CONTRASENA_CORTA,
    CONTRASENA_LARGA,
    CONTRASENA_DEBIL,
    CONTRASENA_COMUN,
    CONTRASENA_CON_DATOS_PERSONALES,
    CONTRASENAS_DISTINTAS,
    DNI_INVALIDO,
    TELEFONO_INVALIDO,
    MENOR_DE_EDAD,
    PRIVACIDAD_NO_ACEPTADA,
    DATO_INVALIDO
}
