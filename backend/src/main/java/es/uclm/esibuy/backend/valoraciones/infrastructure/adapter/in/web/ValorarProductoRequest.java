package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;

/**
 * Cuerpo de POST /api/products/{productoId}/valoraciones: la puntuación de 1 a 5 y un comentario opcional. Un
 * clienteId en el cuerpo se ignora: el cliente sale de la sesión. Las reglas las comprueba el dominio.
 */
public record ValorarProductoRequest(int puntuacion, String comentario) {

    ValorarProductoCommand aComando(String emailCliente, String productoId) {
        return new ValorarProductoCommand(emailCliente, productoId, puntuacion, comentario);
    }
}
