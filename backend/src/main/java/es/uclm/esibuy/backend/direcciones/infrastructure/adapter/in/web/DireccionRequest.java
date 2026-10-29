package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.direcciones.application.port.in.DireccionCommand;

/**
 * Petición de alta o edición de una dirección de envío (HU5). Las reglas de cada campo las valida el dominio.
 * No lleva cliente ni principal: el cliente sale de la sesión y la principal se marca con su propio endpoint.
 */
public record DireccionRequest(
        String destinatario,
        String calle,
        String detalles,
        String codigoPostal,
        String ciudad,
        String provincia,
        String telefono) {

    public DireccionCommand aComando() {
        return new DireccionCommand(destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono);
    }
}
