package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.incidencias.application.port.in.CrearIncidenciaCommand;

/**
 * Cuerpo de POST /api/perfil/incidencias: el asunto y la descripción. Un autorId en el cuerpo se ignora: el autor
 * sale de la sesión. Las reglas las comprueba el dominio.
 */
public record CrearIncidenciaRequest(String asunto, String descripcion) {

    CrearIncidenciaCommand aComando(String email) {
        return new CrearIncidenciaCommand(email, asunto, descripcion);
    }
}