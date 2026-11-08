package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

/**
 * Cuerpo de PATCH /api/admin/incidencias/{id}/abrir (HU50): la respuesta del administrador, que verá el autor de la
 * incidencia. Las reglas (obligatoria, hasta 1000 caracteres) las comprueba el dominio.
 */
public record AbrirIncidenciaRequest(String respuesta) {
}
