package es.uclm.esibuy.backend.alertas.application.port.in;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/**
 * Gestión de sus alertas por parte del cliente: marcarlas como leídas (AB#531). Las demás operaciones del cliente
 * sobre sus alertas (p. ej. eliminarlas, AB#532) se añaden aquí. Recibe el correo del cliente en sesión: solo toca
 * las suyas.
 */
public interface GestionAlertasUseCase {

    /**
     * Marca como leída una alerta del cliente: sigue en su lista, pero deja de ser nueva. Si ya estaba leída, la deja
     * igual. Si no existe o es de otro cliente, lanza AlertaNoEncontradaException.
     */
    Alerta marcarComoLeida(String emailCliente, String alertaId);
}
