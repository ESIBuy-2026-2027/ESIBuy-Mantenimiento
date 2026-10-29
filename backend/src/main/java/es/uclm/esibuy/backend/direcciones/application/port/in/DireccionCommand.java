package es.uclm.esibuy.backend.direcciones.application.port.in;

/**
 * Datos de entrada del alta o la edición de una dirección de envío (HU5). Lleva texto plano y es el servicio
 * quien construye los value objects del dominio.
 */
public record DireccionCommand(
        String destinatario,
        String calle,
        String detalles,
        String codigoPostal,
        String ciudad,
        String provincia,
        String telefono) {
}
