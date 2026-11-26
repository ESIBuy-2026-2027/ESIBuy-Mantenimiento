package es.uclm.esibuy.backend.alertas.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/**
 * Consulta de sus alertas por parte del cliente (AB#529). Recibe el correo del cliente en sesión: solo ve las suyas.
 * Marcarlas como leídas (AB#531) y eliminarlas (AB#532) son de otras historias.
 */
public interface ConsultaAlertasUseCase {

    /** Las alertas del cliente, de la más reciente a la más antigua. Si no tiene, una lista vacía. */
    List<Alerta> consultar(String emailCliente);
}
